package com.atguigu.yygh.hosp.config;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.atguigu.yygh.hosp.mapper")
public class HospConfig {

}
