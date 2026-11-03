ALTER TABLE app_users
    DROP CONSTRAINT chk_app_users_role;

ALTER TABLE app_users
    ADD COLUMN laundry_provider_id BIGINT NULL;

ALTER TABLE app_users
    ADD CONSTRAINT fk_app_users_laundry_provider
        FOREIGN KEY (laundry_provider_id) REFERENCES laundry_providers(id);

ALTER TABLE app_users
    ADD CONSTRAINT chk_app_users_role
        CHECK (role IN ('OPERATOR', 'SUPERVISOR', 'PROVIDER'));

ALTER TABLE app_users
    ADD CONSTRAINT chk_app_users_laundry_provider
        CHECK (
            (role = 'PROVIDER' AND laundry_provider_id IS NOT NULL)
            OR
            (role IN ('OPERATOR', 'SUPERVISOR') AND laundry_provider_id IS NULL)
        );

CREATE INDEX idx_app_users_laundry_provider
    ON app_users (laundry_provider_id, enabled, id);
