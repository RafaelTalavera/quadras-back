ALTER TABLE app_users
    DROP CONSTRAINT chk_app_users_laundry_provider;

ALTER TABLE app_users
    ADD COLUMN provider_type VARCHAR(20) NULL;

ALTER TABLE app_users
    ADD COLUMN court_partner_coach_id BIGINT NULL;

ALTER TABLE app_users
    ADD COLUMN massage_therapist_id BIGINT NULL;

ALTER TABLE app_users
    ADD COLUMN maintenance_provider_id BIGINT NULL;

UPDATE app_users
SET provider_type = 'LAUNDRY'
WHERE role = 'PROVIDER';

ALTER TABLE app_users
    ADD CONSTRAINT fk_app_users_court_partner_coach
        FOREIGN KEY (court_partner_coach_id) REFERENCES court_partner_coaches(id);

ALTER TABLE app_users
    ADD CONSTRAINT fk_app_users_massage_therapist
        FOREIGN KEY (massage_therapist_id) REFERENCES massage_therapists(id);

ALTER TABLE app_users
    ADD CONSTRAINT fk_app_users_maintenance_provider
        FOREIGN KEY (maintenance_provider_id) REFERENCES maintenance_providers(id);

ALTER TABLE app_users
    ADD CONSTRAINT chk_app_users_provider_access
        CHECK (
            (
                role = 'PROVIDER'
                AND provider_type IN ('COURTS', 'MASSAGE', 'MAINTENANCE', 'LAUNDRY')
                AND (
                    (provider_type = 'COURTS'
                        AND court_partner_coach_id IS NOT NULL
                        AND massage_therapist_id IS NULL
                        AND maintenance_provider_id IS NULL
                        AND laundry_provider_id IS NULL)
                    OR
                    (provider_type = 'MASSAGE'
                        AND court_partner_coach_id IS NULL
                        AND massage_therapist_id IS NOT NULL
                        AND maintenance_provider_id IS NULL
                        AND laundry_provider_id IS NULL)
                    OR
                    (provider_type = 'MAINTENANCE'
                        AND court_partner_coach_id IS NULL
                        AND massage_therapist_id IS NULL
                        AND maintenance_provider_id IS NOT NULL
                        AND laundry_provider_id IS NULL)
                    OR
                    (provider_type = 'LAUNDRY'
                        AND court_partner_coach_id IS NULL
                        AND massage_therapist_id IS NULL
                        AND maintenance_provider_id IS NULL
                        AND laundry_provider_id IS NOT NULL)
                )
            )
            OR
            (
                role IN ('OPERATOR', 'SUPERVISOR')
                AND provider_type IS NULL
                AND court_partner_coach_id IS NULL
                AND massage_therapist_id IS NULL
                AND maintenance_provider_id IS NULL
                AND laundry_provider_id IS NULL
            )
        );

CREATE INDEX idx_app_users_court_partner_coach
    ON app_users (court_partner_coach_id, enabled, id);

CREATE INDEX idx_app_users_massage_therapist
    ON app_users (massage_therapist_id, enabled, id);

CREATE INDEX idx_app_users_maintenance_provider
    ON app_users (maintenance_provider_id, enabled, id);
