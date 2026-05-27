CREATE TABLE maintenance_plans (
    id BIGINT NOT NULL AUTO_INCREMENT,
    location_id BIGINT NOT NULL,
    provider_id BIGINT NULL,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(1500) NULL,
    recurrence_unit VARCHAR(20) NOT NULL,
    recurrence_interval INT NOT NULL,
    next_due_date DATE NOT NULL,
    last_generated_on DATE NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_maintenance_plans_location
        FOREIGN KEY (location_id) REFERENCES maintenance_locations (id),
    CONSTRAINT fk_maintenance_plans_provider
        FOREIGN KEY (provider_id) REFERENCES maintenance_providers (id)
);

CREATE INDEX idx_maintenance_plans_next_due_date
    ON maintenance_plans (next_due_date, active);

ALTER TABLE maintenance_orders
    ADD COLUMN order_kind VARCHAR(20) NOT NULL DEFAULT 'CORRECTIVE';

ALTER TABLE maintenance_orders
    ADD COLUMN plan_id BIGINT NULL;

ALTER TABLE maintenance_orders
    ADD CONSTRAINT fk_maintenance_orders_plan
        FOREIGN KEY (plan_id) REFERENCES maintenance_plans (id);

CREATE INDEX idx_maintenance_orders_plan
    ON maintenance_orders (plan_id, status);
