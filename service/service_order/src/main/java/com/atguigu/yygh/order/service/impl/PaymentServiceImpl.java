package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.HospitalException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;

import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.schedule.client.ScheduleFeignClient;
import com.atguigu.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ScheduleFeignClient scheduleFeignClient;

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        //判断支付表中是否存在相同的订单，不存在再添加
        LambdaQueryWrapper<PaymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOrderId, orderInfo.getId());
        wrapper.eq(PaymentInfo::getPaymentStatus, paymentType);
        Integer count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            return;
        }
        //添加记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(LocalDateTime.now());
        paymentInfo.setUpdateTime(LocalDateTime.now());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + "|" + orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymentInfo);
    }

    //修改支付表的支付状态
    @Override
    public void paySuccess(String out_trade_no, Integer status, Map<String, String> resultMap) {
        //1.获取支付记录
        LambdaQueryWrapper<PaymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOutTradeNo, out_trade_no);
        wrapper.eq(PaymentInfo::getPaymentType, PaymentTypeEnum.WEIXIN.getStatus());
        PaymentInfo paymentInfo = this.getOne(wrapper);
        //2.更新支付记录
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));//微信给的
        paymentInfo.setCallbackContent(resultMap.toString());
        baseMapper.updateById(paymentInfo);
        //3.根据订单号得到订单信息,然后更新
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfo.setUpdateTime(LocalDateTime.now());
        orderService.updateById(orderInfo);
        // 4.调用医院接口，通知更新支付状态
        SignInfoVo signInfoVo = scheduleFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if (null == signInfoVo) {
            throw new HospitalException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode", orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, "http://localhost:9998/order/updatePayStatus");
        if (result.getInteger("code") != 200) {
            throw new HospitalException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }

    }

    // 根据订单号和支付类型获取支付记录,用于退款
    @Override
    public PaymentInfo getPatmentInfo(Long orderId, Integer paymentType) {
        LambdaQueryWrapper<PaymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOrderId,orderId);
        wrapper.eq(PaymentInfo::getPaymentType,paymentType);
        PaymentInfo paymentInfo = this.getOne(wrapper);
        return paymentInfo;
    }
}
