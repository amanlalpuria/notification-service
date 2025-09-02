package com.vedvix.notification.entity.repository;

import com.vedvix.notification.entity.PushNotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationConfigRepository extends JpaRepository<PushNotificationConfig, Long> {
}
