package com.vedvix.notification.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vedvix.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsNotificationWorker implements NotificationWorker {

    private final ObjectMapper objectMapper;

    @Override
    public void handleNotification(NotificationRequest request) {
        log.info("Sending SMS to {} using template {} and placeholders {}", request.getUserId(), request.getTemplateCode(), request.getPlaceholders());
        // TODO: Integrate Twilio here
    }

    @RabbitListener(queues = "${notification.queues.sms}")
    public void listen(String message) {
        try {
            NotificationRequest request = objectMapper.readValue(message, NotificationRequest.class);
            handleNotification(request);
        } catch (Exception e) {
            log.error("Failed to process SMS notification", e);
        }
    }
}
