package com.away.producer.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration // 统一给rabbitTemplate设置回调
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        // 是否到达交换机回调，不管有没有到达交换机都会执行
        this.rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause) -> {
            if (ack) {
                log.info("消息到达啦交换机-------------->");
            } else {
                log.error("消息没有到达交换机--------------> cause:{}", cause);
            }
        });
        // 是否到达队列的回调，只有没有到达队列时才执行
        // message 消息体 replyCode 状态码 replyText 响应内容 exchange 交换机 routingKey
        this.rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey) -> {
            System.out.println("消息没有到达队列");
            log.error("消息没有到达队列--------------> 状态码:{},响应内容{},交换机:{},路由键:{},消息内容:{}",
                    replyCode, replyText, exchange, routingKey, new String(message.getBody()));
        });

        // 如何测试？
        // spring_test_exchange不一样可以测试是否到达交换机回调
        // a.b 不一样可以测试队列
        // this.rabbitTemplate.convertAndSend("spring_test_exchange", "a.b", "hello rabbitmq....");
    }
}
