INSERT INTO massage_bookings (
    booking_date,
    start_time,
    client_name,
    guest_reference,
    treatment,
    amount,
    provider_id,
    therapist_id,
    paid,
    payment_method,
    payment_date,
    payment_notes,
    status,
    cancellation_notes,
    created_at,
    updated_at,
    cancelled_at,
    created_by,
    updated_by,
    cancelled_by
)
SELECT
    seed.booking_date,
    seed.start_time,
    seed.client_name,
    seed.guest_reference,
    seed.treatment,
    seed.amount,
    mp.id,
    mt.id,
    seed.paid,
    seed.payment_method,
    seed.payment_date,
    seed.payment_notes,
    'SCHEDULED',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL,
    'system.seed.v16',
    'system.seed.v16',
    NULL
FROM (
    SELECT DATE '2026-04-01' AS booking_date, TIME '09:00:00' AS start_time, 'Camila Silveira' AS client_name, 'Apto 108' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-04-01' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-01' AS booking_date, TIME '11:00:00' AS start_time, 'Marco Mattos' AS client_name, 'Apto 115' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-01' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-01' AS booking_date, TIME '16:00:00' AS start_time, 'Beatriz Braga' AS client_name, 'Apto 122' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-01' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-02' AS booking_date, TIME '09:00:00' AS start_time, 'Beatriz Ramos' AS client_name, 'Apto 111' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-02' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-02' AS booking_date, TIME '11:00:00' AS start_time, 'Ricardo Rocha' AS client_name, 'Apto 118' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-02' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-03' AS booking_date, TIME '09:00:00' AS start_time, 'Patricia Araujo' AS client_name, 'Apto 114' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-03' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-04' AS booking_date, TIME '09:00:00' AS start_time, 'Diego Nunes' AS client_name, 'Apto 117' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-04' AS booking_date, TIME '11:00:00' AS start_time, 'Sandra Freitas' AS client_name, 'Apto 101' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-04' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-04' AS booking_date, TIME '16:00:00' AS start_time, 'Joao Lemos' AS client_name, 'Apto 108' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-04' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-05' AS booking_date, TIME '09:00:00' AS start_time, 'Joao Silveira' AS client_name, 'Apto 120' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-05' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-05' AS booking_date, TIME '11:00:00' AS start_time, 'Marina Mattos' AS client_name, 'Apto 104' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-05' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-06' AS booking_date, TIME '09:00:00' AS start_time, 'Eduardo Ramos' AS client_name, 'Apto 123' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-06' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-07' AS booking_date, TIME '09:00:00' AS start_time, 'Camila Araujo' AS client_name, 'Apto 103' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-04-07' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-07' AS booking_date, TIME '11:00:00' AS start_time, 'Marco Castro' AS client_name, 'Casa 06' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-07' AS booking_date, TIME '16:00:00' AS start_time, 'Beatriz Duarte' AS client_name, 'Apto 117' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-07' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-08' AS booking_date, TIME '09:00:00' AS start_time, 'Beatriz Nunes' AS client_name, 'Casa 07' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-08' AS booking_date, TIME '11:00:00' AS start_time, 'Ricardo Freitas' AS client_name, 'Apto 113' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-08' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-09' AS booking_date, TIME '09:00:00' AS start_time, 'Patricia Silveira' AS client_name, 'Apto 109' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-09' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-10' AS booking_date, TIME '09:00:00' AS start_time, 'Diego Ramos' AS client_name, 'Apto 112' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-04-10' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-10' AS booking_date, TIME '11:00:00' AS start_time, 'Sandra Rocha' AS client_name, 'Apto 119' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-10' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-10' AS booking_date, TIME '16:00:00' AS start_time, 'Joao Teles' AS client_name, 'Apto 103' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-11' AS booking_date, TIME '09:00:00' AS start_time, 'Joao Araujo' AS client_name, 'Apto 115' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-11' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-11' AS booking_date, TIME '11:00:00' AS start_time, 'Marina Castro' AS client_name, 'Apto 122' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-12' AS booking_date, TIME '09:00:00' AS start_time, 'Eduardo Nunes' AS client_name, 'Apto 118' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-13' AS booking_date, TIME '09:00:00' AS start_time, 'Camila Silveira' AS client_name, 'Apto 121' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-04-13' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-13' AS booking_date, TIME '11:00:00' AS start_time, 'Marco Mattos' AS client_name, 'Apto 105' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-13' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-13' AS booking_date, TIME '16:00:00' AS start_time, 'Beatriz Braga' AS client_name, 'Apto 112' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-13' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-14' AS booking_date, TIME '09:00:00' AS start_time, 'Beatriz Ramos' AS client_name, 'Apto 101' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-14' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-14' AS booking_date, TIME '11:00:00' AS start_time, 'Ricardo Rocha' AS client_name, 'Apto 108' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-14' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-15' AS booking_date, TIME '09:00:00' AS start_time, 'Patricia Araujo' AS client_name, 'Apto 104' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-15' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-16' AS booking_date, TIME '09:00:00' AS start_time, 'Diego Nunes' AS client_name, 'Casa 05' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-16' AS booking_date, TIME '11:00:00' AS start_time, 'Sandra Freitas' AS client_name, 'Apto 114' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-16' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-16' AS booking_date, TIME '16:00:00' AS start_time, 'Joao Lemos' AS client_name, 'Apto 121' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-16' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-17' AS booking_date, TIME '09:00:00' AS start_time, 'Joao Silveira' AS client_name, 'Apto 110' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-17' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-17' AS booking_date, TIME '11:00:00' AS start_time, 'Marina Mattos' AS client_name, 'Apto 117' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-17' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-18' AS booking_date, TIME '09:00:00' AS start_time, 'Eduardo Ramos' AS client_name, 'Apto 113' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-18' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-19' AS booking_date, TIME '09:00:00' AS start_time, 'Camila Araujo' AS client_name, 'Apto 116' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-04-19' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-19' AS booking_date, TIME '11:00:00' AS start_time, 'Marco Castro' AS client_name, 'Apto 123' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-19' AS booking_date, TIME '16:00:00' AS start_time, 'Beatriz Duarte' AS client_name, 'Apto 107' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-19' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-20' AS booking_date, TIME '09:00:00' AS start_time, 'Beatriz Nunes' AS client_name, 'Apto 119' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-20' AS booking_date, TIME '11:00:00' AS start_time, 'Ricardo Freitas' AS client_name, 'Apto 103' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-20' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-21' AS booking_date, TIME '09:00:00' AS start_time, 'Patricia Silveira' AS client_name, 'Apto 122' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-21' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-22' AS booking_date, TIME '09:00:00' AS start_time, 'Diego Ramos' AS client_name, 'Apto 102' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-04-22' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-22' AS booking_date, TIME '11:00:00' AS start_time, 'Sandra Rocha' AS client_name, 'Apto 109' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-22' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-22' AS booking_date, TIME '16:00:00' AS start_time, 'Joao Teles' AS client_name, 'Casa 01' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-23' AS booking_date, TIME '09:00:00' AS start_time, 'Joao Araujo' AS client_name, 'Apto 105' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-23' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-23' AS booking_date, TIME '11:00:00' AS start_time, 'Marina Castro' AS client_name, 'Casa 02' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-24' AS booking_date, TIME '09:00:00' AS start_time, 'Eduardo Nunes' AS client_name, 'Casa 03' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-25' AS booking_date, TIME '09:00:00' AS start_time, 'Camila Silveira' AS client_name, 'Apto 111' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-04-25' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-25' AS booking_date, TIME '11:00:00' AS start_time, 'Marco Mattos' AS client_name, 'Apto 118' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-25' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-25' AS booking_date, TIME '16:00:00' AS start_time, 'Beatriz Braga' AS client_name, 'Apto 102' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-25' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-26' AS booking_date, TIME '09:00:00' AS start_time, 'Beatriz Ramos' AS client_name, 'Apto 114' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-26' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-26' AS booking_date, TIME '11:00:00' AS start_time, 'Ricardo Rocha' AS client_name, 'Apto 121' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-26' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-27' AS booking_date, TIME '09:00:00' AS start_time, 'Patricia Araujo' AS client_name, 'Apto 117' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-27' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-28' AS booking_date, TIME '09:00:00' AS start_time, 'Diego Nunes' AS client_name, 'Apto 120' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-28' AS booking_date, TIME '11:00:00' AS start_time, 'Sandra Freitas' AS client_name, 'Apto 104' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-28' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-28' AS booking_date, TIME '16:00:00' AS start_time, 'Joao Lemos' AS client_name, 'Apto 111' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-28' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-29' AS booking_date, TIME '09:00:00' AS start_time, 'Joao Silveira' AS client_name, 'Apto 123' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-04-29' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-29' AS booking_date, TIME '11:00:00' AS start_time, 'Marina Mattos' AS client_name, 'Apto 107' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-29' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-04-30' AS booking_date, TIME '09:00:00' AS start_time, 'Eduardo Ramos' AS client_name, 'Apto 103' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-04-30' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-01' AS booking_date, TIME '09:00:00' AS start_time, 'Camila Araujo' AS client_name, 'Apto 106' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-05-01' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-01' AS booking_date, TIME '11:00:00' AS start_time, 'Marco Castro' AS client_name, 'Casa 09' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-01' AS booking_date, TIME '16:00:00' AS start_time, 'Beatriz Duarte' AS client_name, 'Apto 120' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-01' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-02' AS booking_date, TIME '09:00:00' AS start_time, 'Beatriz Nunes' AS client_name, 'Casa 01' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-02' AS booking_date, TIME '11:00:00' AS start_time, 'Ricardo Freitas' AS client_name, 'Apto 116' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-02' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-03' AS booking_date, TIME '09:00:00' AS start_time, 'Patricia Silveira' AS client_name, 'Apto 112' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-03' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-04' AS booking_date, TIME '09:00:00' AS start_time, 'Diego Ramos' AS client_name, 'Apto 115' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-05-04' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-04' AS booking_date, TIME '11:00:00' AS start_time, 'Sandra Rocha' AS client_name, 'Apto 122' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-04' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-04' AS booking_date, TIME '16:00:00' AS start_time, 'Joao Teles' AS client_name, 'Apto 106' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-05' AS booking_date, TIME '09:00:00' AS start_time, 'Joao Araujo' AS client_name, 'Apto 118' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-05' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-05' AS booking_date, TIME '11:00:00' AS start_time, 'Marina Castro' AS client_name, 'Apto 102' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-06' AS booking_date, TIME '09:00:00' AS start_time, 'Eduardo Nunes' AS client_name, 'Apto 121' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-07' AS booking_date, TIME '09:00:00' AS start_time, 'Camila Silveira' AS client_name, 'Apto 101' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-05-07' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-07' AS booking_date, TIME '11:00:00' AS start_time, 'Marco Mattos' AS client_name, 'Apto 108' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-07' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-07' AS booking_date, TIME '16:00:00' AS start_time, 'Beatriz Braga' AS client_name, 'Apto 115' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-07' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-08' AS booking_date, TIME '09:00:00' AS start_time, 'Beatriz Ramos' AS client_name, 'Apto 104' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-08' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-08' AS booking_date, TIME '11:00:00' AS start_time, 'Ricardo Rocha' AS client_name, 'Apto 111' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-08' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-09' AS booking_date, TIME '09:00:00' AS start_time, 'Patricia Araujo' AS client_name, 'Apto 107' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-09' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-10' AS booking_date, TIME '09:00:00' AS start_time, 'Diego Nunes' AS client_name, 'Casa 08' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-10' AS booking_date, TIME '11:00:00' AS start_time, 'Sandra Freitas' AS client_name, 'Apto 117' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-10' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-10' AS booking_date, TIME '16:00:00' AS start_time, 'Joao Lemos' AS client_name, 'Apto 101' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-10' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-11' AS booking_date, TIME '09:00:00' AS start_time, 'Joao Silveira' AS client_name, 'Apto 113' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-11' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-11' AS booking_date, TIME '11:00:00' AS start_time, 'Marina Mattos' AS client_name, 'Apto 120' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-11' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-12' AS booking_date, TIME '09:00:00' AS start_time, 'Eduardo Ramos' AS client_name, 'Apto 116' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-12' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-13' AS booking_date, TIME '09:00:00' AS start_time, 'Camila Araujo' AS client_name, 'Apto 119' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-05-13' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-13' AS booking_date, TIME '11:00:00' AS start_time, 'Marco Castro' AS client_name, 'Apto 103' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-13' AS booking_date, TIME '16:00:00' AS start_time, 'Beatriz Duarte' AS client_name, 'Apto 110' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-13' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-14' AS booking_date, TIME '09:00:00' AS start_time, 'Beatriz Nunes' AS client_name, 'Apto 122' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-14' AS booking_date, TIME '11:00:00' AS start_time, 'Ricardo Freitas' AS client_name, 'Apto 106' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-14' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-15' AS booking_date, TIME '09:00:00' AS start_time, 'Patricia Silveira' AS client_name, 'Apto 102' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-15' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-16' AS booking_date, TIME '09:00:00' AS start_time, 'Diego Ramos' AS client_name, 'Apto 105' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-05-16' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-16' AS booking_date, TIME '11:00:00' AS start_time, 'Sandra Rocha' AS client_name, 'Apto 112' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-16' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-16' AS booking_date, TIME '16:00:00' AS start_time, 'Joao Teles' AS client_name, 'Casa 04' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-17' AS booking_date, TIME '09:00:00' AS start_time, 'Joao Araujo' AS client_name, 'Apto 108' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-17' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-17' AS booking_date, TIME '11:00:00' AS start_time, 'Marina Castro' AS client_name, 'Casa 05' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-18' AS booking_date, TIME '09:00:00' AS start_time, 'Eduardo Nunes' AS client_name, 'Casa 06' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-19' AS booking_date, TIME '09:00:00' AS start_time, 'Camila Silveira' AS client_name, 'Apto 114' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-05-19' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-19' AS booking_date, TIME '11:00:00' AS start_time, 'Marco Mattos' AS client_name, 'Apto 121' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-19' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-19' AS booking_date, TIME '16:00:00' AS start_time, 'Beatriz Braga' AS client_name, 'Apto 105' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-19' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-20' AS booking_date, TIME '09:00:00' AS start_time, 'Beatriz Ramos' AS client_name, 'Apto 117' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-20' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-20' AS booking_date, TIME '11:00:00' AS start_time, 'Ricardo Rocha' AS client_name, 'Apto 101' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-20' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-21' AS booking_date, TIME '09:00:00' AS start_time, 'Patricia Araujo' AS client_name, 'Apto 120' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-21' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-22' AS booking_date, TIME '09:00:00' AS start_time, 'Diego Nunes' AS client_name, 'Apto 123' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-22' AS booking_date, TIME '11:00:00' AS start_time, 'Sandra Freitas' AS client_name, 'Apto 107' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-22' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-22' AS booking_date, TIME '16:00:00' AS start_time, 'Joao Lemos' AS client_name, 'Apto 114' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-22' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-23' AS booking_date, TIME '09:00:00' AS start_time, 'Joao Silveira' AS client_name, 'Apto 103' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-23' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-23' AS booking_date, TIME '11:00:00' AS start_time, 'Marina Mattos' AS client_name, 'Apto 110' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-23' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-24' AS booking_date, TIME '09:00:00' AS start_time, 'Eduardo Ramos' AS client_name, 'Apto 106' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-24' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-25' AS booking_date, TIME '09:00:00' AS start_time, 'Camila Araujo' AS client_name, 'Apto 109' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-05-25' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-25' AS booking_date, TIME '11:00:00' AS start_time, 'Marco Castro' AS client_name, 'Casa 03' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-25' AS booking_date, TIME '16:00:00' AS start_time, 'Beatriz Duarte' AS client_name, 'Apto 123' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-25' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-26' AS booking_date, TIME '09:00:00' AS start_time, 'Beatriz Nunes' AS client_name, 'Casa 04' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-26' AS booking_date, TIME '11:00:00' AS start_time, 'Ricardo Freitas' AS client_name, 'Apto 119' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-26' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-27' AS booking_date, TIME '09:00:00' AS start_time, 'Patricia Silveira' AS client_name, 'Apto 115' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-27' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-28' AS booking_date, TIME '09:00:00' AS start_time, 'Diego Ramos' AS client_name, 'Apto 118' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-05-28' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-28' AS booking_date, TIME '11:00:00' AS start_time, 'Sandra Rocha' AS client_name, 'Apto 102' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-28' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-28' AS booking_date, TIME '16:00:00' AS start_time, 'Joao Teles' AS client_name, 'Apto 109' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-29' AS booking_date, TIME '09:00:00' AS start_time, 'Joao Araujo' AS client_name, 'Apto 121' AS guest_reference, 'Aromaterapia' AS treatment, CAST(190.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-29' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-29' AS booking_date, TIME '11:00:00' AS start_time, 'Marina Castro' AS client_name, 'Apto 105' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-30' AS booking_date, TIME '09:00:00' AS start_time, 'Eduardo Nunes' AS client_name, 'Apto 101' AS guest_reference, 'Relaxante' AS treatment, CAST(160.00 AS DECIMAL(10, 2)) AS amount, 'Danuska' AS provider_name, 'Danuska' AS therapist_name, FALSE AS paid, NULL AS payment_method, NULL AS payment_date, NULL AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-31' AS booking_date, TIME '09:00:00' AS start_time, 'Camila Silveira' AS client_name, 'Apto 104' AS guest_reference, 'Drenagem' AS treatment, CAST(180.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'David' AS therapist_name, TRUE AS paid, 'CARD' AS payment_method, DATE '2026-05-31' AS payment_date, 'Pago com cartao no atendimento.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-31' AS booking_date, TIME '11:00:00' AS start_time, 'Marco Mattos' AS client_name, 'Apto 111' AS guest_reference, 'Pedras quentes' AS treatment, CAST(220.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Isabelita' AS therapist_name, TRUE AS paid, 'CASH' AS payment_method, DATE '2026-05-31' AS payment_date, 'Pago em dinheiro.' AS payment_notes
    UNION ALL
    SELECT DATE '2026-05-31' AS booking_date, TIME '16:00:00' AS start_time, 'Beatriz Braga' AS client_name, 'Apto 118' AS guest_reference, 'Desportiva' AS treatment, CAST(210.00 AS DECIMAL(10, 2)) AS amount, 'David' AS provider_name, 'Maria' AS therapist_name, TRUE AS paid, 'PIX' AS payment_method, DATE '2026-05-31' AS payment_date, 'Pago por pix na recepcao.' AS payment_notes
) seed
JOIN massage_providers mp
    ON mp.name = seed.provider_name
JOIN massage_therapists mt
    ON mt.provider_id = mp.id
   AND mt.name = seed.therapist_name;
