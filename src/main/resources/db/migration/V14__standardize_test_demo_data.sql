UPDATE reservations
SET guest_name = CASE
    WHEN guest_name = 'H10 Integracion Editada' THEN 'Helena Duarte'
    WHEN guest_name = 'Smoke Script Editado' THEN 'Marco Silveira'
    ELSE guest_name
END
WHERE guest_name IN ('H10 Integracion Editada', 'Smoke Script Editado');

UPDATE court_bookings
SET customer_name = CASE
        WHEN customer_name = 'dsac' THEN 'Camila Freitas'
        WHEN customer_name = 'Prueba Huesped' THEN 'Helena Duarte'
        WHEN customer_name = 'Prueba Externo' THEN 'Marcelo Costa'
        WHEN customer_name = 'Prueba VIP' THEN 'Patricia Ramos'
        WHEN customer_name = 'cwecr' THEN 'Ricardo Mattos'
        WHEN customer_name = 'Prueba Pasado' THEN 'Marco Silveira'
        WHEN customer_name = 'Prueba valo nocturno' THEN 'Eduardo Rocha'
        WHEN customer_name = 'Pepe' THEN 'Pedro Almeida'
        ELSE customer_name
    END,
    customer_reference = CASE
        WHEN customer_reference = 'wecc' THEN 'Apto 110'
        WHEN customer_reference = 'TEST-HUESPED-250326' THEN 'Apto 101'
        WHEN customer_reference = 'TEST-EXTERNO-250326' THEN 'Externo 01'
        WHEN customer_reference = 'TEST-VIP-250326' THEN 'Apto 222'
        WHEN customer_reference = 'wecerc' THEN 'Apto 305'
        WHEN customer_reference = 'TEST-PAST' THEN 'Externo 02'
        WHEN customer_reference = 'Prueba valo nocturno' THEN 'Externo 03'
        WHEN customer_reference = '405' THEN 'Apto 405'
        WHEN customer_reference = 'Ana Profesora' THEN 'Ana Souza'
        WHEN customer_reference = 'Horas Aula' THEN 'Diego Lima'
        ELSE customer_reference
    END
WHERE customer_name IN (
    'dsac',
    'Prueba Huesped',
    'Prueba Externo',
    'Prueba VIP',
    'cwecr',
    'Prueba Pasado',
    'Prueba valo nocturno',
    'Pepe'
)
   OR customer_reference IN (
    'wecc',
    'TEST-HUESPED-250326',
    'TEST-EXTERNO-250326',
    'TEST-VIP-250326',
    'wecerc',
    'TEST-PAST',
    'Prueba valo nocturno',
    '405',
    'Ana Profesora',
    'Horas Aula'
);

UPDATE tour_bookings
SET client_name = CASE
        WHEN client_name = 'dddd' THEN 'Daniel Sosa'
        WHEN client_name = 'www' THEN 'Lucia Peres'
        WHEN client_name = 'MARCOS' THEN 'Marcos Pereira'
        ELSE client_name
    END,
    guest_reference = CASE
        WHEN guest_reference = 'dedd' THEN 'Apto 402'
        WHEN guest_reference = 'www' THEN 'Casa 06'
        WHEN guest_reference = '407' THEN 'Apto 407'
        ELSE guest_reference
    END
WHERE client_name IN ('dddd', 'www', 'MARCOS')
   OR guest_reference IN ('dedd', 'www', '407');

INSERT INTO massage_providers (name, specialty, contact, active, created_at, updated_at)
SELECT 'Danuska', 'Massagem', 'Agenda interna', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM massage_providers
    WHERE LOWER(name) = 'danuska'
);

INSERT INTO massage_providers (name, specialty, contact, active, created_at, updated_at)
SELECT 'David', 'Massagem', '98804-3392', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM massage_providers
    WHERE LOWER(name) = 'david'
);

UPDATE massage_providers
SET name = 'Danuska',
    specialty = 'Massagem',
    contact = 'Agenda interna',
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE LOWER(name) = 'danuska';

UPDATE massage_providers
SET name = 'David',
    specialty = 'Massagem',
    contact = '98804-3392',
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE LOWER(name) = 'david';

INSERT INTO massage_therapists (provider_id, name, active, created_at, updated_at)
SELECT mp.id, 'Danuska', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM massage_providers mp
WHERE mp.name = 'Danuska'
  AND NOT EXISTS (
      SELECT 1
      FROM massage_therapists mt
      WHERE mt.provider_id = mp.id
        AND LOWER(mt.name) = 'danuska'
  );

INSERT INTO massage_therapists (provider_id, name, active, created_at, updated_at)
SELECT mp.id, 'David', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM massage_providers mp
WHERE mp.name = 'David'
  AND NOT EXISTS (
      SELECT 1
      FROM massage_therapists mt
      WHERE mt.provider_id = mp.id
        AND LOWER(mt.name) = 'david'
  );

INSERT INTO massage_therapists (provider_id, name, active, created_at, updated_at)
SELECT mp.id, 'Isabelita', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM massage_providers mp
WHERE mp.name = 'David'
  AND NOT EXISTS (
      SELECT 1
      FROM massage_therapists mt
      WHERE mt.provider_id = mp.id
        AND LOWER(mt.name) = 'isabelita'
  );

INSERT INTO massage_therapists (provider_id, name, active, created_at, updated_at)
SELECT mp.id, 'Maria', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM massage_providers mp
WHERE mp.name = 'David'
  AND NOT EXISTS (
      SELECT 1
      FROM massage_therapists mt
      WHERE mt.provider_id = mp.id
        AND LOWER(mt.name) = 'maria'
  );

UPDATE massage_therapists
SET name = 'Danuska',
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE provider_id = (SELECT id FROM massage_providers WHERE name = 'Danuska')
  AND LOWER(name) = 'danuska';

UPDATE massage_therapists
SET name = 'David',
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE provider_id = (SELECT id FROM massage_providers WHERE name = 'David')
  AND LOWER(name) = 'david';

UPDATE massage_therapists
SET name = 'Isabelita',
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE provider_id = (SELECT id FROM massage_providers WHERE name = 'David')
  AND LOWER(name) = 'isabelita';

UPDATE massage_therapists
SET name = 'Maria',
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE provider_id = (SELECT id FROM massage_providers WHERE name = 'David')
  AND LOWER(name) = 'maria';

UPDATE massage_bookings
SET provider_id = (SELECT id FROM massage_providers WHERE name = 'Danuska'),
    therapist_id = (
        SELECT mt.id
        FROM massage_therapists mt
        WHERE mt.provider_id = (SELECT id FROM massage_providers WHERE name = 'Danuska')
          AND mt.name = 'Danuska'
    )
WHERE provider_id IN (
    SELECT id
    FROM massage_providers
    WHERE LOWER(name) = 'danuska'
);

UPDATE massage_bookings
SET provider_id = (SELECT id FROM massage_providers WHERE name = 'David'),
    therapist_id = (
        SELECT mt.id
        FROM massage_therapists mt
        WHERE mt.provider_id = (SELECT id FROM massage_providers WHERE name = 'David')
          AND mt.name = 'David'
    )
WHERE provider_id IN (
    SELECT id
    FROM massage_providers
    WHERE LOWER(name) IN ('davi/simul', 'david')
);

UPDATE massage_bookings
SET provider_id = (SELECT id FROM massage_providers WHERE name = 'David'),
    therapist_id = (
        SELECT mt.id
        FROM massage_therapists mt
        WHERE mt.provider_id = (SELECT id FROM massage_providers WHERE name = 'David')
          AND mt.name = 'Isabelita'
    )
WHERE provider_id IN (
    SELECT id
    FROM massage_providers
    WHERE LOWER(name) IN ('david/isabela', 'david/isabelita', 'isabelita', 'isabelita/ david')
);

UPDATE massage_bookings
SET client_name = CASE
        WHEN client_name = 'ac' THEN 'Ana Clara'
        WHEN client_name = 'wdq' THEN 'Walter Diaz'
        WHEN client_name = 'qwxqxw' THEN 'Carla Quintero'
        WHEN client_name = 'dwq' THEN 'Diego Quiroz'
        WHEN client_name = 'externo' THEN 'Cliente Externo'
        WHEN client_name = 'PASSANTE' THEN 'Cliente Externo'
        WHEN client_name = 'joao' THEN 'Joao'
        WHEN client_name = 'jimena' THEN 'Jimena'
        WHEN client_name = 'lilian' THEN 'Lilian'
        WHEN client_name = 'fabian' THEN 'Fabian'
        ELSE client_name
    END,
    guest_reference = CASE
        WHEN guest_reference = '10' THEN 'Casa 10'
        WHEN guest_reference = '102' THEN 'Apto 102'
        WHEN guest_reference = '102+DL23:Q26' THEN 'Apto 102'
        WHEN guest_reference = '109' THEN 'Apto 109'
        WHEN guest_reference = '12' THEN 'Casa 12'
        WHEN guest_reference = '2' THEN 'Casa 02'
        WHEN guest_reference = '201' THEN 'Apto 201'
        WHEN guest_reference = '203' THEN 'Apto 203'
        WHEN guest_reference = '208' THEN 'Apto 208'
        WHEN guest_reference = '209' THEN 'Apto 209'
        WHEN guest_reference = '210' THEN 'Apto 210'
        WHEN guest_reference = '211' THEN 'Apto 211'
        WHEN guest_reference = '303' THEN 'Apto 303'
        WHEN guest_reference = '304' THEN 'Apto 304'
        WHEN guest_reference = '307' THEN 'Apto 307'
        WHEN guest_reference = '308' THEN 'Apto 308'
        WHEN guest_reference = '309' THEN 'Apto 309'
        WHEN guest_reference = '310' THEN 'Apto 310'
        WHEN guest_reference = '4' THEN 'Casa 04'
        WHEN guest_reference = '404' THEN 'Apto 404'
        WHEN guest_reference = '405' THEN 'Apto 405'
        WHEN guest_reference = '407' THEN 'Apto 407'
        WHEN guest_reference = '5' THEN 'Casa 05'
        WHEN guest_reference = '6' THEN 'Casa 06'
        WHEN guest_reference = '9' THEN 'Casa 09'
        WHEN guest_reference = 'acc' THEN 'Apto 205'
        WHEN guest_reference = 'APTO 111' THEN 'Apto 111'
        WHEN guest_reference = 'APTO 402' THEN 'Apto 402'
        WHEN guest_reference = 'EXTERNO' THEN 'Externo'
        WHEN guest_reference = 'externo' THEN 'Externo'
        WHEN guest_reference = 'gelson51999819563' THEN 'Externo'
        WHEN guest_reference = 'qwd' THEN 'Apto 204'
        WHEN guest_reference = 'qwx' THEN 'Apto 305'
        WHEN guest_reference = 'qwxqwx' THEN 'Apto 306'
        WHEN guest_reference = 'REINALDO' THEN 'Externo'
        ELSE guest_reference
    END
WHERE client_name IN (
    'ac',
    'wdq',
    'qwxqxw',
    'dwq',
    'externo',
    'PASSANTE',
    'joao',
    'jimena',
    'lilian',
    'fabian'
)
   OR guest_reference IN (
    '10',
    '102',
    '102+DL23:Q26',
    '109',
    '12',
    '2',
    '201',
    '203',
    '208',
    '209',
    '210',
    '211',
    '303',
    '304',
    '307',
    '308',
    '309',
    '310',
    '4',
    '404',
    '405',
    '407',
    '5',
    '6',
    '9',
    'acc',
    'APTO 111',
    'APTO 402',
    'EXTERNO',
    'externo',
    'gelson51999819563',
    'qwd',
    'qwx',
    'qwxqwx',
    'REINALDO'
);

DELETE FROM massage_therapists
WHERE id IN (
    SELECT therapist_id
    FROM (
        SELECT mt.id AS therapist_id
        FROM massage_therapists mt
        JOIN massage_providers mp ON mp.id = mt.provider_id
        WHERE NOT (
            (mp.name = 'Danuska' AND mt.name = 'Danuska')
            OR (mp.name = 'David' AND mt.name IN ('David', 'Isabelita', 'Maria'))
        )
    ) legacy_therapists
);

DELETE FROM massage_providers
WHERE id IN (
    SELECT provider_id
    FROM (
        SELECT id AS provider_id
        FROM massage_providers
        WHERE name NOT IN ('Danuska', 'David')
    ) legacy_providers
);
