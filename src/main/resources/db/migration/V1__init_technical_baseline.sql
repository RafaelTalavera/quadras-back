CREATE TABLE IF NOT EXISTS app_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_key VARCHAR(100) NOT NULL UNIQUE,
    property_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO app_metadata (property_key, property_value)
VALUES ('system.baseline', 'hito2')
ON DUPLICATE KEY UPDATE property_value = VALUES(property_value);
