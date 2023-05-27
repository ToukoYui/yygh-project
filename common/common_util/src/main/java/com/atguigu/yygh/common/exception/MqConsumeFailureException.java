package com.atguigu.yygh.common.exception;

import com.atguigu.yygh.common.result.ResultCodeEnum;

public class MqConsumeFailureException extends RuntimeException{
    private Integer code;

    /**
     * 通过状态码和错误消息创建异常对象
     *
     * @param message
     * @param code
     */
    public MqConsumeFailureException(String message, Integer code) {
        //当在父类中定义了有参构造方法，但是没有定义无参构造方法时，编译器会强制要求我们定义一个相同参数类型的构造方法
        super(message);   //调用父类的有参构造（父类要是存在有参构造，子类必须也调用父类的有参构造）
        this.code = code;
    }

    /**
     * 接收枚举类型对象
     *
     * @param resultCodeEnum
     */
    public MqConsumeFailureException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }


    @Override
    public String toString() {
        return "MqConsumeFailureException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }
}
