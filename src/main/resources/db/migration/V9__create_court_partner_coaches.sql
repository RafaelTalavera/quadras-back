CREATE TABLE court_partner_coaches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(120) NULL,
    CONSTRAINT uk_court_partner_coaches_name UNIQUE (name)
);

INSERT INTO court_partner_coaches (name, active, updated_by)
VALUES
    ('Professor parceiro 1', TRUE, 'system'),
    ('Professor parceiro 2', TRUE, 'system'),
    ('Professor parceiro 3', TRUE, 'system');
