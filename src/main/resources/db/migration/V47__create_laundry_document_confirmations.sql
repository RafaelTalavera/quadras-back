CREATE TABLE laundry_document_confirmations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    notes VARCHAR(500) NULL,
    actor_username VARCHAR(120) NOT NULL,
    actor_role VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ldc_document FOREIGN KEY (document_id) REFERENCES laundry_documents(id),
    CONSTRAINT chk_ldc_action CHECK (action_type IN ('PROVIDER_CONFIRMED', 'PROVIDER_OBSERVED', 'HOTEL_RESOLVED')),
    CONSTRAINT chk_ldc_role CHECK (actor_role IN ('OPERATOR', 'SUPERVISOR', 'PROVIDER')),
    CONSTRAINT chk_ldc_notes CHECK (
        (action_type = 'PROVIDER_CONFIRMED')
        OR
        (notes IS NOT NULL AND CHAR_LENGTH(TRIM(notes)) > 0)
    )
);

CREATE INDEX idx_ldc_document_created
    ON laundry_document_confirmations (document_id, created_at, id);

CREATE INDEX idx_ldc_document_action
    ON laundry_document_confirmations (document_id, action_type, id);
