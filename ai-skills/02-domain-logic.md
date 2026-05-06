# Domain Rules & Business Logic

## 1. Subscription States
Subscriptions must strictly transition between these states: `PENDING`, `ACTIVE`, `CANCELLED`, or `SUSPENDED`.
- When a new subscription is created, its state MUST be set to `PENDING`.
- The state should be updated to `ACTIVE` ONLY after a `PaymentCompletedEvent` (Success) is received.
- If a `PaymentFailedEvent` is received, the state must be updated to `CANCELLED`.

## 2. Auto-Renewal
- Monthly renewal processes will be executed via a scheduled daily job (using Spring `@Scheduled`).
- The job will identify `ACTIVE` subscriptions nearing their expiration date and dispatch a new payment request event to the Payment Service.
- If the renewal process did not succeed, then the subscription MUST be set to `SUSPENDED`

## 3. Cancellation
- When a user cancels an active subscription, its status is set to `CANCELLED`.
- Cancelled subscriptions must be permanently excluded from all future auto-renewal cycles. No further charges should be attempted.