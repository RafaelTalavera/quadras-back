CREATE TABLE maintenance_locations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    location_type VARCHAR(20) NOT NULL,
    code VARCHAR(60) NOT NULL,
    label VARCHAR(160) NOT NULL,
    floor VARCHAR(40) NULL,
    description VARCHAR(500) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_maintenance_locations_type_code UNIQUE (location_type, code)
);

CREATE TABLE maintenance_providers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    provider_type VARCHAR(20) NOT NULL,
    name VARCHAR(120) NOT NULL,
    service_label VARCHAR(120) NOT NULL,
    scope_description VARCHAR(500) NULL,
    contact VARCHAR(160) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_maintenance_providers_type_name UNIQUE (provider_type, name)
);

CREATE TABLE maintenance_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    location_id BIGINT NOT NULL,
    location_type_snapshot VARCHAR(20) NOT NULL,
    location_code_snapshot VARCHAR(60) NOT NULL,
    location_label_snapshot VARCHAR(160) NOT NULL,
    provider_id BIGINT NOT NULL,
    provider_type_snapshot VARCHAR(20) NOT NULL,
    provider_name_snapshot VARCHAR(120) NOT NULL,
    service_label_snapshot VARCHAR(120) NOT NULL,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(1500) NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reported_at TIMESTAMP NOT NULL,
    scheduled_start_at TIMESTAMP NULL,
    scheduled_end_at TIMESTAMP NULL,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    resolution_notes VARCHAR(1500) NULL,
    cancellation_notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    cancelled_by VARCHAR(120) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_maintenance_orders_location
        FOREIGN KEY (location_id) REFERENCES maintenance_locations (id),
    CONSTRAINT fk_maintenance_orders_provider
        FOREIGN KEY (provider_id) REFERENCES maintenance_providers (id)
);

CREATE TABLE maintenance_order_attachments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    maintenance_order_id BIGINT NOT NULL,
    attachment_type VARCHAR(20) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    file_size BIGINT NOT NULL,
    file_content BLOB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(120) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_maintenance_order_attachments_order
        FOREIGN KEY (maintenance_order_id) REFERENCES maintenance_orders (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_maintenance_orders_location ON maintenance_orders (location_id);
CREATE INDEX idx_maintenance_orders_provider ON maintenance_orders (provider_id);
CREATE INDEX idx_maintenance_orders_status ON maintenance_orders (status);
CREATE INDEX idx_maintenance_orders_scheduled_start ON maintenance_orders (scheduled_start_at);

INSERT INTO maintenance_providers (
    provider_type,
    name,
    service_label,
    scope_description,
    contact,
    active,
    created_at,
    updated_at,
    created_by,
    updated_by
) VALUES
(
    'INTERNAL',
    'Mantenimiento interno',
    'Equipo interno del hotel',
    'Atencion operativa general de mantenimiento interno del hotel.',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
),
(
    'EXTERNAL',
    'Servicio de mantenimiento de aires',
    'Mantenimiento de aires',
    'Proveedor externo para revision, limpieza y reparacion de aires acondicionados.',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
),
(
    'EXTERNAL',
    'Servicio de mantenimiento de internet',
    'Mantenimiento de internet',
    'Proveedor externo para conectividad, routers y soporte de red.',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
);
