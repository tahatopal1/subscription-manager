# Context: Data Layer & Flyway Migrations (MySQL)
This document defines the strict constraints for database interactions, entity modeling, and schema migrations using Spring Data JPA, MySQL 8+, and Flyway.

## 1. Schema Management (Flyway)
**Rule: Flyway is the single source of truth for the database schema.**
* **Hibernate Auto-DDL is strictly forbidden.** `spring.jpa.hibernate.ddl-auto` MUST be set to `validate` (or `none`). Never use `update` or `create-drop`.
* **Migration Naming Convention:** All migration scripts must reside in `src/main/resources/db/migration` and follow the exact format: `V<Version>__<Description>.sql`.
* **MySQL Native Features:** Leverage MySQL 8+ features in migrations. Use the native `JSON` data type for Outbox event payloads. For UUIDs, use `VARCHAR(36)` (or `BINARY(16)` with `UUID_TO_BIN()` if optimizing for massive scale).

## 2. JPA Entity Modeling Guidelines
When generating or modifying `@Entity` classes, adhere to these constraints:

* **Primary Key Generation:** MySQL relies on auto-incrementing columns. Always use `@GeneratedValue(strategy = GenerationType.IDENTITY)`. Do NOT use `GenerationType.SEQUENCE` (which is for Postgres/Oracle).
* **No `@Data` for Entities:** Never use Lombok's `@Data` or `@EqualsAndHashCode` on JPA entities. It causes infinite loops and severe performance issues with lazy-loaded collections. Instead, use:
    ```java
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    ```
* **Base Auditing:** All entities must extend a `@MappedSuperclass` BaseEntity containing:
    * `@CreatedDate private Instant createdAt;`
    * `@LastModifiedDate private Instant updatedAt;`
    * `@Version private Long version;` (For Optimistic Locking).
* **Lazy Loading Default:** All `@OneToMany` and `@ManyToOne` relationships MUST explicitly be set to `fetch = FetchType.LAZY`. 

## 3. Data Isolation & Security
* **Query-Level Tenant Filtering:** Controllers must never filter data in memory. All repository methods accessing user-owned data must enforce the `userId` in the SQL query.
* **Read-Only Transactions:** Service layers should be annotated with `@Transactional(readOnly = true)` at the class level.

## 4. Domain-Specific Business Rules
### A. Subscriptions (Soft Deletes)
* Never generate a physical `DELETE` operation (`repository.delete()`).
* Use Soft Deletes. Update the `status` to `CANCELLED` and populate an `endDate` timestamp. 

### B. Payments (Immutable Ledger)
* Payments are strictly an append-only ledger. No updates or deletes allowed once a payment is completed. Use Refund records to reverse transactions.

## 5. The Outbox Pattern Schema Constraints
When interacting with the `outbox_events` table:
* **Status Enum:** Must support `PENDING`, `PROCESSED`, and `FAILED` (mapped as `VARCHAR` in the database).
* **Failure Mitigation Columns:** Include `retry_count` (INT) and `error_message` (TEXT).
* **Payload Storage:** The serialized event data must be mapped to a MySQL `JSON` column.