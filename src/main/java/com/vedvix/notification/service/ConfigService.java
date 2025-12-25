package com.vedvix.notification.service;

import com.vedvix.notification.entity.PushNotificationConfig;

import java.util.List;

public interface ConfigService {
    List<PushNotificationConfig> getPushNotificationConfig();
}