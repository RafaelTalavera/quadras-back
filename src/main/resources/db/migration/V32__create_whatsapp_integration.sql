ALTER TABLE massage_providers ADD COLUMN whatsapp_number VARCHAR(20) NULL;
ALTER TABLE massage_providers ADD COLUMN whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE maintenance_providers ADD COLUMN whatsapp_number VARCHAR(20) NULL;
ALTER TABLE maintenance_providers ADD COLUMN whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE whatsapp_settings (
    id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    dry_run BOOLEAN NOT NULL DEFAULT TRUE,
    monthly_budget_brl DECIMAL(12,4) NOT NULL DEFAULT 0,
    monthly_message_limit INT NOT NULL DEFAULT 100,
    daily_message_limit INT NOT NULL DEFAULT 20,
    per_provider_daily_limit INT NOT NULL DEFAULT 3,
    estimated_utility_cost_brl DECIMAL(12,4) NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_whatsapp_settings_singleton CHECK (id = 1),
    CONSTRAINT chk_whatsapp_settings_limits CHECK (
        monthly_budget_brl >= 0 AND monthly_message_limit >= 0
        AND daily_message_limit >= 0 AND per_provider_daily_limit >= 0
    )
);

INSERT INTO whatsapp_settings (
    id, enabled, dry_run, monthly_budget_brl, monthly_message_limit,
    daily_message_limit, per_provider_daily_limit, updated_at, updated_by
) VALUES (1, FALSE, TRUE, 0, 100, 20, 3, CURRENT_TIMESTAMP(6), 'system');

CREATE TABLE whatsapp_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    direction VARCHAR(12) NOT NULL,
    module_name VARCHAR(24) NOT NULL,
    aggregate_id BIGINT NULL,
    provider_id BIGINT NULL,
    recipient VARCHAR(20) NOT NULL,
    template_name VARCHAR(120) NULL,
    payload_json TEXT NULL,
    status VARCHAR(32) NOT NULL,
    idempotency_key VARCHAR(220) NOT NULL,
    meta_message_id VARCHAR(200) NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP(6) NULL,
    last_error VARCHAR(500) NULL,
    estimated_cost_brl DECIMAL(12,4) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    delivered_at TIMESTAMP(6) NULL,
    read_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_whatsapp_message_idempotency UNIQUE (idempotency_key),
    CONSTRAINT uk_whatsapp_message_meta_id UNIQUE (meta_message_id),
    INDEX idx_whatsapp_message_queue (status, next_attempt_at),
    INDEX idx_whatsapp_message_usage (created_at, direction),
    INDEX idx_whatsapp_message_aggregate (module_name, aggregate_id)
);

CREATE TABLE whatsapp_external_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    module_name VARCHAR(24) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    message_id BIGINT NULL,
    status VARCHAR(32) NOT NULL,
    resource_version VARCHAR(80) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    responded_at TIMESTAMP(6) NULL,
    proposed_start_at TIMESTAMP(6) NULL,
    proposed_end_at TIMESTAMP(6) NULL,
    proposal_notes VARCHAR(500) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_whatsapp_request_message FOREIGN KEY (message_id)
        REFERENCES whatsapp_messages (id),
    INDEX idx_whatsapp_request_aggregate (module_name, aggregate_id, created_at),
    INDEX idx_whatsapp_request_message (message_id)
);
