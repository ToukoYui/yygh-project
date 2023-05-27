package com.atguigu.yygh.common.utils;

import com.atguigu.yygh.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;

//获取当前用户信息工具类(通过token获取用户id和用户名字)
public class AuthContextHolder {
    // 获取用户id
    public static Long getUserId(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        return userId;
    }

    // 获取用户name
    public static String getUserName(HttpServletRequest request){
        String token = request.getHeader("token");
        String userName = JwtHelper.getUserName(token);
        return userName;
    }
}
