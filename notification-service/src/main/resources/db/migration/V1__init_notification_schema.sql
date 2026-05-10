-- V1__init_notification_schema.sql
-- Notification Service — complete schema baseline

CREATE TABLE notifications (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    event_id         BIGINT       NULL,
    source           VARCHAR(50)  NOT NULL,
    channel          VARCHAR(100) NOT NULL,
    status           VARCHAR(10)  NOT NULL,
    message_template TEXT         NOT NULL,
    retry_count      INTEGER      NOT NULL DEFAULT 0,
    sent_at          DATETIME(6)  NULL,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT chk_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_notifications_status   ON notifications (status);
CREATE INDEX idx_notifications_event_id ON notifications (event_id);
CREATE INDEX idx_notifications_channel  ON notifications (channel);
