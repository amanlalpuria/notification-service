package com.vedvix.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "notification_config")
@Data
public class PushNotificationConfig {
    @Id
    private Long id;
    private String projectId;
    private String key;
    private String value;
}
