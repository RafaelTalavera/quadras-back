CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(40) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_app_users_role CHECK (role IN ('OPERATOR'))
);

INSERT INTO app_metadata (property_key, property_value)
VALUES ('security.users', 'hito12')
ON DUPLICATE KEY UPDATE property_value = VALUES(property_value);
