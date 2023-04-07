package com.atguigu.yygh.msm.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.utils.RandomCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/msm")
public class MsmApiController {
    @Autowired
    private MsmService msmService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("send/{phone}")
    public Result sendCode(@PathVariable String phone) throws Exception {
        // 如果redis存在验证码缓存，则取缓存
        String code = stringRedisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(code)){
            return Result.ok("存在验证码缓存，无需生成验证码");
        }
        // 如果不存在则生成验证码，调用阿里云服务发送验证码到手机
        code = RandomCodeUtil.getSixBitRandom();
        boolean isSend = msmService.sendCode(phone,code);
        // 如果发送成功，将验证码放入缓存
        if (isSend){
            stringRedisTemplate.opsForValue().set(phone,code,1, TimeUnit.MINUTES);
            return Result.ok("发送短信成功");
        }
        return Result.fail("发送短信失败");
    }
}
