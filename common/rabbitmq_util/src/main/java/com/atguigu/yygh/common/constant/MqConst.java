package com.atguigu.yygh.common.constant;

public class MqConst {
    /**
     * 预约下单
     */
    public static final String EXCHANGE_DIRECT_ORDER
            = "exchange.direct.order";
    public static final String ROUTING_ORDER = "order";
    //队列
    public static final String QUEUE_ORDER  = "queue.order";


    /**
     * 短信
     */
    public static final String EXCHANGE_DIRECT_MSM = "exchange.direct.msm";
    public static final String ROUTING_MSM_ITEM = "msm.item";
    //队列
    public static final String QUEUE_MSM_ITEM  = "queue.msm.item";

    /**
     * 定时任务提醒
     */
    public static final String EXCHANGE_DIRECT_TASK = "exchange.direct.task";
    public static final String ROUTING_TASK_8 = "task.8";
    //队列
    public static final String QUEUE_TASK_8 = "queue.task.8";

    /**
     * 号源更新
     */
    public static final String EXCHANGE_Number_Source = "exchange.number_source";
    public static final String ROUTING_Number_Source = "task.number_source";
    //队列
    public static final String QUEUE_Number_Source = "queue.number_source";
}

