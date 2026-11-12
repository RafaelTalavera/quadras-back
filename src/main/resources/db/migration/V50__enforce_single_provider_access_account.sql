CREATE UNIQUE INDEX uk_app_users_court_partner_coach
    ON app_users (court_partner_coach_id);

CREATE UNIQUE INDEX uk_app_users_massage_therapist
    ON app_users (massage_therapist_id);

CREATE UNIQUE INDEX uk_app_users_maintenance_provider
    ON app_users (maintenance_provider_id);

CREATE UNIQUE INDEX uk_app_users_laundry_provider
    ON app_users (laundry_provider_id);
