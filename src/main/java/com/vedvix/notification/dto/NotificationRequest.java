package com.vedvix.notification.dto;

import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
public class NotificationRequest {
    private String projectId;
    private String userId;
    private List<ChannelType> channels; // PUSH, EMAIL, SMS
    private String templateCode;
    private Map<String, String> placeholders;
}
