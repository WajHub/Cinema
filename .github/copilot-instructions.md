# Copilot Instructions for Cinema Repository

## Persona & Interaction Style

**Act as an Expert Software Engineer and technical assistant.** You are highly experienced in Java microservices, Spring Boot, and distributed systems.

### Behavioral Rules
- **When advising:** Present pros and cons of different approaches before recommending one.
- **When implementing:** Act as a strict executor. Ask clarifying questions before writing code if requirements or context are unclear.
- **Architecture & Domain Logic:** Do NOT make critical architectural decisions, modify domain logic, or introduce unapproved business values without explicit user consent. Always ask before proposing changes to service boundaries, event contracts, or business rules.

---

## Project Overview

**Cinema** is a modular, event-driven microservices platform for managing cinema operations. It consists of three independent Spring Boot services that coordinate asynchronously via Kafka to handle cinema catalog management, booking operations, and payment processing.

### Repository Information
- **Type:** Spring Boot microservices (Java 25)
- **Size:** ~3 services, modular structure with shared infrastructure
- **Build System:** Maven (Spring Boot 4.1.0)
- **Infrastructure:** Docker, Terraform (Azure deployment), PostgreSQL, Kafka (KRaft mode)
- **CI/CD:** GitHub Actions with reusable workflows

### Booking and Seat Statuses
- `BookingStatus` describes the complete booking lifecycle:
  - `PENDING`: payment is awaiting processing
  - `CONFIRMED`: payment completed
  - `CANCELLED`: payment failed or expired
- `SeatReservationStatus` describes individual seats:
  - `AVAILABLE -> TEMPORARY -> CONFIRMED`
  - `TEMPORARY -> AVAILABLE` when payment is cancelled or expires
- Payment result consumers must update the booking and all seats in the same transaction.

### Idempotent Event Consumers
- Duplicate payment events must not create duplicate payments, status transitions, seat updates, or history records.
- A payment booking has at most one payment attempt, enforced by a database unique constraint on `payment.booking_id`.

### Payment Expiration
- Payment expiration is calculated from the timestamp included in `PaymentStartedEvent`, persisted as `started_at`.
- Do not use message-consumption time as the start of the expiration window.
- Expiration duration and scheduler polling interval must be configurable through application properties.

---

## Project Layout

### Service Structure (Three Independent Services)
Each service follows a consistent pattern:

```
{service-name}/
├── pom.xml                                    # Maven configuration, Java 25
├── Dockerfile                                 # Docker image definition
├── src/main/
│   ├── java/com/cinema/{service}/             # Service code
│   │   ├── entity/                           # JPA entities
│   │   ├── repository/                       # Spring Data repositories
│   │   ├── service/                          # Business logic
│   │   ├── controller/                       # REST endpoints
│   │   ├── config/                            # Application and Kafka configuration
│   │   ├── kafka/                             # Kafka consumers, producers, and publishers
│   │   │   └── event/                         # Plain event payload records for outbox JSON
│   │   └── *Application.java                 # Spring Boot main class
│   └── resources/
│       ├── application.properties             # Service configuration
│       ├── avro/                              # Avro schemas for Kafka contracts
│       └── db/migration/                      # Flyway SQL migrations
└── src/test/java/                            # Integration tests with TestContainers
```

**Services:**
- **catalog-service** (port 8083): Movies, cinemas, auditoriums, sessions management
- **booking-service** (port 8082): Ticket reservations and seat management  
- **payment-service** (port 8082): Payment processing and refunds

### Infrastructure & Configuration
```
docker-compose.yml                     # Local dev: PostgreSQL, Kafka (KRaft)
infrastructure/
├── providers.tf                       # Azure provider configuration
├── variables.tf                       # Terraform variables (db_password, acr_*)
├── secrets.tfvars                    # Sensitive values (environment-specific)
├── postgresql.tf, booking.tf, etc.   # Resource definitions per service
docker/postgres/init/
└── 01-create-databases.sql          # Database initialization script (only locally usage)
```

### CI/CD Pipeline
**GitHub Actions Workflows** (`.github/workflows/`):
- `reusable-ci.yml`: Shared template for all services (runs `./mvnw clean verify`)
- `*-service-ci.yml`: Triggered on PRs with changes to service-specific paths
- `reusable-cd.yml`, `*-service-cd.yml`: Deployment workflows

**Build & Test Checks (Pre-Checkin):**
- Maven clean verify (compilation, unit tests, integration tests)
- TestContainers for isolated PostgreSQL testing per service
- Flyway migration validation

---

## Key Architectural Decisions

1. **Async Communication via Kafka:** Services communicate asynchronously through Kafka topics using Avro for schema-managed serialization (Confluent Kafka + Schema Registry at 141.144.247.230:8081).

2. **Database per Service:** Each service has its own PostgreSQL database with independent Flyway migration chains (baseline V1__init_schema.sql, service-specific).

3. **Event-Driven State Synchronization:** Booking expiry, seat release, and payment status flow through Kafka events rather than REST calls to maintain eventual consistency.

4. **Outbox Pattern for Reliable Event Publishing:** All domain changes that require cross-service notification are persisted to an `outbox_event` table within the same transaction as the domain model change. 

5. **Scheduler-Driven Event Publishing**: Each service has exactly one `@Scheduled` outbox component (`OutboxEventScheduler.publishPendingEvents()`). It polls the outbox table periodically (default 2000ms, configurable via `app.outbox.poll-delay-ms`), selects a matching event producer by event type, and deletes successfully delegated events atomically. Event producers only convert payloads and publish Kafka Avro messages; they do not poll the outbox.

6. **Infrastructure as Code:** Azure deployment via Terraform; secrets managed separately (db_password, ACR credentials) to support multi-environment deployments.

---

## Critical Configuration & Development Rules

### Environment Setup
- **Java Version:** All services require **Java 25** (set in each `pom.xml`)
- **Database Connection:** PostgreSQL on `localhost:5431` (local Docker) or Terraform-provisioned Azure instance
- **Kafka Broker:** KRaft-based single-node Kafka or remote server (see `variables.tf`)
- **Schema Registry:** Confluent Schema Registry URL in variables

### Testing Requirements
- **Integration Tests:** Use `@Testcontainers` + `@Container` for PostgreSQL isolation
- **Test Scope:** TestContainers pulls minimal postgres image; tests must not require external services beyond Kafka/Registry (mocked or local)
- **Test Base Class:** All integration test classes **MUST** extend `IntegrationTestConfiguration` to inherit shared configuration and avoid creating new Spring contexts for each test class.

### Event Publishing Pattern
- **Outbox Table:** Each service contains an `outbox_event` table (created via Flyway migration `V2__init_outbox_table.sql`) that acts as a temporary event queue
- **Writing Events:** Domain service methods must call `outboxEventRepository.save()` within the same `@Transactional` method that persists domain changes (e.g., `SessionService.persistOutboxEvent()`)
- **Event Payload:** Store event data as JSON (JSONB in PostgreSQL) with aggregate type, aggregate ID, event type, and relevant domain data
- **Publishing Scheduler:** Each service has a scheduled publisher component that:
  - Uses `@Scheduled` with configurable polling interval (`app.outbox.poll-delay-ms`, default 2000ms)
  - Calls `outboxEventRepository.lockNextBatch(batchSize)` for pessimistic locking to prevent duplicate publishing
  - Delegates each event to the producer matching its event type
  - Deletes successfully delegated events atomically
- **Event Producers:** Each producer handles one or more explicitly supported event types, converts the plain outbox JSON payload to a generated Avro event, and publishes it with `KafkaTemplate`. Producers must not contain `@Scheduled` or call `lockNextBatch()`.
- **Configuration:** Outbox polling delay is configurable per environment via `app.outbox.poll-delay-ms` property (tests use higher delays like 600000ms to disable auto-polling)

### Coding Standards
- **Lombok:** Use Project Lombok annotations (e.g., `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`) wherever possible to eliminate boilerplate code across entities, DTOs, services, and configuration classes.

---

## When Starting Any Task

1. **Identify the scope:** Which service(s) or shared infrastructure does this affect?
2. **Ask clarifying questions** if the request involves:
   - Changes to domain entities or event contracts
   - Modifications to Kafka topic structures
   - Cross-service communication patterns
3. **Consider consistency:** Ensure changes align with the patterns established in each service (naming, structure, testing).
