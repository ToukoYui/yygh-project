package com.atguigu.yygh.msm.service;

import com.atguigu.yygh.vo.msm.MsmVo;

import java.util.Map;

public interface MsmService {
    boolean sendCode(String phone,String code) throws Exception;
    //
    boolean sendCodeByMQ(MsmVo msmVo) throws Exception;

    boolean sendTip(String phone, Map<String,Object> param) throws Exception;
}
