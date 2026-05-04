# Architecture & Distributed Systems Skills

## 1. Event-Driven & Async Communication
- Communication between the Subscription Service and Payment Service MUST be strictly asynchronous (via a Message Broker).
- The Payment Service must emit an event indicating the final outcome of the payment (e.g., SUCCESS or FAILED).

## 2. Distributed Transactions (Saga & Outbox Pattern)
- Use a Choreography-based SAGA pattern for distributed transaction management.
- To guarantee Data Consistency, database write operations and event publishing must occur within the exact same database transaction. Implement the **Transactional Outbox Pattern** to achieve this. 
- NEVER publish events directly to the message broker; always persist them to an Outbox table first.

## 3. Idempotency
- All payment requests sent to the Payment Service must be idempotent.
- Duplicate requests arriving with the same `paymentId` or `idempotencyKey` must not break the system state and should return the exact same result as the initial request.

## 4. Resilience
- Implement Retry mechanisms (Spring Retry) and Circuit Breakers (Resilience4j) for all HTTP/RPC calls to external services (e.g., the Mock Payment Provider).