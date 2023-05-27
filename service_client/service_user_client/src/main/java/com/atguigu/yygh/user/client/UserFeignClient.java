package com.atguigu.yygh.user.client;

import com.atguigu.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Repository
@FeignClient("service-user")
public interface UserFeignClient {
    //根据就诊人id获取就诊人信息、
    @GetMapping("/api/user/patient/inner/get/{id}")
    Patient getPatientById(@PathVariable Long id);


}
