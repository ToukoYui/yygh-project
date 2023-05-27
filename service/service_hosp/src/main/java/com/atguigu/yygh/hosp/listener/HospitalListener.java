package com.atguigu.yygh.hosp.listener;

import com.atguigu.yygh.common.constant.MqConst;
import com.atguigu.yygh.common.exception.MqConsumeFailureException;
import com.atguigu.yygh.common.service.RabbitService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HospitalListener {
    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 设置redis的key，查询失败次数
        String redisKey = orderMqVo.toString();
        Integer count = Integer.parseInt(stringRedisTemplate.opsForValue().get(redisKey));
        try {
            if (null != orderMqVo.getAvailableNumber()) {
                //下单成功更新预约数
                Schedule schedule = scheduleService.getById(orderMqVo.getScheduleId());
                schedule.setReservedNumber(orderMqVo.getReservedNumber());
                schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
                scheduleService.update(schedule);
            } else {
                //取消预约更新预约数
                Schedule schedule = scheduleService.getById(orderMqVo.getScheduleId());
                int availableNumber = schedule.getAvailableNumber().intValue() + 1;
                schedule.setAvailableNumber(availableNumber);
                scheduleService.update(schedule);
            }
            // 修改成功，返回ack回队列
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 如果更新失败的话，处理异常：nack消息的同时在redis中记录失败次数
            try {
                channel.basicNack(deliveryTag, false, true);
                if (count==0){
                    stringRedisTemplate.opsForValue().set(redisKey,String.valueOf(1));
                }else if (count >= 3) {
                    stringRedisTemplate.opsForValue().getOperations().delete(redisKey);
                    rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,"数据库更新异常，请处理");
                    return;
                }
                stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(count + 1));
                e.printStackTrace();
                throw new MqConsumeFailureException("更新号源预约信息消费失败", 500);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        //发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if (null != msmVo) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
            System.out.println("发送短信取消预约---->" + msmVo);
        }


    }
}
