CREATE TABLE laundry_documents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reference VARCHAR(36) NOT NULL,
    document_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider_id BIGINT NOT NULL,
    effective_at TIMESTAMP NOT NULL,
    notes VARCHAR(500) NULL,
    created_by VARCHAR(120) NOT NULL,
    submitted_by VARCHAR(120) NULL,
    submitted_at TIMESTAMP NULL,
    correlation_id VARCHAR(64) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ld_provider FOREIGN KEY (provider_id) REFERENCES laundry_providers(id),
    CONSTRAINT uk_ld_reference UNIQUE (reference),
    CONSTRAINT chk_ld_type CHECK (document_type IN ('STANDARD_DISPATCH', 'REWASH_DISPATCH')),
    CONSTRAINT chk_ld_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'CONFIRMED', 'OBSERVED', 'RESOLVED', 'CANCELLED'))
);

CREATE INDEX idx_ld_provider_status_date
    ON laundry_documents (provider_id, status, effective_at, id);
CREATE INDEX idx_ld_type_status_date
    ON laundry_documents (document_type, status, effective_at, id);
CREATE INDEX idx_ld_correlation ON laundry_documents (correlation_id);

CREATE TABLE laundry_document_lines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity DECIMAL(12,3) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ldl_document FOREIGN KEY (document_id) REFERENCES laundry_documents(id),
    CONSTRAINT fk_ldl_item FOREIGN KEY (item_id) REFERENCES laundry_items(id),
    CONSTRAINT uk_ldl_document_item UNIQUE (document_id, item_id),
    CONSTRAINT chk_ldl_quantity CHECK (quantity > 0),
    CONSTRAINT chk_ldl_price CHECK (unit_price >= 0)
);

CREATE INDEX idx_ldl_item ON laundry_document_lines (item_id, document_id);

CREATE TABLE laundry_rewash_dispatch_allocations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    document_line_id BIGINT NOT NULL,
    obligation_id BIGINT NOT NULL,
    quantity DECIMAL(12,3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lrda_line FOREIGN KEY (document_line_id) REFERENCES laundry_document_lines(id),
    CONSTRAINT fk_lrda_obligation FOREIGN KEY (obligation_id) REFERENCES laundry_obligations(id),
    CONSTRAINT uk_lrda_line_obligation UNIQUE (document_line_id, obligation_id),
    CONSTRAINT chk_lrda_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_lrda_obligation ON laundry_rewash_dispatch_allocations (obligation_id, id);
