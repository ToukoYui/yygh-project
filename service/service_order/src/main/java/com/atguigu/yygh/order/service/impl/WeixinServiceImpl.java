package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.RefundService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.ConstantPropertiesUtils;
import com.atguigu.yygh.order.utils.HttpClient;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeixinServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements WeixinService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RefundService refundService;


    //生成微信二维码
    @Override
    public Map createNative(Long orderId) {
        try {
            //从redis中获取订单信息
            Map payMap = (Map) redisTemplate.opsForValue().get(orderId.toString());
            if (payMap != null) {
                return payMap;
            }
            //获取订单信息
            OrderInfo orderInfo = orderService.getById(orderId);
            //向支付表中添加信息
            paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
            //设置参数，调用微信生成二维码接口
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = orderInfo.getReserveDate() + "就诊" + orderInfo.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");
            //发送请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //获取返回的相关数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、封装返回结果集
            System.out.println("resultMap = " + resultMap);
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url")); //二维码的url

            if (resultMap.get("result_code") != null) {//不为空说明返回有结果
                redisTemplate.opsForValue().set(orderId.toString(), map, 120, TimeUnit.MINUTES);
            }
            System.out.println("map = " + map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //根据订单号去微信第三方查询支付的状态
    @Override
    public Map<String, String> queryPayStatus(Long orderId, String name) {
        try{
            OrderInfo orderInfo = orderService.getById(orderId);
            //1、封装参数
            Map paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //2.设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();//发送请求
            //3.处理微信返回的请求响应
            String content = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(content);
            System.out.println("map = " + map);
            return map;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //微信退款
    @Override
    public Boolean isRefund(Long orderId) {
        // 获取支付信息
        PaymentInfo patmentInfo = paymentService.getPatmentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
        //添加支付信息到退款表中
        RefundInfo refundInfo = refundService.saveRefundInfo(patmentInfo);
        //判断订单是否已经退款
        if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()){
            return true;
        }
        //微信接口退款
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
        paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
        paramMap.put("transaction_id",patmentInfo.getTradeNo()); //微信订单号
        paramMap.put("out_trade_no",patmentInfo.getOutTradeNo()); //商户订单编号
        paramMap.put("out_refund_no","tk"+patmentInfo.getOutTradeNo()); //商户退款单号
//       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
        paramMap.put("total_fee","1");
        paramMap.put("refund_fee","1");
        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            // 设置证书
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();//发送请求

            //接收返回的数据
            String xmlContent = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlContent);
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundService.updateById(refundInfo);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
