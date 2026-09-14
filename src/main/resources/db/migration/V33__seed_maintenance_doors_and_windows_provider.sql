-- Keep existing INTERNET providers and orders unchanged. Doors and windows is
-- a new specialty, not a rename of INTERNET.
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
)
SELECT
    'EXTERNAL',
    'DOORS_AND_WINDOWS',
    'Servico de portas e janelas',
    'Manutencao de portas e janelas',
    'Prestador externo para portas, fechaduras, janelas e esquadrias.',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
WHERE NOT EXISTS (
    SELECT 1
    FROM maintenance_providers
    WHERE specialty = 'DOORS_AND_WINDOWS'
);
