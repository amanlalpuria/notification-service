package com.vedvix.notification.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.rest.api.v2010.account.Message;
import com.vedvix.notification.config.TwilioConfig;
import com.vedvix.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsNotificationWorker implements NotificationWorker {

    private final ObjectMapper objectMapper;
    @Autowired
    private TwilioConfig twilioConfig;
    @Override
    public void handleNotification(NotificationRequest request) {
        log.info("Sending SMS to {} using template {} and placeholders {}", request.getUserId(), request.getTemplateCode(), request.getPlaceholders());
        // TODO: Integrate Twilio here
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber("+918983375352"),
                new com.twilio.type.PhoneNumber(twilioConfig.getFromNumber()),request.getPlaceholders().toString()).create();
        System.out.println(message.getSid());
        log.info("Message sent successfully"+message.getSid());


    }

    @RabbitListener(queues = "${notification.queues.sms}")
    public void listen(NotificationRequest message) {
        try {
            //NotificationRequest request = objectMapper.readValue(message, NotificationRequest.class);
            handleNotification(message);
        } catch (Exception e) {
            log.error("Failed to process SMS notification", e);
        }
    }


}
