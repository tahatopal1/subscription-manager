# Architecture & Distributed Systems Skills

## 1. Event-Driven & Async Communication
- Communication between the domain services (e.g., Subscription Service and Payment Service) MUST be strictly asynchronous (via a Message Broker like RabbitMQ).
- The Payment Service must emit an event indicating the final outcome of the payment (e.g., SUCCESS or FAILED).
- **Identity Events:** If a user account is permanently deleted (e.g., GDPR compliance), the Auth Service must publish a `UserDeletedEvent` so domain services can anonymize historical data.

## 2. Distributed Transactions (Saga & Outbox Pattern)
- Use a Choreography-based SAGA pattern for distributed transaction management.
- To guarantee Data Consistency, database write operations and event publishing must occur within the exact same database transaction. Implement the **Transactional Outbox Pattern** to achieve this.
- NEVER publish events directly to the message broker; always persist them to an Outbox table first.

## 3. Idempotency
- All requests sent to domain services (especially Payment requests) must be idempotent.
- Duplicate requests arriving with the same `paymentId` or `idempotencyKey` must not break the system state and should return the exact same result as the initial request.

## 4. Resilience
- Implement Retry mechanisms (Spring Retry) and Circuit Breakers (Resilience4j) for all HTTP/RPC calls to external APIs or mock third-party providers.

## 5. Identity & Access Management (IAM)
- **Single Source of Identity:** The `auth-service` is the exclusive owner of user credentials, password hashing, JWT generation, and role assignments (`ROLE_USER`, `ROLE_ADMIN`).
- **No User Entity Duplication:** Domain services (Subscription, Payment) MUST NOT contain a `User` entity or map to an identity table. They must only store the `userId` (String/UUID) as a foreign reference to associate business records with an identity.
- **Stateless Authorization:** Domain services MUST NOT validate passwords or cryptographic JWT signatures. They must rely exclusively on the `X-User-Id` and `X-User-Roles` HTTP headers injected securely by the API Gateway (Kong).
- **Lifecycle Separation:** Business operations (like cancelling a subscription) MUST NOT alter the Auth Service's identity record. Account deletion, role provisioning, and banning are exclusively Auth Service admin operations.