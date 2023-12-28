USE catalogdb;
 
INSERT INTO `user` (id, username, password, email, city, street, zip, enabled, roles, is_admin)
SELECT * FROM (
    SELECT
        1 AS id,
        'COMPANY' AS username,
        '$2y$12$eod5nYm5oQNgCD6xQqOGuOqd2fDAROsBZHyNJvLaXl4ONwkSB3Iri' AS password, /* password = ciaomondo */
        'wa2greateight@gmail.com' as email,
        'Torino' as city,
        'Corso Duca degli Abruzzi' as street,
        '10129' as zip,
        true as enabled,
        'ADMIN' as roles,
        true as is_admin
    )   AS tmp
WHERE NOT EXISTS (
    SELECT username FROM user WHERE id = 1
)   LIMIT 1;

INSERT INTO `user` (id, username, password, email, city, street, zip, enabled, roles, is_admin)
SELECT * FROM (
    SELECT
        2 AS id,
        'JohnDoe' AS username,
        '$2y$12$k.9TIXfLuDrdvTxjuwAgq.PIT9v98nfV9NDxyFrXooRb5We7FL08G' AS password, /* password = password */
        'email1@email.com' as email,
        'Milano' as city,
        'Via Camillo Golgi 20' as street,
        '20133' as zip,
        true as enabled,
        'CUSTOMER' as roles,
        false as is_admin
    )   AS tmp
WHERE NOT EXISTS (
    SELECT username FROM user WHERE id = 2
)   LIMIT 1;


INSERT INTO `user` (id, username, password, email, city, street, zip, enabled, roles, is_admin)
SELECT * FROM (
    SELECT
        3 AS id,
        'TaleDeiTali' AS username,
        '$2y$12$ZHX3/obBUJ6K1WHbtit28.GadoLmdtKXbnRhGw4qFfvGbzEcNvi0O' AS password, /* password = ottootto */
        'email2@email.com' as email,
        'Torino' as city,
        'Lungo Dora Siena, 104' as street,
        '10129' as zip,
        false as enabled,
        'CUSTOMER' as roles,
        false as is_admin
    )   AS tmp
WHERE NOT EXISTS (
    SELECT username FROM user WHERE id = 3
)   LIMIT 1;
