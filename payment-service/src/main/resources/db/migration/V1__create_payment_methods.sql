CREATE TABLE payment_methods (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    gateway_token VARCHAR(255) NOT NULL,
    last_four     VARCHAR(4)   NOT NULL,
    brand         VARCHAR(20)  NOT NULL,
    is_default    TINYINT(1)   NOT NULL DEFAULT 0,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_pm_user_id (user_id),
    UNIQUE KEY uq_pm_gateway_token_user_id (gateway_token, user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
