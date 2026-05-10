-- V1__create_subscriptions.sql
-- Core subscription domain entity.
--
-- Uses a MySQL-compatible partial unique index via a VIRTUAL generated column
-- (active_user_id) to enforce at most one live subscription per user.

CREATE TABLE subscriptions
(
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    user_id              BIGINT      NOT NULL,
    status               VARCHAR(20) NOT NULL,
    start_date           DATETIME(6),
    end_date             DATETIME(6),
    cancel_at_period_end TINYINT(1)  NOT NULL DEFAULT 0
        COMMENT 'When true, subscription stays ACTIVE until endDate, then is reaped to CANCELLED by the nightly job.',
    created_at           DATETIME(6) NOT NULL,
    updated_at           DATETIME(6) NOT NULL,

    -- Generated column: non-NULL only while the subscription is "alive".
    -- MySQL never treats two NULLs as equal in a UNIQUE index, so this column
    -- acts as a MySQL-compatible partial unique index — equivalent to the
    -- PostgreSQL WHERE status IN ('PENDING', 'ACTIVE', 'SUSPENDED') clause.
    active_user_id       BIGINT AS (
        CASE WHEN status IN ('PENDING', 'ACTIVE', 'SUSPENDED') THEN user_id ELSE NULL END
    ) VIRTUAL,

    PRIMARY KEY (id),
    INDEX idx_subscriptions_status_end_date (status, end_date),

    -- Enforces: at most one live (PENDING/ACTIVE/SUSPENDED) subscription per user.
    UNIQUE INDEX idx_unique_active_subscription (active_user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
