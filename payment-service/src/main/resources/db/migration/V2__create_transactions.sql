CREATE TABLE transactions (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    user_id           BIGINT         NOT NULL,
    subscription_id   BIGINT,
    payment_method_id BIGINT,
    amount            DECIMAL(19, 2) NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    idempotency_key   VARCHAR(100)   NOT NULL,
    payment_reference VARCHAR(100),
    failure_reason    VARCHAR(255),
    created_at        DATETIME(6)    NOT NULL,
    updated_at        DATETIME(6)    NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_tx_idempotency_key (idempotency_key),
    INDEX idx_tx_user_id (user_id),
    INDEX idx_tx_subscription_id (subscription_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
