package com.vedvix.notification.service;

import com.vedvix.notification.entity.PushNotificationConfig;
import com.vedvix.notification.entity.repository.NotificationConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final NotificationConfigRepository repository;

    @Override
    public List<PushNotificationConfig> getPushNotificationConfig() {
        return repository.findAll();
    }
}