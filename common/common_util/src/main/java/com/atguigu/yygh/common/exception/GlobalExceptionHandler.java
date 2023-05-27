package com.atguigu.yygh.common.exception;

import com.atguigu.yygh.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author: Touko
 * @Date: 2022/10/3 17:31
 * @Description: 全局异常处理器
 **/
@RestControllerAdvice   //一种对所有controller增强的组件，能够根据条件拦截下来
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)  //出现异常就拦截下来，走自定义的方法
    public Result error(Exception e) {
        e.printStackTrace();
        return Result.fail();
    }

    @ExceptionHandler(HospitalException.class)
    public Result error(HospitalException e) {
        e.printStackTrace();
        return Result.fail(e.toString());
    }
}
