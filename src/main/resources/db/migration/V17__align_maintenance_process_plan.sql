ALTER TABLE maintenance_providers
    ADD COLUMN specialty VARCHAR(40) NOT NULL DEFAULT 'GENERAL_MAINTENANCE';

UPDATE maintenance_providers
SET specialty = CASE
    WHEN provider_type = 'EXTERNAL' AND (
        LOWER(name) LIKE '%aire%' OR LOWER(service_label) LIKE '%aire%'
    ) THEN 'AIR_CONDITIONING'
    WHEN provider_type = 'EXTERNAL' AND (
        LOWER(name) LIKE '%elev%' OR LOWER(service_label) LIKE '%elev%'
    ) THEN 'ELEVATORS'
    ELSE 'GENERAL_MAINTENANCE'
END;

ALTER TABLE maintenance_orders
    MODIFY COLUMN provider_id BIGINT NULL;

ALTER TABLE maintenance_orders
    MODIFY COLUMN provider_type_snapshot VARCHAR(20) NULL;

ALTER TABLE maintenance_orders
    MODIFY COLUMN provider_name_snapshot VARCHAR(120) NULL;

ALTER TABLE maintenance_orders
    MODIFY COLUMN service_label_snapshot VARCHAR(120) NULL;

ALTER TABLE maintenance_orders
    ADD COLUMN request_origin VARCHAR(30) NOT NULL DEFAULT 'INTERNAL_ROLE';

ALTER TABLE maintenance_orders
    ADD COLUMN requested_for_guest BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE maintenance_orders
    ADD COLUMN guest_name VARCHAR(160) NULL;

ALTER TABLE maintenance_orders
    ADD COLUMN guest_reference VARCHAR(80) NULL;

ALTER TABLE maintenance_orders
    ADD COLUMN requested_by_username VARCHAR(120) NOT NULL DEFAULT 'system';

ALTER TABLE maintenance_orders
    ADD COLUMN requested_by_role VARCHAR(60) NULL;

ALTER TABLE maintenance_orders
    ADD COLUMN business_priority VARCHAR(30) NOT NULL DEFAULT 'INTERNAL_STANDARD';

ALTER TABLE maintenance_orders
    ADD COLUMN estimated_execution_minutes INT NULL;

ALTER TABLE maintenance_orders
    ADD COLUMN assigned_username VARCHAR(120) NULL;

ALTER TABLE maintenance_orders
    ADD COLUMN assigned_at TIMESTAMP NULL;

UPDATE maintenance_orders
SET requested_by_username = COALESCE(created_by, 'system'),
    requested_by_role = COALESCE(requested_by_role, 'OPERATOR'),
    business_priority = CASE
        WHEN priority = 'URGENT' THEN 'CRITICAL_OPERATION'
        ELSE 'INTERNAL_STANDARD'
    END,
    estimated_execution_minutes = CASE
        WHEN scheduled_start_at IS NOT NULL AND scheduled_end_at IS NOT NULL
            THEN TIMESTAMPDIFF(MINUTE, scheduled_start_at, scheduled_end_at)
        ELSE NULL
    END,
    assigned_username = CASE
        WHEN provider_id IS NOT NULL THEN COALESCE(updated_by, created_by, 'system')
        ELSE NULL
    END,
    assigned_at = CASE
        WHEN provider_id IS NOT NULL THEN COALESCE(updated_at, created_at, CURRENT_TIMESTAMP)
        ELSE NULL
    END,
    status = CASE
        WHEN status = 'OPEN' AND provider_id IS NOT NULL THEN 'ASSIGNED'
        ELSE status
    END;

UPDATE maintenance_orders
SET provider_id = NULL
WHERE provider_id IN (
    SELECT id
    FROM maintenance_providers
    WHERE name = 'Servicio de mantenimiento de internet'
);

DELETE FROM maintenance_providers
WHERE name = 'Servicio de mantenimiento de internet';

INSERT INTO maintenance_providers (
    provider_type,
    specialty,
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
    'EXTERNAL',
    'ELEVATORS',
    'Servicio de elevadores',
    'Mantenimiento de elevadores',
    'Proveedor externo para inspeccion, reparacion y rescate tecnico de elevadores.',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
),
(
    'EXTERNAL',
    'AIR_CONDITIONING',
    'Servicio de aires acondicionados',
    'Mantenimiento de aires acondicionados',
    'Proveedor externo para limpieza, mantenimiento preventivo y reparaciones de aire acondicionado.',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
);
