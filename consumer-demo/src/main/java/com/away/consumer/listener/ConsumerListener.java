package com.away.consumer.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ConsumerListener {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "spring_test_queue"),
            exchange = @Exchange(value = "spring_test_exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"a.*"}
    ))
    public void listen(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println("消费者已经接受到消息：" + msg);
            int i = 1 / 0;
            // true 标识从最近确认的消息都要确认掉 一般都是false 
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            if (message.getMessageProperties().getRedelivered()){
                // 拒绝消息， 2-是否重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                // 不确认：1-邮票 2-是否批量操作 3-是否重新入队，如果设置为true则重新入队，如果设置为false（如果当前队列有死信队列的情况下，会进入死信队列）
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }

    }
}
