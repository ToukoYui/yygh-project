package com.atguigu.yygh.msm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;
import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.utils.ConstantPropertiesUtils;
import com.atguigu.yygh.msm.utils.Sample;
import com.atguigu.yygh.vo.msm.MsmVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.aliyun.teautil.models.RuntimeOptions;
import com.aliyun.teautil.Common;
import java.util.HashMap;
import java.util.Map;

@Service
public class MsmServiceImpl implements MsmService {
    @Override
    public boolean sendCode(String phone,String code) throws Exception {
        //判断手机号是否为空
        if(StringUtils.isEmpty(phone)) {
            return false;
        }
        Client client =
                Sample.createClient(ConstantPropertiesUtils.ACCESS_KEY_ID, ConstantPropertiesUtils.SECRECT);
        Map<String,String> codeMap = new HashMap<>();
        codeMap.put("code",code);
        SendSmsRequest sendSmsRequest =
                new SendSmsRequest()
                .setSignName("阿里云短信测试")
                .setTemplateCode("SMS_154950909")
                .setPhoneNumbers(phone)
                .setTemplateParam(JSONObject.toJSONString(codeMap));
        System.out.println("codeMap = " + JSONObject.toJSONString(codeMap));
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            SendSmsResponse sendSmsResponse = client.sendSmsWithOptions(sendSmsRequest, runtime);
            System.out.println("sendSmsResponse = " + sendSmsResponse.statusCode);
            return true;
        } catch (TeaException error) {
            // 如有需要，请打印 error
            Common.assertAsString(error.message);
            System.out.println("error1 = " + error);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 如有需要，请打印 error
            System.out.println("error2="+error);
            com.aliyun.teautil.Common.assertAsString(error.message);
        }
        return false;
    }

    @Override
    public boolean sendCodeByMQ(MsmVo msmVo) throws Exception {
        //判断手机号是否为空
        if(!StringUtils.isEmpty(msmVo.getPhone())) {
            boolean isSend = this.sendTip(msmVo.getPhone(), msmVo.getParam());
            return isSend;
        }

        return false;
    }


    @Override
    public boolean sendTip(String phone,Map<String,Object> param) throws Exception {
        //判断手机号是否为空
        if(StringUtils.isEmpty(phone)) {
            return false;
        }
        Client client =
                Sample.createClient(ConstantPropertiesUtils.ACCESS_KEY_ID, ConstantPropertiesUtils.SECRECT);
        SendSmsRequest sendSmsRequest =
                new SendSmsRequest()
                        .setSignName("阿里云短信测试")
                        .setTemplateCode("SMS_154950909")
                        .setPhoneNumbers(phone)
                        .setTemplateParam(JSONObject.toJSONString(param));
        System.out.println("codeMap = " + JSONObject.toJSONString(param));
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            SendSmsResponse sendSmsResponse = client.sendSmsWithOptions(sendSmsRequest, runtime);
            System.out.println("sendSmsResponse = " + sendSmsResponse.statusCode);
            return true;
        } catch (TeaException error) {
            // 如有需要，请打印 error
            Common.assertAsString(error.message);
            System.out.println("error1 = " + error);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 如有需要，请打印 error
            System.out.println("error2="+error);
            com.aliyun.teautil.Common.assertAsString(error.message);
        }
        return false;
    }
}


