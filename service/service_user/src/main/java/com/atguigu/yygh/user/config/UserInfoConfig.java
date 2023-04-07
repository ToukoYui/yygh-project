package com.atguigu.yygh.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
// 定义之后编译时会在指定的包下生成包下mapper接口的对应实现类
@MapperScan("com.atguigu.yygh.user.mapper")
public class UserInfoConfig {
}
