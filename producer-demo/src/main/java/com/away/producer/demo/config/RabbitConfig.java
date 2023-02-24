package com.away.producer.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * 死信队列：没有死信队列这个概念
     * 	死信：Dead Letter
     * 	消息变成死信消息的场景？
     * 		1.执行basicNack或者basicReject方法时requeue为false
     * 		2.生存时间已到
     * 		3.队列已满仍然入队
     * 	死信交换机：本质就是普通的交换机，只是转发的是死信消息而已
     *
     *  延时队列：可以给队列或者是消息设置过期时间
     */

    /**
     * 业务交换机:spring_test_exchange2
     */
    @Bean
    public Exchange exchange() {
        // TopicExchange 交换机名称、是否持久化、是否自动删除（当该交换机未任何生产者或队列绑定情况下则该交换机删除）
        // return new TopicExchange("spring_test_exchange2", true, false);
        // 构造方式，和上边创建一致（durable默认就是持久化）
        return ExchangeBuilder.topicExchange("spring_test_exchange2").durable(true).build();
    }

    /**
     * 业务队列:spring_test_queue2
     */
    // exclusive 如果队列是排他队列，则这个队列不能有其他消费者.如果先创建了队列 1 后创建啦队列2 3，那么2 3 将成为无效队列。一般设置为false
    @Bean
    public Queue queue() {
        Map<String, Object> arguments = new HashMap<>(2);
        // 指定死信交换机及死信的 routingkey (key 为固定写法)
        arguments.put("x-dead-letter-exchange", "spring_dead_exchange");
        arguments.put("x-dead-letter-routing-key", "msg.dead");
        // 构造一个新队列，给定名称、持久性标志、自动删除标志和参数。
        // 参数：
        // 名称 – 队列的名称 - 不得为空;设置为 “” 以使代理生成名称。
        // 持久 – 如果我们声明持久队列，则为 true（队列将在服务器重新启动后幸存下来）
        // 独占 – 如果我们声明一个独占队列，则为 true（该队列将仅由声明者的连接使用）
        // autoDelete – 如果服务器在不再使用队列时应将其删除，则为 true
        // 参数 – 用于声明队列的参数
        // return new Queue("spring_test_queue2", true, false, false, arguments);

        // 也可以用构建器方式
        return QueueBuilder.durable("spring_test_queue2").withArguments(arguments).build();
    }

    /**
     * 把业务队列 binding 到交换机: msg.test
     */
    @Bean
    public Binding binding(Queue queue, Exchange exchange) {
        // return new Binding("spring_test_queue2", Binding.DestinationType.QUEUE, "spring_test_exchange2", "msg.test", null);
        // 构建器创建方式:把哪个队列给哪个交换机,指定routingkey.这里利用了@Bean 参数为以上两个bean注入
        return BindingBuilder.bind(queue).to(exchange).with("msg.test").noargs();
    }

    /**
     * 死信交换机:spring_dead_exchange
     */
    @Bean
    public Exchange deadExchange() {
        // TopicExchange 交换机名称、是否持久化、是否自动删除（当该交换机未任何生产者或队列绑定情况下则该交换机删除）
        return new TopicExchange("spring_dead_exchange", true, false);
    }

    /**
     * 死信队列:spring_dead_queue
     */
    @Bean
    public Queue deadQueue() {
        return new Queue("spring_dead_queue", true, false, false);
    }

    /**
     * 把死信队列 binding 到死信交换机: msg.dead
     */
    @Bean
    public Binding deadBinding() {
        return new Binding("spring_dead_queue", Binding.DestinationType.QUEUE, "spring_dead_exchange", "msg.dead", null);
    }

}
