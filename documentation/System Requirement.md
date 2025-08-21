# Detailed Requirement Document: Black Box for Ecommerce Portal (Web \& Mobile Apps)

## Executive Summary

This document outlines the comprehensive requirements and architecture for a generic "Black Box" solution for an ecommerce online selling platform. The solution will support onboarding of customers and vendors from any industry, leveraging microservices (Spring Boot), a cross-platform mobile front-end (React Native), and a hybrid database strategy. The platform will support user/customer/vendor authentication, store and inventory management, purchasing and payment flows, reporting, notifications, and delivery assignment—all modular, scalable, and industry-agnostic.

***

## 1. System Overview and Objectives

- **Goal:** Create a reusable, industry-agnostic ecommerce platform ("Black Box") for web portal and mobile apps.
- **Architecture:** Microservices-based, API-centric, deployable via container orchestration (e.g. Kubernetes/Docker).
- **Tech Stack:**
    - Backend: Spring Boot microservices
    - Frontend: React Native (mobile), React (web)
    - Database: Hybrid (mix of SQL/NoSQL as per service needs)
- **Key Principles:** Single Responsibility, Business-Centric Design, Decentralization, Resilience.[^1][^2]

***

## 2. User Roles and Functional Requirements

### 2.1. Customer

| Functionality | Description |
| :-- | :-- |
| User Authentication | OAuth/JWT-based login, registration, password management |
| Browse Inventory | Search/filter vendor products, view product details, real-time stock status |
| Shopping Cart | Add/remove items, adjust quantities |
| Order Placement | Create orders, choose payment options, apply coupons |
| Order Tracking | View live order status, delivery ETA, tracking updates |
| Order History | View previous orders, download invoices, reorder |
| Payment | Secure payment processing through integrated gateways (Stripe/PayPal/Razorpay) |
| Profile Management | Manage addresses, payment methods, preferences |

### 2.2. Vendor

| Functionality | Description |
| :-- | :-- |
| Vendor Authentication | Secure onboarding (KYC/identity validation), JWT/OAuth-based login |
| Store Management | Register/manage store, configure profile and branding |
| Inventory Management | Add/update products, stock levels, manage SKUs, upload rich content |
| Order Management | View/manage incoming orders, update order status, handle refunds/exchanges |
| Reporting | Access sales analytics, order reports, inventory trends |
| Assign Delivery Partners | Select/assign delivery partners (manual/automatic or via API integration) |
| Notification Management | Receive real-time updates (orders, inventory alerts, payments) via email/push/SMS |
| Promotions | Create/manage offers, discounts, special catalogs |

### 2.3. Admin (Multi-tenant Platform, Optional)

| Functionality | Description |
| :-- | :-- |
| Tenant Store Management | Onboard/manage stores from different industries, set configurations |
| Role-based Access | Define and manage user/vendor/admin permissions |
| System Monitoring | View traffic, errors, performance metrics, distributed tracing |
| Maintenance | Centralized updates, feature activation, scaling, security patches |


***

## 3. Microservices Architecture

### 3.1. Key Microservices and Responsibilities

| Microservice | Responsibility | DB Type |
| :-- | :-- | :-- |
| Auth Service | Authentication/Authorization (SSO, JWT, OAuth) | SQL/NoSQL |
| User Service | User profile, customer info | SQL |
| Vendor Service | Vendor, store, KYC info | SQL |
| Product Catalog | Product listing \& search | NoSQL |
| Inventory Service | Stock management, sync with Orders | SQL/NoSQL |
| Cart Service | Cart state management | NoSQL |
| Order Service | Order lifecycle: creation, update, tracking | SQL/NoSQL |
| Payment Service | Payment gateway, transaction log, invoice | SQL |
| Notification Service | Email/SMS/push notifications (triggered by events) | NoSQL |
| Delivery Assignment | Allocate delivery partners, track pickup/ETA | SQL |
| Tracking Service | Real-time delivery location, order status | NoSQL |
| Reporting/Analytics | Aggregate reports (sales, inventory trends) | NoSQL |
| API Gateway | Centralized entry point, routing, security | -- |
| Service Discovery | Dynamic service discovery (Eureka/Kubernetes) | -- |

#### Communication

- Services communicate via REST APIs or event-driven architecture (Kafka/RabbitMQ).
- Central API Gateway for external clients/routes and auth.

***

### 3.2. Multi-Tenant Architecture

- **Industry-Agnostic:** Each store/tenant is isolated with own inventory, branding, and custom features.
- **Shared Infrastructure:** Central resource management, updates, scaling.
- **Role \& Permission Management:** Fine-grained user/vendor/admin roles.
- **Customization:** Tenant-level theme, catalog, workflow configurations.
- **Hybrid Database:** Per-service DB (SQL for transactional, NoSQL for catalog/search), extensible for tenant isolation.

***

### 3.3. Security and Authentication

- **Auth Service:** Handles user/vendor authentication, supports SSO, JWT, OAuth2, OpenID Connect.
- **Role-based Access Control (RBAC):** Assigns permissions per role and microservice.
- **API Gateway:** Entry-point for all requests, validating tokens and routing to internal services.
- **Data Isolation:** Each microservice owns/accesses its own DB.
- **Audit \& Logging:** Centralized logging, distributed tracing (Spring Cloud Sleuth/Zipkin).

***

### 3.4. Order and Inventory Flow

#### Order Flow

1. Customer adds products to cart (Cart Service → Product Catalog/Inventory).
2. Order is placed (Order Service), checks inventory, creates order record.
3. Payment processed (Payment Service), order status updated.
4. Delivery partner assigned (Delivery Assignment Service), tracking activated.
5. Notifications sent (Notification Service), order history updated (Order Service).

#### Inventory Flow

- Real-time sync between Inventory Service and Order Service.
- Stock updates via admin interface (manual, bulk import) or warehouse systems (API/barcode/IoT integration).

***

### 3.5. Vendor Operations

- Store registration and KYC validation via Vendor Service.
- Inventory uploaded and managed via Inventory/Product Catalog microservices.
- Orders tracked, managed, fulfilled via Order and Delivery Assignment Services.
- Reports pulled from Reporting/Analytics.
- Notifications via Notification microservice.
- Assign deliveries manually or programmatically; integration with third-party logistics possible via API.

***

### 3.6. Notifications and Tracking

- **Notification Service:** Event-driven, supports email, push, SMS, configurable per user and tenant.
- **Tracking Service:** Real-time updates via REST/websockets. Delivery partner sends live location to Tracking Service, customer polls or receives push notifications.

***

### 3.7. Delivery Assignment

- **Assignment Service:** Matches orders to available delivery partners (manual/algorithms).
- **Fairness/Optimization:** Assignments consider ETA, rating, workload, location.
- **Tracking Integration:** Persistent connection with delivery partner; updates location/status to Tracking Service.

***

### 3.8. Reporting \& Analytics

- Real-time dashboards (sales, order trends, fulfillment efficiency).
- Vendor- and admin-level reports.
- Exportable CSVs/Excel.

***

## 4. Technology Stack Details

| Component | Technology Choices | Notes |
| :-- | :-- | :-- |
| API Gateway | Spring Cloud Gateway, Kong, Nginx | Auth, routing, rate limit |
| Auth | Spring Security, Keycloak | JWT/OAuth2, RBAC |
| Service Discovery | Netflix Eureka, Kubernetes | Dynamic service endpoints |
| Microservices Framework | Spring Boot (Java) | REST APIs |
| Mobile/Web Frontend | React Native (mobile), React (web) | Cross-platform UI |
| Database | Hybrid (MySQL, PostgreSQL, MongoDB, Elasticsearch) | Per-service DB |
| Messaging/Event | Kafka, RabbitMQ | Event-Driven Architecture |
| Distributed Tracing | Spring Cloud Sleuth, Zipkin | End-to-end request monitoring |
| Containerization | Docker, Kubernetes | Orchestrated deployment |


***

## 5. Industry-Agnostic, Extensible Features

- **Plug-in Support:** Custom modules for industry-specific requirements.
- **Flexible Catalog/Data Modeling:** Schema can be extended for different product types.
- **Configuration:** Feature toggling for business rules (e.g. payment options, inventory policies).
- **Integration:** Easy connections for external payment gateways, logistics, CRM, ERP via APIs.
- **Theming/Branding:** Tenant-level customization.

***

## 6. Example Microservice API Contracts (Simplified)

- **User Service:**
    - `POST /users/register`
    - `POST /users/login`
    - `GET /users/{id}/profile`
- **Vendor Service:**
    - `POST /vendors/register`
    - `PUT /vendors/{id}/profile`
- **Product Catalog:**
    - `GET /products`
    - `GET /products/{id}`
    - `POST /products` (vendor only)
- **Order Service:**
    - `POST /orders`
    - `GET /orders/{id}`
    - `PUT /orders/{id}/status`
- **Payment Service:**
    - `POST /payments`
    - `GET /payments/{id}/status`
- **Delivery Assignment:**
    - `POST /delivery/assign`
    - `GET /delivery/{id}/tracking`
- **Notification Service:**
    - `POST /notifications/send`
    - `GET /notifications/{userId}`

***

## 7. Observability \& Health

- Distributed tracing for order/payment flows.
- Health check endpoints (`/health`) for all services.
- Centralized monitoring and alert system.
- Audit logs and security event tracking.

***

## 8. Non-Functional Requirements

- **Scalability:** Horizontal scaling for spike management.
- **Security \& Compliance:** GDPR-ready, PCI DSS for payments.
- **Performance:** Fast response, optimized frontends.
- **Reliability:** Graceful failure, fallback, and robust retry logic.

***

## 9. Black Box Testing Model

- Functional input/output testing on APIs and user flows.
- Test scenarios: registration, login, inventory fetch, order placement, payment, delivery assignment, notifications, reporting.
- Automated regression suites, penetration/security testing.

***

## 10. Extending/Onboarding New Industries

- Industry onboarding via:
    - Tenant creation with custom configurations.
    - Schema extensions for domain-specific attributes.
    - Feature enablement per industry (e.g., loyalty, subscriptions, rental flows).
    - Onboarding utilities—import/export tools, demo data, documentation portal.

***

## 11. Deployment \& DevOps

- CI/CD workflow for all microservices.
- Automated container build, test, and orchestration.
- Modular deployment—spin up/down services per tenant/industry load.
- Central secrets/user credential vault.

***

## 12. Key Challenges \& Recommendation

- Centralize auth but keep business rules decentralized.
- Per-service DB for security, flexibility, and scaling.
- Real-time event bus for critical updates (order, payment, delivery).
- Observability with tracing, monitoring, logging—integrate early to support operational excellence.

***

## Summary Table: Microservices Inventory

| Service | Function | Scalability | DB Type | Extensibility |
| :-- | :-- | :-- | :-- | :-- |
| User/Auth | Login, registration, permissions | High | SQL/NoSQL | Yes |
| Product/Inventory | Catalog, search, stock | High | NoSQL | Yes |
| Order/Cart | Shopping cart, order lifecycle | High | SQL | Yes |
| Payment | Gateway integration, invoice | Medium | SQL | Yes |
| Delivery/Tracking | Assignment, ETA, live tracking | Medium | NoSQL | Yes |
| Notification | Email, SMS, push alerts | High | NoSQL | Yes |
| Reporting/Analytics | Dashboards, reports, trends | Medium | NoSQL | Yes |


***

