package com.vedvix.notification.controller;

import com.vedvix.notification.entity.PushNotificationConfig;
import com.vedvix.notification.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService; // Injected via constructor

    @GetMapping("/getPushNotificationConfig")
    public ResponseEntity<List<PushNotificationConfig>> getPushNotificationConfig() {
        List<PushNotificationConfig> configData = configService.getPushNotificationConfig();
        return ResponseEntity.ok(configData);
    }
}