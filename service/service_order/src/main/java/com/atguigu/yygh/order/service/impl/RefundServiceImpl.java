package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.mapper.RefundInfoMapper;
import com.atguigu.yygh.order.service.RefundService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RefundServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundService {
    // 添加退款信息
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        //先判断表中是否存在重复数据
        LambdaQueryWrapper<RefundInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefundInfo::getOrderId,paymentInfo.getOrderId());
        wrapper.eq(RefundInfo::getPaymentType,paymentInfo.getPaymentType());
        RefundInfo refundInfo = this.getOne(wrapper);
        if (refundInfo != null){
            return refundInfo;
        }
        //添加记录
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(LocalDateTime.now());
        refundInfo.setUpdateTime(LocalDateTime.now());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refundInfo.setSubject(paymentInfo.getSubject());
        //paymentInfo.setSubject("test");
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        this.save(refundInfo);
        return refundInfo;
    }
}
