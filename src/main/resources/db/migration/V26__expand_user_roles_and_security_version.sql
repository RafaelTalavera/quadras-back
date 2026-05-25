ALTER TABLE app_users
    ADD COLUMN security_version BIGINT NOT NULL DEFAULT 1;

ALTER TABLE app_users
    DROP CONSTRAINT chk_app_users_role;

ALTER TABLE app_users
    ADD CONSTRAINT chk_app_users_role CHECK (role IN ('OPERATOR', 'SUPERVISOR'));
