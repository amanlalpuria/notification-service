package com.vedvix.notification.service;

import com.vedvix.notification.dto.NotificationRequest;

public interface NotificationService {
    void sendNotification(NotificationRequest request);
}