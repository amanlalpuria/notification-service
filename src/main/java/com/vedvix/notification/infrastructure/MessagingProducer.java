package com.vedvix.notification.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vedvix.notification.dto.ChannelType;
import com.vedvix.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessagingProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publishToQueue(ChannelType channel, NotificationRequest request) {
        try {
            String message = objectMapper.writeValueAsString(request);
            rabbitTemplate.convertAndSend("notification.exchange", "notify." + channel.name().toLowerCase(), message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize notification", e);
        }
    }
}