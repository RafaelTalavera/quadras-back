CREATE TABLE audit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_name VARCHAR(40) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id BIGINT NOT NULL,
    action_name VARCHAR(40) NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_username VARCHAR(120) NULL,
    actor_role VARCHAR(60) NULL,
    summary_text VARCHAR(255) NULL,
    changes_json LONGTEXT NULL,
    before_state_json LONGTEXT NULL,
    after_state_json LONGTEXT NULL
);

CREATE INDEX idx_audit_events_entity_lookup
    ON audit_events (entity_type, entity_id, occurred_at, id);

ALTER TABLE reservations ADD COLUMN created_by VARCHAR(120) NULL;
ALTER TABLE reservations ADD COLUMN updated_by VARCHAR(120) NULL;
ALTER TABLE reservations ADD COLUMN cancelled_at TIMESTAMP NULL;
ALTER TABLE reservations ADD COLUMN cancelled_by VARCHAR(120) NULL;

UPDATE reservations
SET created_by = COALESCE(created_by, 'system'),
    updated_by = COALESCE(updated_by, 'system')
WHERE created_by IS NULL OR updated_by IS NULL;
