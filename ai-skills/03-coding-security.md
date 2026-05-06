# Coding Standards & Security

## 1. Exception Handling
- Do not scatter `try-catch` blocks throughout the business logic.
- Create a global `@RestControllerAdvice` to handle all application exceptions (e.g., `BusinessException`, `NotFoundException`) centrally.
- Error responses returned to the client must follow a standardized JSON format (e.g., an `ApiError` wrapper object).

## 2. Logging
- Implement comprehensive logging across all critical business flows using SLF4J (Lombok `@Slf4j`).
- STRICTLY mask or exclude PII (Personally Identifiable Information) such as credit card numbers, CVVs, or plain-text passwords from all application logs.
- Logs must include a `correlationId` (using MDC - Mapped Diagnostic Context) to trace a single request across multiple asynchronous services.

## 3. Data Validation
- Validate all incoming HTTP request payloads at the Controller level using `@Valid` and Jakarta Validation annotations (e.g., `@NotNull`, `@Size`, `@Email`).