# 📣 Scalable Notification Service - Architecture & Functional Documentation

## 📌 Overview

This document outlines the architecture and functional design of a scalable, reusable notification service. It supports push notifications (FCM), email (via SES), and SMS (via Twilio), and is built to serve multiple independent applications with varying user bases and delivery preferences.

---

## 🧩 Functional Requirements

* Support **three channels**: Push (FCM)(ACN), Email (SES), SMS (Twilio).
* Allow **multi-project usage** with isolated configurations.
* Expose **REST APIs** to:

    * Send notification via one or more channels.
    * Schedule notifications.
    * Store templates and use them with dynamic placeholders.
    * Track delivery status and logs.
* Ensure **retries** and **dead letter queue** for failed deliveries.
* Allow **multilingual** messaging.
* Provide **admin dashboard** for tracking, testing, and managing campaigns (future scope).

---

## 🏗️ System Architecture

### 🧱 Components

1. **Notification API Gateway**

    * Accepts requests to trigger/send/schedule notifications.
    * Authenticates and validates input.

2. **Notification Processor (Worker)**

    * Reads from a message queue (e.g., RabbitMQ, AWS SQS).
    * Fetches template, resolves content, and routes to appropriate channel handler.

3. **Channel Handlers**

    * **Push Handler** → Firebase Cloud Messaging (FCM)
    * **Email Handler** → AWS SES
    * **SMS Handler** → Twilio or other providers
    * Each handler implements retry logic and logs response metadata.

4. **Template Engine**

    * Stores templates with placeholders.
    * Resolves dynamic values at runtime.

5. **Audit & Status Service**

    * Tracks notification status per user and channel.
    * Provides APIs to fetch delivery logs.

6. **Configuration Manager**

    * Stores project-specific API keys, rate limits, preferences.

7. **Scheduler**

    * Supports future-dated notifications.

8. **Retry & DLQ Handler**

    * Retries failed notifications (exponential backoff).
    * Routes permanently failed items to DLQ for investigation.

---

## 🧮 Tech Stack

| Component            | Tech Used                       |
| -------------------- | ------------------------------- |
| API Layer            | Spring Boot + REST              |
| Queue                | RabbitMQ / AWS SQS              |
| Database             | PostgreSQL / MongoDB            |
| Template Engine      | Apache FreeMarker               |
| Push Notification    | Firebase FCM                    |
| Email                | AWS SES                         |
| SMS                  | Twilio / SMS Gateway            |
| Logging & Monitoring | OpenTelemetry + Grafana         |
| Retry / DLQ          | Spring Retry + DeadLetter Queue |
| Deployment           | Docker + Terraform + EC2        |

---

## 🧑‍💻 API Endpoints (Sample)

### POST `/api/v1/notifications/send`

Send immediate notification to a user via one or more channels.

```json
{
  "projectId": "p1-prod",
  "userId": "user-123",
  "channels": ["PUSH", "EMAIL"],
  "templateCode": "ORDER_CONFIRMATION",
  "placeholders": {
    "userName": "John",
    "orderId": "ORD123456"
  }
}
```

### POST `/api/v1/notifications/schedule`

Schedule notification for a future time.

### POST `/api/v1/templates`

Create or update a notification template.

### GET `/api/v1/status/{notificationId}`

Fetch delivery status for a specific notification.

---

## 🧾 Template Example

### Template Code: `ORDER_CONFIRMATION`

```html
Subject: Order Confirmation - #{orderId}

Hi #{userName},

Thank you for your order #{orderId}. We are processing it and will notify you once it's out for delivery.

Thanks,  
Team Sipstr
```

---

## 🔐 Multi-Tenancy & Config Isolation

Each project will have:

* API keys for external services
* Project-specific templates
* Rate limits per channel
* Data isolation (notification logs, templates, configs)

All routing is governed by the `projectId`.

---

## 🛡️ Security & Authentication

* Each project gets a client token (API key)
* Role-based access for admin actions
* Token validation middleware in API Gateway

---

## 📉 Scalability Considerations

* Horizontal scaling of the processor workers.
* Channel handler thread pools based on message volume.
* Use of queues decouples producer and consumer.
* DB indexing on `projectId`, `userId`, `notificationId`.

---

## ✅ Future Enhancements

* In-app notifications with real-time sockets.
* Admin dashboard for notification tracking and testing.
* A/B testing framework for notification content.
* Batch & Segment targeting (e.g., notify all premium users).

---