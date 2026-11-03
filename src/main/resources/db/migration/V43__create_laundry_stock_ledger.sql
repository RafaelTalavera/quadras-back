CREATE TABLE laundry_stock_balances (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    bucket VARCHAR(30) NOT NULL,
    provider_id BIGINT NULL,
    provider_scope BIGINT NOT NULL,
    quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lsb_item FOREIGN KEY (item_id) REFERENCES laundry_items(id),
    CONSTRAINT fk_lsb_provider FOREIGN KEY (provider_id) REFERENCES laundry_providers(id),
    CONSTRAINT chk_lsb_bucket CHECK (
        bucket IN (
            'HOTEL_AVAILABLE',
            'HOTEL_REWASH_QUEUE',
            'PROVIDER_PROCESSING',
            'PROVIDER_REWASH',
            'MISSING',
            'DAMAGED'
        )
    ),
    CONSTRAINT chk_lsb_scope CHECK (
        (bucket = 'HOTEL_AVAILABLE' AND provider_id IS NULL AND provider_scope = 0)
        OR (bucket <> 'HOTEL_AVAILABLE' AND provider_id IS NOT NULL AND provider_scope = provider_id)
    ),
    CONSTRAINT chk_lsb_quantity CHECK (quantity >= 0)
);

CREATE UNIQUE INDEX uk_lsb_scope ON laundry_stock_balances (item_id, bucket, provider_scope);
CREATE INDEX idx_lsb_bucket ON laundry_stock_balances (bucket, provider_id, item_id);

CREATE TABLE laundry_stock_lots (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    bucket VARCHAR(30) NOT NULL,
    provider_id BIGINT NULL,
    provider_scope BIGINT NOT NULL,
    remaining_quantity DECIMAL(12,3) NOT NULL,
    effective_at TIMESTAMP NOT NULL,
    source_movement_id BIGINT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lsl_item FOREIGN KEY (item_id) REFERENCES laundry_items(id),
    CONSTRAINT fk_lsl_provider FOREIGN KEY (provider_id) REFERENCES laundry_providers(id),
    CONSTRAINT chk_lsl_bucket CHECK (
        bucket IN (
            'HOTEL_AVAILABLE',
            'HOTEL_REWASH_QUEUE',
            'PROVIDER_PROCESSING',
            'PROVIDER_REWASH',
            'MISSING',
            'DAMAGED'
        )
    ),
    CONSTRAINT chk_lsl_scope CHECK (
        (bucket = 'HOTEL_AVAILABLE' AND provider_id IS NULL AND provider_scope = 0)
        OR (bucket <> 'HOTEL_AVAILABLE' AND provider_id IS NOT NULL AND provider_scope = provider_id)
    ),
    CONSTRAINT chk_lsl_remaining CHECK (remaining_quantity >= 0)
);

CREATE INDEX idx_lsl_fifo ON laundry_stock_lots (item_id, bucket, provider_scope, effective_at, id);

CREATE TABLE laundry_stock_movements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    provider_id BIGINT NULL,
    provider_scope BIGINT NOT NULL,
    movement_type VARCHAR(40) NOT NULL,
    from_bucket VARCHAR(30) NULL,
    to_bucket VARCHAR(30) NULL,
    quantity DECIMAL(12,3) NOT NULL,
    effective_at TIMESTAMP NOT NULL,
    document_type VARCHAR(30) NULL,
    document_id BIGINT NULL,
    document_line_id BIGINT NULL,
    actor_username VARCHAR(120) NOT NULL,
    actor_role VARCHAR(40) NOT NULL,
    reason VARCHAR(500) NULL,
    correlation_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lsm_item FOREIGN KEY (item_id) REFERENCES laundry_items(id),
    CONSTRAINT fk_lsm_provider FOREIGN KEY (provider_id) REFERENCES laundry_providers(id),
    CONSTRAINT chk_lsm_type CHECK (
        movement_type IN (
            'OPENING',
            'DISPATCH',
            'RETURN_CLEAN',
            'RETURN_REWASH_QUEUE',
            'RETENTION',
            'MISSING',
            'DAMAGED',
            'ADJUSTMENT_INCREASE',
            'ADJUSTMENT_DECREASE',
            'TRANSFER',
            'REGULARIZATION'
        )
    ),
    CONSTRAINT chk_lsm_from_bucket CHECK (
        from_bucket IS NULL OR from_bucket IN (
            'HOTEL_AVAILABLE',
            'HOTEL_REWASH_QUEUE',
            'PROVIDER_PROCESSING',
            'PROVIDER_REWASH',
            'MISSING',
            'DAMAGED'
        )
    ),
    CONSTRAINT chk_lsm_to_bucket CHECK (
        to_bucket IS NULL OR to_bucket IN (
            'HOTEL_AVAILABLE',
            'HOTEL_REWASH_QUEUE',
            'PROVIDER_PROCESSING',
            'PROVIDER_REWASH',
            'MISSING',
            'DAMAGED'
        )
    ),
    CONSTRAINT chk_lsm_endpoint CHECK (from_bucket IS NOT NULL OR to_bucket IS NOT NULL),
    CONSTRAINT chk_lsm_quantity CHECK (quantity > 0),
    CONSTRAINT chk_lsm_provider_scope CHECK (provider_scope >= 0)
);

CREATE INDEX idx_lsm_document ON laundry_stock_movements (document_type, document_id);
CREATE INDEX idx_lsm_correlation ON laundry_stock_movements (correlation_id);
CREATE INDEX idx_lsm_item_date ON laundry_stock_movements (item_id, effective_at, id);

CREATE TABLE laundry_obligations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    provider_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    source_document_type VARCHAR(30) NOT NULL,
    source_document_id BIGINT NOT NULL,
    source_line_id BIGINT NOT NULL,
    original_quantity DECIMAL(12,3) NOT NULL,
    open_quantity DECIMAL(12,3) NOT NULL,
    in_rewash_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    unit_price DECIMAL(12,2) NOT NULL,
    effective_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lo_provider FOREIGN KEY (provider_id) REFERENCES laundry_providers(id),
    CONSTRAINT fk_lo_item FOREIGN KEY (item_id) REFERENCES laundry_items(id),
    CONSTRAINT uk_lo_source_line UNIQUE (source_document_type, source_line_id),
    CONSTRAINT chk_lo_status CHECK (status IN ('OPEN', 'CLOSED')),
    CONSTRAINT chk_lo_original CHECK (original_quantity > 0),
    CONSTRAINT chk_lo_open CHECK (open_quantity >= 0),
    CONSTRAINT chk_lo_rewash CHECK (in_rewash_quantity >= 0),
    CONSTRAINT chk_lo_open_limit CHECK (open_quantity <= original_quantity),
    CONSTRAINT chk_lo_rewash_limit CHECK (in_rewash_quantity <= open_quantity)
);

CREATE INDEX idx_lo_fifo ON laundry_obligations (provider_id, item_id, status, effective_at, id);

CREATE TABLE laundry_opening_stock (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reference VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    approved_by VARCHAR(120) NOT NULL,
    approved_at TIMESTAMP NOT NULL,
    effective_at TIMESTAMP NOT NULL,
    notes VARCHAR(500) NULL,
    correlation_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_los_reference UNIQUE (reference),
    CONSTRAINT chk_los_status CHECK (status IN ('APPROVED'))
);

CREATE INDEX idx_los_correlation ON laundry_opening_stock (correlation_id);

CREATE TABLE laundry_opening_stock_lines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    opening_stock_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity DECIMAL(12,3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_losl_opening FOREIGN KEY (opening_stock_id) REFERENCES laundry_opening_stock(id) ON DELETE CASCADE,
    CONSTRAINT fk_losl_item FOREIGN KEY (item_id) REFERENCES laundry_items(id),
    CONSTRAINT uk_losl_item UNIQUE (opening_stock_id, item_id),
    CONSTRAINT chk_losl_quantity CHECK (quantity >= 0)
);

CREATE TABLE laundry_stock_adjustments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    operation VARCHAR(20) NOT NULL,
    from_bucket VARCHAR(30) NULL,
    to_bucket VARCHAR(30) NULL,
    provider_id BIGINT NULL,
    provider_scope BIGINT NOT NULL,
    quantity DECIMAL(12,3) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    effective_at TIMESTAMP NOT NULL,
    actor_username VARCHAR(120) NOT NULL,
    actor_role VARCHAR(40) NOT NULL,
    correlation_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lsa_item FOREIGN KEY (item_id) REFERENCES laundry_items(id),
    CONSTRAINT fk_lsa_provider FOREIGN KEY (provider_id) REFERENCES laundry_providers(id),
    CONSTRAINT chk_lsa_operation CHECK (operation IN ('INCREASE', 'DECREASE', 'TRANSFER')),
    CONSTRAINT chk_lsa_quantity CHECK (quantity > 0),
    CONSTRAINT chk_lsa_provider_scope CHECK (provider_scope >= 0),
    CONSTRAINT chk_lsa_shape CHECK (
        (operation = 'INCREASE' AND from_bucket IS NULL AND to_bucket IS NOT NULL)
        OR (operation = 'DECREASE' AND from_bucket IS NOT NULL AND to_bucket IS NULL)
        OR (operation = 'TRANSFER' AND from_bucket IS NOT NULL AND to_bucket IS NOT NULL AND from_bucket <> to_bucket)
    )
);

CREATE INDEX idx_lsa_item_date ON laundry_stock_adjustments (item_id, effective_at, id);
CREATE INDEX idx_lsa_correlation ON laundry_stock_adjustments (correlation_id);

CREATE TABLE laundry_idempotency_keys (
    id BIGINT NOT NULL AUTO_INCREMENT,
    actor_username VARCHAR(120) NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    request_fingerprint VARCHAR(64) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    response_status INT NULL,
    response_body LONGTEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_lik_actor_key UNIQUE (actor_username, idempotency_key),
    CONSTRAINT chk_lik_response_status CHECK (
        response_status IS NULL OR (response_status >= 100 AND response_status <= 599)
    )
);

CREATE INDEX idx_lik_expires ON laundry_idempotency_keys (expires_at);
