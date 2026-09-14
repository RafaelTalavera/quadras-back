-- New specialties do not change existing providers or maintenance orders.
INSERT INTO maintenance_providers (
    provider_type, specialty, name, service_label, scope_description,
    contact, active, created_at, updated_at, created_by, updated_by
)
SELECT
    'EXTERNAL', 'CAMERAS', 'Servico de cameras', 'Manutencao de cameras',
    'Prestador para cameras de seguranca, gravacao e monitoramento.',
    NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM maintenance_providers WHERE specialty = 'CAMERAS'
);

INSERT INTO maintenance_providers (
    provider_type, specialty, name, service_label, scope_description,
    contact, active, created_at, updated_at, created_by, updated_by
)
SELECT
    'EXTERNAL', 'TELEPHONES', 'Servico de telefonia', 'Manutencao de telefones',
    'Prestador para aparelhos, ramais e linhas telefonicas.',
    NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM maintenance_providers WHERE specialty = 'TELEPHONES'
);

INSERT INTO maintenance_providers (
    provider_type, specialty, name, service_label, scope_description,
    contact, active, created_at, updated_at, created_by, updated_by
)
SELECT
    'EXTERNAL', 'IT_SUPPORT', 'Servico de TI', 'Suporte de TI',
    'Prestador para computadores, sistemas e infraestrutura de TI.',
    NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM maintenance_providers WHERE specialty = 'IT_SUPPORT'
);
