package com.vedvix.notification.service;

import com.vedvix.notification.config.TwilioConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
@RequiredArgsConstructor
public class SmsService {
    private final TwilioConfig twilioConfig;

    public void sendSms(String to, String message) {
        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(twilioConfig.getFromNumber()),
                message
        ).create();
    }
}
