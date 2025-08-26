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
        Notification notification = Notification.builder()
                .setTitle(request.getProjectId())
                .setBody(request.getPlaceholders().toString())
                .build();

        // For direct device messaging, use the following:
        /*
        Message message = Message.builder()
                .setToken("deviceToken")
                .setNotification(notification)
                .build();
        */
        // For topic-based messaging for dummy test as we don't have APP ready, use the following instead:
        Message message = Message.builder()
                .setTopic("test-topic")
                .setNotification(notification)
                .build();

        try {
            String result= FirebaseMessaging.getInstance().send(message);
            log.info("Push Notification sent successfully: {}", result);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "${notification.queues.push}")
    public void listen(NotificationRequest request) {
        try {
            log.info("Received Push Notification request");
            handleNotification(request);
        } catch (Exception e) {
            log.error("Failed to process push notification", e);
        }
    }

 */
}
