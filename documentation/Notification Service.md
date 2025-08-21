# Documentation: Multi-Channel Notification Service - Generic Spring Boot Microservice

## Executive Summary

This document provides comprehensive technical specifications for a generic, multi-tenant notification service built on Spring Boot microservices architecture. The service supports SMS, email, and push notifications with customizable templates and configuration management through a self-service portal. Customers can configure their own API keys, create templates, and send notifications via a unified REST API.

***

## 1. System Overview

### 1.1. Architecture Goals

- **Multi-tenant Support:** Each tenant maintains isolated configurations, templates, and API credentials
- **Channel Agnostic:** Unified interface supporting SMS, email, and push notifications
- **Template Engine:** Dynamic message generation with variable substitution
- **Self-Service Portal:** Customer dashboard for configuration management
- **Scalable Design:** Microservices architecture with horizontal scaling capabilities


### 1.2. Key Components

| Component | Purpose | Technology |
| :-- | :-- | :-- |
| Notification Core Service | Main processing engine | Spring Boot |
| Configuration Management | Tenant settings \& API keys | Spring Boot + JPA |
| Template Service | Message template processing | Spring Boot + FreeMarker/Thymeleaf |
| Channel Adapters | Provider-specific integrations | Spring Boot modules |
| Admin Portal | Self-service configuration UI | React/Angular |
| Message Queue | Async processing | RabbitMQ/Kafka |


***

## 2. API Request Structure

### 2.1. Unified Notification API

**Endpoint:** `POST /api/v1/notifications/send`

**Request Body Structure:**

```json
{
  "tenant_id": "abc123",
  "channel_type": "PUSH_NOTIFICATION|EMAIL|SMS",
  "template_name": "welcome_message",
  "recipients": ["recipient_identifier"],
  "variables": {
    "firstName": "John",
    "planName": "Premium",
    "customField": "value"
  },
  "priority": "HIGH|MEDIUM|LOW",
  "scheduled_time": "2025-08-21T15:30:00Z",
  "metadata": {
    "campaign_id": "camp_001",
    "tracking_id": "track_123"
  }
}
```


### 2.2. Channel-Specific Examples

#### Push Notification Request

```json
{
  "tenant_id": "ecommerce_tenant",
  "channel_type": "PUSH_NOTIFICATION",
  "template_name": "order_confirmation",
  "recipients": ["fcm_token_xyz123", "fcm_token_abc456"],
  "variables": {
    "customerName": "Sarah",
    "orderNumber": "ORD-2025-001",
    "totalAmount": "$149.99"
  }
}
```


#### Email Request

```json
{
  "tenant_id": "saas_platform",
  "channel_type": "EMAIL",
  "template_name": "password_reset",
  "recipients": ["user@example.com"],
  "variables": {
    "userName": "John Doe",
    "resetLink": "https://app.example.com/reset?token=xyz",
    "expiryTime": "24 hours"
  }
}
```


#### SMS Request

```json
{
  "tenant_id": "banking_app",
  "channel_type": "SMS", 
  "template_name": "otp_verification",
  "recipients": ["+1234567890"],
  "variables": {
    "otpCode": "123456",
    "expiryMinutes": "5"
  }
}
```


***

## 3. Database Schema Design

### 3.1. Core Tables

#### Tenants Table

```sql
CREATE TABLE tenants (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```


#### Tenant Configurations Table

```sql
CREATE TABLE tenant_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(255) REFERENCES tenants(id),
    channel_type ENUM('EMAIL', 'SMS', 'PUSH_NOTIFICATION'),
    provider VARCHAR(100), -- 'firebase', 'twilio', 'sendgrid', etc.
    api_key_encrypted TEXT,
    configuration_json TEXT, -- Additional provider-specific config
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_tenant_channel (tenant_id, channel_type)
);
```


#### Templates Table

```sql
CREATE TABLE notification_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(255) REFERENCES tenants(id),
    template_name VARCHAR(255),
    channel_type ENUM('EMAIL', 'SMS', 'PUSH_NOTIFICATION'),
    subject_template TEXT, -- For email
    body_template TEXT NOT NULL,
    variables_schema JSON, -- Expected variable names and types
    language_code VARCHAR(10) DEFAULT 'en',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_tenant_template (tenant_id, template_name, channel_type, language_code)
);
```


#### Notification Logs Table

```sql
CREATE TABLE notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(255) REFERENCES tenants(id),
    channel_type ENUM('EMAIL', 'SMS', 'PUSH_NOTIFICATION'),
    template_name VARCHAR(255),
    recipient VARCHAR(255),
    status ENUM('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED'),
    provider_response TEXT,
    error_message TEXT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```


***

## 4. Spring Boot Service Implementation

### 4.1. Core Service Structure

#### Notification Service Interface

```java
@Service
public interface NotificationService {
    NotificationResponse sendNotification(NotificationRequest request);
    NotificationResponse scheduleNotification(NotificationRequest request, LocalDateTime scheduledTime);
    List<NotificationLog> getNotificationHistory(String tenantId, NotificationFilter filter);
}
```


#### Main Implementation

```java
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
    
    @Autowired
    private TenantConfigurationService configService;
    
    @Autowired
    private TemplateService templateService;
    
    @Autowired
    private Map<String, ChannelAdapter> channelAdapters;
    
    @Autowired
    private NotificationLogRepository logRepository;
    
    @Override
    public NotificationResponse sendNotification(NotificationRequest request) {
        try {
            // 1. Validate tenant and get configuration
            TenantConfiguration config = configService.getConfiguration(
                request.getTenantId(), 
                request.getChannelType()
            );
            
            // 2. Process template with variables
            ProcessedTemplate template = templateService.processTemplate(
                request.getTenantId(),
                request.getTemplateName(),
                request.getChannelType(),
                request.getVariables()
            );
            
            // 3. Get appropriate channel adapter
            ChannelAdapter adapter = channelAdapters.get(
                request.getChannelType().toLowerCase() + "Adapter"
            );
            
            // 4. Send notification via adapter
            ChannelResponse response = adapter.sendNotification(
                config, template, request.getRecipients()
            );
            
            // 5. Log the result
            logNotification(request, response);
            
            return NotificationResponse.success(response);
            
        } catch (Exception e) {
            logError(request, e);
            return NotificationResponse.error(e.getMessage());
        }
    }
}
```


### 4.2. Template Processing Service

```java
@Service
public class TemplateService {
    
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    
    @Autowired
    private NotificationTemplateRepository templateRepository;
    
    public ProcessedTemplate processTemplate(String tenantId, String templateName, 
            ChannelType channelType, Map<String, Object> variables) {
        
        // Fetch template from database
        NotificationTemplate template = templateRepository
            .findByTenantIdAndTemplateNameAndChannelType(tenantId, templateName, channelType)
            .orElseThrow(() -> new TemplateNotFoundException("Template not found"));
        
        try {
            // Process subject (for email)
            String processedSubject = null;
            if (template.getSubjectTemplate() != null) {
                Template subjectTemplate = new Template("subject", 
                    template.getSubjectTemplate(), freeMarkerConfigurer.getConfiguration());
                processedSubject = FreeMarkerTemplateUtils.processTemplateIntoString(
                    subjectTemplate, variables);
            }
            
            // Process body
            Template bodyTemplate = new Template("body", 
                template.getBodyTemplate(), freeMarkerConfigurer.getConfiguration());
            String processedBody = FreeMarkerTemplateUtils.processTemplateIntoString(
                bodyTemplate, variables);
            
            return ProcessedTemplate.builder()
                .subject(processedSubject)
                .body(processedBody)
                .channelType(channelType)
                .build();
                
        } catch (Exception e) {
            throw new TemplateProcessingException("Failed to process template", e);
        }
    }
}
```


***

## 5. Channel Adapters Implementation

### 5.1. Firebase Push Notification Adapter

```java
@Component("pushNotificationAdapter")
public class FirebasePushAdapter implements ChannelAdapter {
    
    private final Map<String, FirebaseApp> tenantApps = new ConcurrentHashMap<>();
    
    @Override
    public ChannelResponse sendNotification(TenantConfiguration config, 
            ProcessedTemplate template, List<String> recipients) {
        
        try {
            FirebaseApp app = getOrCreateFirebaseApp(config);
            FirebaseMessaging messaging = FirebaseMessaging.getInstance(app);
            
            List<String> successfulTokens = new ArrayList<>();
            List<String> failedTokens = new ArrayList<>();
            
            for (String token : recipients) {
                try {
                    Message message = Message.builder()
                        .setToken(token)
                        .setNotification(Notification.builder()
                            .setTitle(template.getSubject())
                            .setBody(template.getBody())
                            .build())
                        .build();
                        
                    String response = messaging.send(message);
                    successfulTokens.add(token);
                    
                } catch (Exception e) {
                    failedTokens.add(token);
                    log.error("Failed to send push notification to token: {}", token, e);
                }
            }
            
            return ChannelResponse.builder()
                .successful(successfulTokens)
                .failed(failedTokens)
                .build();
                
        } catch (Exception e) {
            return ChannelResponse.error("Firebase send failed: " + e.getMessage());
        }
    }
    
    private FirebaseApp getOrCreateFirebaseApp(TenantConfiguration config) throws IOException {
        String tenantId = config.getTenantId();
        
        return tenantApps.computeIfAbsent(tenantId, key -> {
            try {
                // Decrypt and parse Firebase configuration
                String decryptedConfig = encryptionService.decrypt(config.getApiKeyEncrypted());
                InputStream serviceAccount = new ByteArrayInputStream(decryptedConfig.getBytes());
                
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
                    
                return FirebaseApp.initializeApp(options, tenantId);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Firebase app for tenant: " + tenantId, e);
            }
        });
    }
}
```


### 5.2. Email Adapter Implementation

```java
@Component("emailAdapter")
public class EmailAdapter implements ChannelAdapter {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Override
    public ChannelResponse sendNotification(TenantConfiguration config, 
            ProcessedTemplate template, List<String> recipients) {
        
        List<String> successful = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        
        for (String email : recipients) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                // Configure from tenant settings
                JSONObject configJson = new JSONObject(config.getConfigurationJson());
                helper.setFrom(configJson.getString("fromEmail"));
                helper.setTo(email);
                helper.setSubject(template.getSubject());
                helper.setText(template.getBody(), true); // HTML content
                
                mailSender.send(message);
                successful.add(email);
                
            } catch (Exception e) {
                failed.add(email);
                log.error("Failed to send email to: {}", email, e);
            }
        }
        
        return ChannelResponse.builder()
            .successful(successful)
            .failed(failed)
            .build();
    }
}
```


### 5.3. SMS Adapter Implementation

```java
@Component("smsAdapter")
public class TwilioSmsAdapter implements ChannelAdapter {
    
    @Override
    public ChannelResponse sendNotification(TenantConfiguration config, 
            ProcessedTemplate template, List<String> recipients) {
        
        try {
            // Initialize Twilio with tenant credentials
            JSONObject configJson = new JSONObject(config.getConfigurationJson());
            String accountSid = configJson.getString("accountSid");
            String authToken = encryptionService.decrypt(config.getApiKeyEncrypted());
            String fromNumber = configJson.getString("fromNumber");
            
            Twilio.init(accountSid, authToken);
            
            List<String> successful = new ArrayList<>();
            List<String> failed = new ArrayList<>();
            
            for (String phoneNumber : recipients) {
                try {
                    Message message = Message.creator(
                        new PhoneNumber(phoneNumber),
                        new PhoneNumber(fromNumber),
                        template.getBody()
                    ).create();
                    
                    successful.add(phoneNumber);
                    
                } catch (Exception e) {
                    failed.add(phoneNumber);
                    log.error("Failed to send SMS to: {}", phoneNumber, e);
                }
            }
            
            return ChannelResponse.builder()
                .successful(successful)
                .failed(failed)
                .build();
                
        } catch (Exception e) {
            return ChannelResponse.error("SMS send failed: " + e.getMessage());
        }
    }
}
```


***

## 6. Configuration Management API

### 6.1. Tenant Configuration Endpoints

```java
@RestController
@RequestMapping("/api/v1/config")
@Validated
public class ConfigurationController {
    
    @Autowired
    private TenantConfigurationService configService;
    
    @PostMapping("/{tenantId}/channels")
    public ResponseEntity<ConfigurationResponse> configureChannel(
            @PathVariable String tenantId,
            @RequestBody @Valid ChannelConfigRequest request) {
        
        ConfigurationResponse response = configService.configureChannel(tenantId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{tenantId}/channels")
    public ResponseEntity<List<ChannelConfiguration>> getChannelConfigurations(
            @PathVariable String tenantId) {
        
        List<ChannelConfiguration> configs = configService.getChannelConfigurations(tenantId);
        return ResponseEntity.ok(configs);
    }
    
    @PostMapping("/{tenantId}/templates")
    public ResponseEntity<TemplateResponse> saveTemplate(
            @PathVariable String tenantId,
            @RequestBody @Valid TemplateRequest request) {
        
        TemplateResponse response = configService.saveTemplate(tenantId, request);
        return ResponseEntity.ok(response);
    }
}
```


### 6.2. Channel Configuration Request Models

#### Firebase Configuration Request

```java
@Data
@AllArgsConstructor
public class FirebaseConfigRequest implements ChannelConfigRequest {
    @NotNull
    private ChannelType channelType = ChannelType.PUSH_NOTIFICATION;
    
    @NotBlank
    private String provider = "firebase";
    
    @NotBlank
    private String serviceAccountJson; // Will be encrypted before storage
    
    private String projectId;
    private boolean enableAnalytics = true;
}
```


#### Email Configuration Request

```java
@Data
@AllArgsConstructor
public class EmailConfigRequest implements ChannelConfigRequest {
    @NotNull
    private ChannelType channelType = ChannelType.EMAIL;
    
    @NotBlank
    private String provider; // "sendgrid", "ses", "smtp"
    
    @Email
    private String fromEmail;
    
    private String fromName;
    
    @NotBlank
    private String apiKey; // Will be encrypted
    
    // SMTP specific fields (optional)
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
}
```


***

## 7. Template Management System

### 7.1. Template Creation and Variables

#### Template Request Model

```java
@Data
public class TemplateRequest {
    @NotBlank
    private String templateName;
    
    @NotNull
    private ChannelType channelType;
    
    private String subjectTemplate; // For email
    
    @NotBlank
    private String bodyTemplate;
    
    private List<TemplateVariable> expectedVariables;
    
    private String languageCode = "en";
    
    @Data
    public static class TemplateVariable {
        private String name;
        private String type; // STRING, NUMBER, BOOLEAN, DATE
        private boolean required;
        private String defaultValue;
        private String description;
    }
}
```


#### Template Examples

**Welcome Email Template (HTML with FreeMarker)**

```html
<!DOCTYPE html>
<html>
<head>
    <title>Welcome to ${companyName}</title>
</head>
<body>
    <h1>Welcome, ${firstName}!</h1>
    <p>Thank you for joining ${companyName}. Your ${planName} plan is now active.</p>
    
    <#if activationLink??>
        <p><a href="${activationLink}">Activate your account</a></p>
    </#if>
    
    <p>Best regards,<br>The ${companyName} Team</p>
</body>
</html>
```

**SMS Template**

```text
Hi ${firstName}! Your OTP for ${serviceName} is ${otpCode}. Valid for ${expiryMinutes} minutes. Don't share this code.
```

**Push Notification Template (JSON structure)**

```json
{
    "title": "Order Update",
    "body": "Hi ${customerName}, your order ${orderNumber} is ${status}!",
    "data": {
        "orderId": "${orderId}",
        "trackingUrl": "${trackingUrl}"
    }
}
```


***

## 8. Security and Encryption

### 8.1. API Key Encryption Service

```java
@Service
public class EncryptionService {
    
    @Value("${app.encryption.key}")
    private String encryptionKey;
    
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(
                encryptionKey.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
            
        } catch (Exception e) {
            throw new SecurityException("Encryption failed", e);
        }
    }
    
    public String decrypt(String encryptedText) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedText);
            
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(
                encryptionKey.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
            
        } catch (Exception e) {
            throw new SecurityException("Decryption failed", e);
        }
    }
}
```


### 8.2. Authentication and Authorization

```java
@RestController
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class SecureNotificationController {
    
    @PostMapping("/notifications/send")
    @PreAuthorize("@tenantService.belongsToTenant(authentication.name, #request.tenantId)")
    public ResponseEntity<NotificationResponse> sendNotification(
            @RequestBody @Valid NotificationRequest request,
            Authentication authentication) {
        
        // Implementation
    }
}
```


***

## 9. Monitoring and Analytics

### 9.1. Metrics and Logging

```java
@Component
public class NotificationMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter sentCounter;
    private final Counter failedCounter;
    private final Timer processingTimer;
    
    public NotificationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.sentCounter = Counter.builder("notifications.sent")
            .description("Total notifications sent")
            .register(meterRegistry);
        this.failedCounter = Counter.builder("notifications.failed")
            .description("Total notifications failed")
            .register(meterRegistry);
        this.processingTimer = Timer.builder("notifications.processing.time")
            .description("Notification processing time")
            .register(meterRegistry);
    }
    
    public void recordSent(String tenantId, String channelType) {
        sentCounter.increment(
            Tags.of("tenant", tenantId, "channel", channelType)
        );
    }
    
    public void recordFailed(String tenantId, String channelType, String error) {
        failedCounter.increment(
            Tags.of("tenant", tenantId, "channel", channelType, "error", error)
        );
    }
    
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
}
```


### 9.2. Analytics API

```java
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    
    @GetMapping("/{tenantId}/delivery-stats")
    public ResponseEntity<DeliveryStats> getDeliveryStats(
            @PathVariable String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        DeliveryStats stats = analyticsService.getDeliveryStats(tenantId, from, to);
        return ResponseEntity.ok(stats);
    }
}
```


***

## 10. Configuration Properties

### 10.1. Application Configuration

```yaml
# application.yml
spring:
  application:
    name: notification-service
  
  datasource:
    url: jdbc:mysql://localhost:3306/notification_db
    username: ${DB_USERNAME:notification_user}
    password: ${DB_PASSWORD:password}
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    
  mail:
    host: ${SMTP_HOST:smtp.gmail.com}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            
  freemarker:
    template-loader-path: classpath:/templates/
    suffix: .ftl
    
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

app:
  encryption:
    key: ${ENCRYPTION_KEY:your-32-char-encryption-key}
  
  rate-limiting:
    enabled: true
    requests-per-minute: 1000
    
  retry:
    max-attempts: 3
    delay-seconds: 5

notification:
  channels:
    email:
      enabled: true
      default-from: noreply@yourservice.com
    sms:
      enabled: true
      providers:
        - twilio
        - aws-sns
    push:
      enabled: true
      providers:
        - firebase

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: always
```


***

## 11. Docker Configuration

### 11.1. Dockerfile

```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app

COPY target/notification-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```


### 11.2. Docker Compose for Development

```yaml
version: '3.8'
services:
  notification-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=mysql
      - RABBITMQ_HOST=rabbitmq
    depends_on:
      - mysql
      - rabbitmq
      
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: notification_db
      MYSQL_USER: notification_user
      MYSQL_PASSWORD: password
      MYSQL_ROOT_PASSWORD: rootpassword
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      
  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest

volumes:
  mysql_data:
```


***

## 12. Testing Strategy

### 12.1. Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    
    @Mock
    private TenantConfigurationService configService;
    
    @Mock
    private TemplateService templateService;
    
    @Mock
    private Map<String, ChannelAdapter> channelAdapters;
    
    @Mock
    private ChannelAdapter emailAdapter;
    
    @InjectMocks
    private NotificationServiceImpl notificationService;
    
    @Test
    void shouldSendEmailNotificationSuccessfully() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .tenantId("test-tenant")
            .channelType(ChannelType.EMAIL)
            .templateName("welcome")
            .recipients(List.of("test@example.com"))
            .variables(Map.of("name", "John"))
            .build();
            
        TenantConfiguration config = new TenantConfiguration();
        ProcessedTemplate template = ProcessedTemplate.builder()
            .subject("Welcome")
            .body("Hello John")
            .build();
        ChannelResponse channelResponse = ChannelResponse.success(List.of("test@example.com"));
        
        when(configService.getConfiguration("test-tenant", ChannelType.EMAIL))
            .thenReturn(config);
        when(templateService.processTemplate(any(), any(), any(), any()))
            .thenReturn(template);
        when(channelAdapters.get("emailAdapter"))
            .thenReturn(emailAdapter);
        when(emailAdapter.sendNotification(config, template, request.getRecipients()))
            .thenReturn(channelResponse);
        
        // When
        NotificationResponse response = notificationService.sendNotification(request);
        
        // Then
        assertThat(response.isSuccess()).isTrue();
        verify(emailAdapter).sendNotification(config, template, request.getRecipients());
    }
}
```


***

## 13. Deployment Architecture

### 13.1. Microservices Deployment Diagram

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Load Balancer │────│   API Gateway    │────│  Auth Service   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                               │
                       ┌───────┴────────────────────┐
                       │                            │
              ┌─────────────────┐           ┌──────────────────┐
              │ Notification    │           │ Config Service   │
              │    Service      │           │                  │
              └─────────────────┘           └──────────────────┘
                       │                            │
           ┌───────────┼───────────┐                │
           │           │           │                │
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ │
    │  Email      │ │   SMS       │ │   Push      │ │
    │  Adapter    │ │  Adapter    │ │  Adapter    │ │
    └─────────────┘ └─────────────┘ └─────────────┘ │
                                                    │
    ┌──────────────────────────────────────────────┼──┐
    │                Database Layer                │  │
    │                                              │  │
    │  ┌─────────────┐  ┌──────────────────────┐  │  │
    │  │   MySQL     │  │      Redis           │  │  │
    │  │ (Config &   │  │   (Caching &         │  │  │
    │  │  Logs)      │  │   Rate Limiting)     │  │  │
    │  └─────────────┘  └──────────────────────┘  │  │
    └─────────────────────────────────────────────┼──┘
                                                  │
    ┌─────────────────────────────────────────────┼──┐
    │              Message Queue                  │  │
    │  ┌─────────────────────────────────────────┐│  │
    │  │           RabbitMQ/Kafka                ││  │
    │  └─────────────────────────────────────────┘│  │
    └──────────────────────────────────────────────┘
```


***

## 14. Performance and Scalability

### 14.1. Performance Optimizations

| Optimization | Implementation | Expected Impact |
| :-- | :-- | :-- |
| Connection Pooling | HikariCP for database connections | 30% faster DB queries |
| Redis Caching | Template and config caching | 50% reduction in template processing |
| Async Processing | @Async methods for non-blocking operations | 70% improved throughput |
| Batch Processing | Bulk notification sending | 40% reduced API calls |
| Rate Limiting | Redis-based sliding window | Prevents system overload |

### 14.2. Scaling Strategy

- **Horizontal Scaling:** Multiple service instances behind load balancer
- **Database Sharding:** Tenant-based data partitioning
- **Message Queue Scaling:** RabbitMQ clustering for high availability
- **Cache Distribution:** Redis cluster for shared caching
- **CDN Integration:** Static template assets served via CDN

***

## 15. Summary

This notification service provides a comprehensive, multi-tenant solution for modern applications requiring flexible notification delivery. The architecture ensures scalability, security, and maintainability while offering customers complete control over their notification templates and provider configurations.

### Key Benefits

| Benefit | Description |
| :-- | :-- |
| **Multi-Tenant Isolation** | Secure tenant data separation with encrypted API keys |
| **Channel Flexibility** | Support for SMS, email, and push notifications|
| **Template Power** | Dynamic message generation with FreeMarker/Thymeleaf |
| **Self-Service Portal** | Customer dashboard for configuration management |
| **Provider Agnostic** | Easy integration with multiple service providers|
| **Analytics Ready** | Built-in metrics and delivery tracking |
| **Production Ready** | Comprehensive error handling, logging, and monitoring |

The service can be deployed as a standalone microservice or integrated into existing Spring Boot applications, providing a robust foundation for enterprise-grade notification requirements.

