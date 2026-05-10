CREATE TABLE subscription_outbox_messages
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    subscription_id   BIGINT       NOT NULL,
    event_type        VARCHAR(100) NOT NULL,
    payload           TEXT         NOT NULL,
    created_at        DATETIME(6)  NOT NULL,
    processed         TINYINT(1)   NOT NULL DEFAULT 0,

    -- Resilience fields
    retry_count       INT          NOT NULL DEFAULT 0
        COMMENT 'Number of failed publish attempts. Rows with retry_count >= 3 are dead-lettered.',
    last_attempt_time DATETIME(6)           DEFAULT NULL
        COMMENT 'Timestamp of the most recent publish attempt.',
    error_reason      TEXT                  DEFAULT NULL
        COMMENT 'Last exception message; populated on failure, stored permanently in dead_letter_outbox.',

    PRIMARY KEY (id),
    INDEX idx_sub_outbox_processed (processed),
    INDEX idx_sub_outbox_retry     (processed, retry_count)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
