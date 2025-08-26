package com.vedvix.notification.service;

import com.vedvix.notification.dto.NotificationRequest;

public interface NotificationRouterService {
    void routeNotification(NotificationRequest request);
}