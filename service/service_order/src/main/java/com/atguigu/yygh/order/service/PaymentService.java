package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface PaymentService extends IService<PaymentInfo> {
    void savePaymentInfo(OrderInfo orderInfo, Integer status);

    void paySuccess(String out_trade_no, Integer status, Map<String, String> resultMap);

    PaymentInfo getPatmentInfo(Long orderId,Integer paymentType);
}
