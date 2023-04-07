package com.atguigu.yygh.cmn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//指定要调用的服务名，该服务名需要在对应的服务模块的配置文件中配置
//(spring.application.name=service_cmn)
@FeignClient("service-cmn")
@Repository
public interface DictFeignClient {
    // 医院等级的value可能不唯一，所以还需要dictCode
    @GetMapping("/admin/cmn/dict/getName/{dictCode}/{value}")
    String getName(@PathVariable("dictCode") String dictCode, @PathVariable("value") String value);

    @GetMapping("/admin/cmn/dict/getName/{value}")
    String getName(@PathVariable("value") String value);
}
