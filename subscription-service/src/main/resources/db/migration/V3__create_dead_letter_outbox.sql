CREATE TABLE dead_letter_outbox
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    original_outbox_id  BIGINT       NOT NULL
        COMMENT 'ID of the original subscription_outbox_messages row, for traceability.',
    subscription_id     BIGINT       NOT NULL,
    event_type          VARCHAR(100) NOT NULL,
    payload             TEXT         NOT NULL,
    retry_count         INT          NOT NULL,
    last_attempt_time   DATETIME(6)           DEFAULT NULL,
    error_reason        TEXT                  DEFAULT NULL,
    dead_lettered_at    DATETIME(6)  NOT NULL
        COMMENT 'When this record was moved from the main outbox to the dead-letter table.',

    PRIMARY KEY (id),
    INDEX idx_dl_outbox_subscription_id (subscription_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
