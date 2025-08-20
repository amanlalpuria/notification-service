package com.vedvix.notification.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vedvix.notification.dto.NotificationRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationWorker implements NotificationWorker {

    private final ObjectMapper objectMapper;

    @Override
    public void handleNotification(NotificationRequest request) {
        log.info("Sending Email to {} with template {} and placeholders {}", request.getUserId(), request.getTemplateCode(), request.getPlaceholders());

        // TODO: Integrate AWS SES here
    }
    /*
    @RabbitListener(queues = "${notification.queues.email}")
    public void listen(String message) {
        try {
            NotificationRequest request = objectMapper.readValue(message, NotificationRequest.class);
            handleNotification(request);
        } catch (Exception e) {
            log.error("Failed to process email notification", e);
        }
    }

     */
}
