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

    active_user_id       BIGINT AS (
        CASE WHEN status IN ('PENDING', 'ACTIVE', 'SUSPENDED') THEN user_id ELSE NULL END
    ) VIRTUAL,

    PRIMARY KEY (id),
    INDEX idx_subscriptions_status_end_date (status, end_date),

    UNIQUE INDEX idx_unique_active_subscription (active_user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
