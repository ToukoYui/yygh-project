package com.atguigu.yygh.task.schedule;

import com.atguigu.yygh.common.constant.MqConst;
import com.atguigu.yygh.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ScheduleTask {
    @Autowired
    private RabbitService rabbitService;
    //  0 0 8 * * ?
    //
    //每天8点执行方法，就医提醒
    @Scheduled(cron = "0 0 8 * * ?") //cron表达式，设置执行间隔
    public void taskPatient(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_8,"");
    }
}
