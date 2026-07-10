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
    'INTERNET',
    'Servico de internet',
    'Manutencao de internet',
    'Prestador externo para conectividade, Wi-Fi, cabeamento e suporte de rede.',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
WHERE NOT EXISTS (
    SELECT 1
    FROM maintenance_providers
    WHERE specialty = 'INTERNET'
);
