
# 📘 Notification Service - Developer Onboarding Document

## 1. Introduction

The goal of this project is to build a **scalable, multi-tenant Notification Service** capable of delivering **push notifications (Android/iOS), emails, and SMS messages**.
Key features:

- Multi-tenant support.
- Configurable notification channels per tenant.
- Customizable templates with placeholders (variables).
- API-driven message triggering.
- Secure, reliable, extensible architecture.

This document provides **end-to-end guidelines** for development, setup, coding practices, and deployment.

***

## 2. High-Level Architecture

### Components

1. **API Gateway / REST Layer** – Accepts incoming requests from client applications.
2. **Authentication \& Authorization Service** – Ensures tenant isolation and secure usage.
3. **Notification Orchestrator** (Application Service Layer) – Core logic for processing requests, filling templates, resolving tenant configs, and routing notifications.
4. **Channel Providers** – Adapter classes to deliver notifications:
    - Email Provider (e.g., SMTP, SES, SendGrid)
    - SMS Provider (e.g., Twilio)
    - Push Provider (Firebase, APNS)
5. **Template Service** – Stores and resolves templates with dynamic variables.
6. **Configuration Service** – Manages per-tenant settings for channels and credentials.
7. **Database** – Central persistent store for tenants, templates, notification logs.
8. **Queue / Message Broker** – (Optional but recommended, e.g., RabbitMQ, Kafka, SQS) for async delivery and retries.

### Suggested Architecture Pattern

- **Microservices + Event-driven (CQRS optional)**.
- **Clean Architecture / Hexagonal Architecture** – to keep channel providers pluggable.
- **Strategy Pattern** for choosing a notification channel.
- **Factory Pattern** for channel providers instantiation.
- **Observer/Event pattern** for handling notification events.

***

## 3. Tech Stack Recommendation

- **Backend Framework**: Node.js (NestJS/Express), or Java (Spring Boot), or .NET Core depending on team preference.
- **Database**: PostgreSQL / MySQL (relational, multi-tenant configs) + Redis (caching).
- **ORM**: TypeORM / Hibernate / EF Core.
- **Message Queue**: RabbitMQ / Kafka / AWS SQS.
- **Push Notifications**: Firebase Cloud Messaging (FCM) \& Apple Push Notification Service (APNS).
- **Email**: SMTP (Postfix/SES/SendGrid).
- **SMS**: Twilio/Nexmo/Plivo.
- **Authentication**: OAuth2.0 / JWT tokens.
- **CI/CD**: GitHub Actions / GitLab CI / Jenkins.
- **Containerization**: Docker + Kubernetes for scalability.

***

## 4. Database Design (Multi-tenant Aware)

**Tables:**

1. **Tenants**
    - `tenant_id (PK)`
    - `name`
    - `status`
    - `created_at`
2. **Configurations**
    - `config_id (PK)`
    - `tenant_id (FK)`
    - `channel_type` (EMAIL/SMS/PUSH)
    - `provider_name`
    - `credentials (JSON)`
    - `status`
3. **Templates**
    - `template_id (PK)`
    - `tenant_id (FK)`
    - `channel_type`
    - `name`
    - `subject` (for email)
    - `body` (with placeholders `{{variable}}`)
    - `is_active`
4. **Notification_Logs**
    - `log_id (PK)`
    - `tenant_id (FK)`
    - `channel_type`
    - `template_id (FK)`
    - `request_payload (JSON)`
    - `status` (SENT/FAILED/PENDING)
    - `timestamp`

***

## 5. Workflow

1. **Client/Customer System calls API** → Includes `tenant_id`, template identifier, variables, target recipient(s).
2. **Notification Service validates tenant auth** + fetches config.
3. **Template Service resolves template** by replacing `{{variables}}`.
4. **Notification Orchestrator selects appropriate provider** using strategy pattern.
5. **Message sent via channel provider** (e.g., Email via SMTP, SMS via Twilio).
6. **Logs written to DB** for tracking and retries.
7. **Async Retry Mechanism** via Queue if delivery fails.

***

## 6. API Design (REST)

### Authentication

- **Bearer token (JWT)** per tenant.
- API Gateway validates tokens.


### Endpoints

**Tenant Management**

- `POST /tenants` – create new tenant.
- `GET /tenants/{id}` – fetch tenant details.

**Configuration Management**

- `POST /tenants/{id}/configurations` – add/update channel config.
- `GET /tenants/{id}/configurations` – list configs.

**Template Management**

- `POST /tenants/{id}/templates` – create template.
- `GET /tenants/{id}/templates` – list templates.

**Notification Trigger**

- `POST /notifications/send`

```json
{
  "tenant_id": "abc123",
  "channel_type": "EMAIL",
  "template_name": "welcome_email",
  "recipients": ["user@example.com"],
  "variables": {
    "firstName": "John",
    "planName": "Premium"
  }
}
```


***

## 7. Design Patterns Used

- **Factory Pattern** → Instantiate providers (Email/SMS/Push).
- **Strategy Pattern** → Decide delivery logic per channel.
- **Observer/Event Pattern** → Trigger notifications asynchronously.
- **Repository Pattern** → Clean database operations abstraction.

***

## 8. Quality, Security \& Compliance

- **Code:** Follow SOLID principles, Clean Architecture.
- **Testing:** Unit + Integration + End-to-End.
- **Security:** Encrypt credentials, TLS everywhere, OWASP standards.
- **Observability:** Central logging (ELK/Prometheus + Grafana).
- **Scalability:** Auto-scale workers in K8s.
- **Compliance:** GDPR (opt-in/out logs), DND Handling for SMS.

***

## 9. Developer Workflow

1. **Clone Repository**

```bash
git clone <repo-url>
cd notification-service
```

2. **Setup Environment**
    - Install Docker \& docker-compose.
    - Create `.env` file (sample provided).
3. **Run Local Development**

```bash
docker-compose up
```

    - Starts API service + DB + Queue.
4. **Coding Guidelines**
    - Follow feature-branch model.
    - Commit messages: Conventional Commits.
    - PR reviews mandatory.
5. **Running Tests**

```bash
npm run test
```


***

## 10. Future Enhancements

- WebSocket in-app notifications.
- WhatsApp / Telegram integration.
- Template versioning \& approval workflows.
- Advanced analytics dashboards.

***

## 📂 Recommended Project Folder Structure

```
notification-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── notification/
│   │   │               ├── NotificationServiceApplication.java
│   │   │               ├── config/
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── SwaggerConfig.java
│   │   │               │   └── ...  
│   │   │               ├── controller/
│   │   │               │   ├── NotificationController.java
│   │   │               │   ├── TenantController.java
│   │   │               │   └── TemplateController.java
│   │   │               ├── service/
│   │   │               │   ├── NotificationService.java
│   │   │               │   ├── EmailNotificationService.java
│   │   │               │   ├── SmsNotificationService.java
│   │   │               │   ├── PushNotificationService.java
│   │   │               │   ├── TemplateService.java
│   │   │               │   └── ConfigService.java
│   │   │               ├── repository/
│   │   │               │   ├── TenantRepository.java
│   │   │               │   ├── TemplateRepository.java
│   │   │               │   ├── ConfigRepository.java
│   │   │               │   └── NotificationLogRepository.java
│   │   │               ├── model/
│   │   │               │   ├── Tenant.java
│   │   │               │   ├── Template.java
│   │   │               │   ├── Configuration.java
│   │   │               │   ├── NotificationLog.java
│   │   │               │   └── enums/
│   │   │               │       ├── ChannelType.java
│   │   │               │       └── NotificationStatus.java
│   │   │               ├── dto/
│   │   │               │   ├── NotificationRequest.java
│   │   │               │   ├── NotificationResponse.java
│   │   │               │   ├── TenantDto.java
│   │   │               │   ├── TemplateDto.java
│   │   │               │   └── ConfigurationDto.java
│   │   │               ├── exception/
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   └── CustomException.java
│   │   │               ├── util/
│   │   │               │   ├── TemplateParser.java
│   │   │               │   └── JwtUtil.java
│   │   │               ├── event/
│   │   │               │   ├── NotificationEvent.java
│   │   │               │   └── NotificationEventListener.java
│   │   │               └── provider/
│   │   │                   ├── NotificationProvider.java         // Interface
│   │   │                   ├── EmailProviderImpl.java
│   │   │                   ├── SmsProviderImpl.java
│   │   │                   └── PushProviderImpl.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── logback-spring.xml
│   └── test/
│       └── java/com/example/notification/
│           └── ... (Test classes mirroring main structure)
└── pom.xml
```


***

## 🏗️ Structure Explanation \& Best Practices

**1. Layered Clean Architecture**

- Controllers (API layer): Expose endpoints, delegate logic.
- Services: Business logic and orchestration.
- Providers (Strategy/Factory): Adapters for Email/SMS/Push (Strategy pattern).
- Repositories: Interact with database (Spring Data JPA).
- Event package: For async/event-driven notification send and retry.
- Config: All application and security configurations.
- DTOs: Strictly for external/internal API schema separation.

**2. Template Handling**

- Use a `TemplateParser` utility for variable replacement in notification bodies using Java’s pattern matching.

**3. Multi-tenancy**

- All data models (Template, Config, Log) reference `tenantId`.
- Tenant authentication by JWT and validation via `SecurityConfig`.

**4. Asynchronous Notifications**

- Use events or Spring @Async.
- Consider integrating RabbitMQ/Kafka for queue-based delivery.

**5. Pluggable Providers**

- Use a `NotificationProvider` interface and respective impls (email/SMS/push).
- Provider selection via Factory or @Qualifier Spring Beans.

**6. Logging \& Monitoring**

- Use `NotificationLog` entity for auditing.
- Slf4j/Logback configuration in `logback-spring.xml`.

**7. Test Coverage**

- All packages mirrored in `/test/java/...` with unit and integration tests using JUnit and Mockito.

***

## ⚡️ Quick Project Bootstrap

### Initialize the Spring Boot project (if not already done):

You can use [Spring Initializr](https://start.spring.io/) with:

- Dependencies: Spring Web, Spring Security, Spring Data JPA, Spring Boot Starter Mail, Lombok, Validation, PostgreSQL/MySQL, Spring Cloud (if using JWT/OAuth2), Actuator.


### Example: Main Class

```java
@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
```


### Example: Notification Send Endpoint (Controller)

```java
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(
            @RequestBody @Valid NotificationRequest request
    ) {
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }
}
```


### Example: NotificationProvider Interface

```java
public interface NotificationProvider {
    NotificationStatus sendNotification(NotificationRequest request, String parsedContent, Configuration channelConfig);
}
```


***

## 🔗 Next Steps For Developers

1. **Clone the skeleton**: Create the above folder structure.
2. **Set up DB**: Create schemas per the design doc.
3. **Configure application.yml**: Add DB, mail, SMS, and push settings.
4. **Implement repositories \& entities**.
5. **Develop providers and business logic**.
6. **Write tests as you build**.
7. **Use Docker for local stack orchestration (DB, brokers, etc.)**.

***

## 📄 Summary Table

| Layer | Folder/Package | Pattern/Responsibility | Example Classes |
| :-- | :-- | :-- | :-- |
| API Controller | controller/ | REST API, Request Validations | NotificationController |
| Service | service/ | Core Logic, Orchestration | NotificationService, TemplateService |
| Persistence | repository/ | CRUD, DB Access | TenantRepository, TemplateRepository |
| Domain Model | model/ | JPA Entities, Enums | Tenant, Template, ChannelType |
| Adapter | provider/ | Communication via Provider interface | EmailProviderImpl, SmsProviderImpl |
| Event | event/ | Async/Event Pattern | NotificationEvent, ... |
| Config | config/ | App/Security config | SecurityConfig, SwaggerConfig |
| Utilities | util/ | Helpers, Parsers, JWT | TemplateParser, JwtUtil |
| Exception | exception/ | Centralized error handling | GlobalExceptionHandler |


***
