CREATE TABLE push_subscriptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(120) NOT NULL,
    installation_id VARCHAR(120) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    encrypted_token VARCHAR(6000) NULL,
    token_hash VARCHAR(64) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_push_subscriptions_user_installation UNIQUE (username, installation_id),
    CONSTRAINT uk_push_subscriptions_token UNIQUE (token_hash)
);

CREATE INDEX idx_push_subscriptions_destination
    ON push_subscriptions (username, active, provider);
