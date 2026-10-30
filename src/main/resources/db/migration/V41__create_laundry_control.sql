CREATE TABLE laundry_providers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    contact VARCHAR(160) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_laundry_providers_name UNIQUE (name)
);

CREATE TABLE laundry_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    unit_type VARCHAR(20) NOT NULL,
    current_unit_price DECIMAL(12,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_laundry_items_name UNIQUE (name)
);

CREATE TABLE laundry_shipments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shipment_number VARCHAR(36) NOT NULL,
    provider_id BIGINT NOT NULL,
    provider_name_snapshot VARCHAR(120) NOT NULL,
    kind VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    received_at TIMESTAMP NULL,
    notes VARCHAR(500) NULL,
    cancellation_notes VARCHAR(500) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_laundry_shipments_number UNIQUE (shipment_number),
    CONSTRAINT fk_laundry_shipments_provider FOREIGN KEY (provider_id) REFERENCES laundry_providers (id)
);

CREATE TABLE laundry_shipment_lines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shipment_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    item_name_snapshot VARCHAR(120) NOT NULL,
    unit_type_snapshot VARCHAR(20) NOT NULL,
    sent_quantity DECIMAL(12,3) NOT NULL,
    accepted_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    rewash_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    missing_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    damaged_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    unit_price_snapshot DECIMAL(12,2) NOT NULL,
    billable_amount DECIMAL(12,2) NOT NULL,
    origin_line_id BIGINT NULL,
    rewash_reason VARCHAR(30) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_laundry_shipment_item UNIQUE (shipment_id, item_id),
    CONSTRAINT fk_laundry_lines_shipment FOREIGN KEY (shipment_id) REFERENCES laundry_shipments (id) ON DELETE CASCADE,
    CONSTRAINT fk_laundry_lines_item FOREIGN KEY (item_id) REFERENCES laundry_items (id),
    CONSTRAINT fk_laundry_lines_origin FOREIGN KEY (origin_line_id) REFERENCES laundry_shipment_lines (id)
);

CREATE INDEX idx_laundry_shipments_status_sent ON laundry_shipments (status, sent_at);
CREATE INDEX idx_laundry_shipments_provider_sent ON laundry_shipments (provider_id, sent_at);
CREATE INDEX idx_laundry_lines_origin ON laundry_shipment_lines (origin_line_id);

INSERT INTO laundry_providers (name, active) VALUES ('Aguas Claras', TRUE), ('5aSec', TRUE);

INSERT INTO laundry_items (name, unit_type, current_unit_price, active) VALUES
('Sabana individual', 'PIECE', 0, TRUE),
('Sabana doble', 'PIECE', 0, TRUE),
('Funda de almohada', 'PIECE', 0, TRUE),
('Toalla de bano', 'PIECE', 0, TRUE),
('Toalla de rostro', 'PIECE', 0, TRUE),
('Alfombra de bano', 'PIECE', 0, TRUE),
('Cobertor', 'PIECE', 0, TRUE);
