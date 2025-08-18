package com.vedvix.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
/*

    @Value("${notification.queues.email}")
    private String emailQueue;
*/
    @Value("${notification.queues.sms}")
    private String smsQueue;
/*
    @Value("${notification.queues.push}")
    private String pushQueue;


*/
    @Value("${notification.exchange}")
    private String exchangeName;

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(exchangeName);
    }
    @Bean
    public Queue smsNotificationQueue() {
        return new Queue(smsQueue);
    }
/*
    @Bean
    public Queue emailNotificationQueue() {
        return new Queue(emailQueue);
    }



    @Bean
    public Queue pushNotificationQueue() {
        return new Queue(pushQueue);
    }

    @Bean
    public Binding bindEmailQueue() {
        return BindingBuilder.bind(emailNotificationQueue())
                .to(notificationExchange())
                .with("notify.email");
    }



    @Bean
    public Binding bindPushQueue() {
        return BindingBuilder.bind(pushNotificationQueue())
                .to(notificationExchange())
                .with("notify.push");
    }*/

    @Bean
    public Binding bindSmsQueue() {
        return BindingBuilder.bind(smsNotificationQueue())
                .to(notificationExchange())
                .with("notify.sms");
    }
}
