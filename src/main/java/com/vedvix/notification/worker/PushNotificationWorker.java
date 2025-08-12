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
public class PushNotificationWorker implements NotificationWorker {

    private final ObjectMapper objectMapper;

    @Override
    public void handleNotification(NotificationRequest request) {
        log.info("Sending Push Notification to {} with template {}", request.getUserId(), request.getTemplateCode());
        // TODO: Integrate Firebase FCM here
    }

    @RabbitListener(queues = "${notification.queues.push}")
    public void listen(String message) {
        try {
            NotificationRequest request = objectMapper.readValue(message, NotificationRequest.class);
            handleNotification(request);
        } catch (Exception e) {
            log.error("Failed to process push notification", e);
        }
    }
}
