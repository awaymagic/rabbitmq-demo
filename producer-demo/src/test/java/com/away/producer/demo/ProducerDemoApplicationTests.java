package com.away.producer.demo;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProducerDemoApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
        this.rabbitTemplate.convertAndSend("spring_test_exchange", "ab", "hello rabbitmq....");
        System.out.println("消息已发送------------------->");
    }

}
