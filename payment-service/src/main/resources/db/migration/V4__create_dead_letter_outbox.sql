-- V4__create_dead_letter_outbox.sql
-- Dead-letter table for payment outbox rows that exhausted all retry attempts.
-- The scheduler inserts here and deletes from payment_outbox_messages,
-- keeping the hot outbox table lean and SKIP LOCKED queries fast.

CREATE TABLE dead_letter_outbox (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    original_outbox_id  BIGINT       NOT NULL
        COMMENT 'ID of the original payment_outbox_messages row, for traceability.',
    aggregate_id        BIGINT       NOT NULL,
    event_type          VARCHAR(100) NOT NULL,
    payload             TEXT         NOT NULL,
    retry_count         INT          NOT NULL,
    last_attempt_time   DATETIME(6)           DEFAULT NULL,
    error_reason        TEXT                  DEFAULT NULL,
    dead_lettered_at    DATETIME(6)  NOT NULL
        COMMENT 'When this record was moved from the main outbox to the dead-letter table.',

    PRIMARY KEY (id),
    INDEX idx_dl_payment_outbox_aggregate_id (aggregate_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
