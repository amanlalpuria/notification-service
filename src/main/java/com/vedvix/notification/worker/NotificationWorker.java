package com.vedvix.notification.worker;


import com.vedvix.notification.dto.NotificationRequest;

public interface NotificationWorker {
    void handleNotification(NotificationRequest request);
}