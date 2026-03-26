CREATE TABLE tour_provider_offerings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    service_type VARCHAR(20) NOT NULL,
    name VARCHAR(120) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(1000) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(120) NULL,
    CONSTRAINT fk_tour_provider_offerings_provider
        FOREIGN KEY (provider_id) REFERENCES tour_providers (id),
    CONSTRAINT uk_tour_provider_offerings_provider_name UNIQUE (provider_id, name),
    CONSTRAINT chk_tour_provider_offerings_amount CHECK (amount >= 0)
);

ALTER TABLE tour_bookings
    ADD COLUMN provider_offering_id BIGINT NULL;

ALTER TABLE tour_bookings
    ADD COLUMN provider_offering_name VARCHAR(120) NULL;

ALTER TABLE tour_bookings
    ADD CONSTRAINT fk_tour_bookings_provider_offering
        FOREIGN KEY (provider_offering_id) REFERENCES tour_provider_offerings (id);

CREATE INDEX idx_tour_provider_offerings_provider
    ON tour_provider_offerings (provider_id, active, name);

CREATE INDEX idx_tour_bookings_provider_offering
    ON tour_bookings (provider_offering_id, start_at);

INSERT INTO tour_provider_offerings (
    provider_id,
    service_type,
    name,
    amount,
    description,
    active,
    updated_by
)
SELECT id,
       'TOUR',
       'Isla de Campeche con desembarco',
       350.00,
       'Viaje con desembarco que incluye traslado al puerto.',
       TRUE,
       'system'
FROM tour_providers
WHERE name = 'Agencia Costa Norte';

INSERT INTO tour_provider_offerings (
    provider_id,
    service_type,
    name,
    amount,
    description,
    active,
    updated_by
)
SELECT id,
       'TOUR',
       'Tour a Camburiu',
       150.00,
       'Traslado con city tour a Camburiu e incluye paradas.',
       TRUE,
       'system'
FROM tour_providers
WHERE name = 'Agencia Costa Norte';

INSERT INTO tour_provider_offerings (
    provider_id,
    service_type,
    name,
    amount,
    description,
    active,
    updated_by
)
SELECT id,
       'TRAVEL',
       'Traslado Aeropuerto',
       190.00,
       'Hasta 4 personas, auto pequeno.',
       TRUE,
       'system'
FROM tour_providers
WHERE name = 'Traslados Ilha Sul';

INSERT INTO tour_provider_offerings (
    provider_id,
    service_type,
    name,
    amount,
    description,
    active,
    updated_by
)
SELECT id,
       'TRAVEL',
       'Traslado Centro',
       120.00,
       'Hasta 4 personas, auto pequeno.',
       TRUE,
       'system'
FROM tour_providers
WHERE name = 'Traslados Ilha Sul';

INSERT INTO tour_provider_offerings (
    provider_id,
    service_type,
    name,
    amount,
    description,
    active,
    updated_by
)
SELECT id,
       'TRAVEL',
       'Traslado Canasvieira',
       90.00,
       'Hasta 4 personas, auto pequeno.',
       TRUE,
       'system'
FROM tour_providers
WHERE name = 'Traslados Ilha Sul';
