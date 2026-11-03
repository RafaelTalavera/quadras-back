CREATE TABLE laundry_return_manifests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reference VARCHAR(36) NOT NULL,
    provider_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    effective_at TIMESTAMP NOT NULL,
    notes VARCHAR(500) NULL,
    created_by VARCHAR(120) NOT NULL,
    submitted_by VARCHAR(120) NULL,
    submitted_at TIMESTAMP NULL,
    confirmed_by VARCHAR(120) NULL,
    confirmed_at TIMESTAMP NULL,
    correction_notes VARCHAR(500) NULL,
    correlation_id VARCHAR(64) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lrm_provider FOREIGN KEY (provider_id) REFERENCES laundry_providers(id),
    CONSTRAINT uk_lrm_reference UNIQUE (reference),
    CONSTRAINT chk_lrm_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'CONFIRMED', 'CONFIRMED_BY_HOTEL', 'CANCELLED'))
);

CREATE INDEX idx_lrm_provider_status_date
    ON laundry_return_manifests (provider_id, status, effective_at, id);
CREATE INDEX idx_lrm_correlation ON laundry_return_manifests (correlation_id);

CREATE TABLE laundry_return_manifest_lines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    manifest_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    declared_clean_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    declared_retained_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    accepted_clean_quantity DECIMAL(12,3) NULL,
    returned_dirty_quantity DECIMAL(12,3) NULL,
    retained_quantity DECIMAL(12,3) NULL,
    missing_quantity DECIMAL(12,3) NULL,
    damaged_quantity DECIMAL(12,3) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lrml_manifest FOREIGN KEY (manifest_id) REFERENCES laundry_return_manifests(id),
    CONSTRAINT fk_lrml_item FOREIGN KEY (item_id) REFERENCES laundry_items(id),
    CONSTRAINT uk_lrml_manifest_item UNIQUE (manifest_id, item_id),
    CONSTRAINT chk_lrml_declared CHECK (
        declared_clean_quantity >= 0 AND declared_retained_quantity >= 0
        AND declared_clean_quantity + declared_retained_quantity > 0
    ),
    CONSTRAINT chk_lrml_final CHECK (
        (accepted_clean_quantity IS NULL AND returned_dirty_quantity IS NULL
            AND retained_quantity IS NULL AND missing_quantity IS NULL AND damaged_quantity IS NULL)
        OR
        (accepted_clean_quantity >= 0 AND returned_dirty_quantity >= 0
            AND retained_quantity >= 0 AND missing_quantity >= 0 AND damaged_quantity >= 0
            AND accepted_clean_quantity + returned_dirty_quantity + retained_quantity
                + missing_quantity + damaged_quantity > 0)
    )
);

CREATE INDEX idx_lrml_item ON laundry_return_manifest_lines (item_id, manifest_id);

CREATE TABLE laundry_return_allocations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    manifest_line_id BIGINT NOT NULL,
    obligation_id BIGINT NOT NULL,
    outcome VARCHAR(30) NOT NULL,
    source_bucket VARCHAR(30) NOT NULL,
    quantity DECIMAL(12,3) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lra_line FOREIGN KEY (manifest_line_id) REFERENCES laundry_return_manifest_lines(id),
    CONSTRAINT fk_lra_obligation FOREIGN KEY (obligation_id) REFERENCES laundry_obligations(id),
    CONSTRAINT uk_lra_line_obligation_outcome_bucket
        UNIQUE (manifest_line_id, obligation_id, outcome, source_bucket),
    CONSTRAINT chk_lra_outcome CHECK (outcome IN ('ACCEPTED_CLEAN', 'RETURNED_DIRTY', 'RETAINED', 'MISSING', 'DAMAGED')),
    CONSTRAINT chk_lra_source CHECK (source_bucket IN ('PROVIDER_PROCESSING', 'PROVIDER_REWASH')),
    CONSTRAINT chk_lra_quantity CHECK (quantity > 0),
    CONSTRAINT chk_lra_price CHECK (unit_price >= 0)
);

CREATE INDEX idx_lra_obligation ON laundry_return_allocations (obligation_id, id);

CREATE TABLE laundry_billable_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    provider_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    return_manifest_id BIGINT NOT NULL,
    return_line_id BIGINT NOT NULL,
    allocation_id BIGINT NOT NULL,
    obligation_id BIGINT NOT NULL,
    quantity DECIMAL(12,3) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    accepted_at TIMESTAMP NOT NULL,
    accepted_by VARCHAR(120) NOT NULL,
    export_status VARCHAR(20) NOT NULL DEFAULT 'PENDING_EXPORT',
    correlation_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lbe_provider FOREIGN KEY (provider_id) REFERENCES laundry_providers(id),
    CONSTRAINT fk_lbe_item FOREIGN KEY (item_id) REFERENCES laundry_items(id),
    CONSTRAINT fk_lbe_manifest FOREIGN KEY (return_manifest_id) REFERENCES laundry_return_manifests(id),
    CONSTRAINT fk_lbe_line FOREIGN KEY (return_line_id) REFERENCES laundry_return_manifest_lines(id),
    CONSTRAINT fk_lbe_allocation FOREIGN KEY (allocation_id) REFERENCES laundry_return_allocations(id),
    CONSTRAINT fk_lbe_obligation FOREIGN KEY (obligation_id) REFERENCES laundry_obligations(id),
    CONSTRAINT uk_lbe_allocation UNIQUE (allocation_id),
    CONSTRAINT chk_lbe_quantity CHECK (quantity > 0),
    CONSTRAINT chk_lbe_price CHECK (unit_price >= 0),
    CONSTRAINT chk_lbe_amount CHECK (amount >= 0),
    CONSTRAINT chk_lbe_export CHECK (export_status IN ('PENDING_EXPORT', 'EXPORTED'))
);

CREATE INDEX idx_lbe_provider_date ON laundry_billable_events (provider_id, accepted_at, id);
CREATE INDEX idx_lbe_export_date ON laundry_billable_events (export_status, accepted_at, id);
