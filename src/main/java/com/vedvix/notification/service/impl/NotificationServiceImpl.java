package com.vedvix.notification.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vedvix.notification.dto.NotificationRequest;
import com.vedvix.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void sendNotification(NotificationRequest request) {
        request.getChannels().forEach(channel -> {
            String routingKey = "notification_" + channel.name().toLowerCase();
            rabbitTemplate.convertAndSend("", routingKey, request);
            log.info("Published to routingKey: {}, user: {}", routingKey, request.getUserId());
        });
    }
}