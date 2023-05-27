package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface WeixinService extends IService<PaymentInfo> {
    Map createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId, String name);

    Boolean isRefund(Long orderId);
}
