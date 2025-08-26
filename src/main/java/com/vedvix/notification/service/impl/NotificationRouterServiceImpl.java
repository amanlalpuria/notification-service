package com.vedvix.notification.service.impl;

import com.vedvix.notification.dto.ChannelType;
import com.vedvix.notification.dto.NotificationRequest;
import com.vedvix.notification.infrastructure.MessagingProducer;
import com.vedvix.notification.service.NotificationRouterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationRouterServiceImpl implements NotificationRouterService {

    private final MessagingProducer producer;

    @Override
    public void routeNotification(NotificationRequest request) {
        for (ChannelType channel : request.getChannels()) {
            producer.publishToQueue(channel, request);
        }
    }
}