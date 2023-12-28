USE walletdb;

INSERT INTO `customer` (id, username)
SELECT * FROM (
    SELECT
        1 AS id,
        'COMPANY' AS username
    )   AS tmp
WHERE NOT EXISTS (
    SELECT username FROM `customer` WHERE id = 1
)   LIMIT 1;

INSERT INTO `wallet` (id, current_amount, customer_id)
SELECT * FROM (
    SELECT
        2 AS id,
        1000000000.99 AS current_amount,
        1 AS customer_id
    )   AS tmp
WHERE NOT EXISTS (
    SELECT id FROM `wallet` WHERE id = 2
)   LIMIT 1;

INSERT INTO `customer_wallet_list` (customer_id, wallet_list_id)
SELECT * FROM (
    SELECT
          1 AS customer_id,
          2 AS wallet_list_id
    )   AS tmp
WHERE NOT EXISTS (
    SELECT * FROM `customer_wallet_list` WHERE customer_id = 1 AND wallet_list_id = 2
)   LIMIT 1;


INSERT INTO `hibernate_sequence` (next_val)
SELECT * FROM (
    SELECT
        3 AS next_val
    )   AS tmp
WHERE NOT EXISTS (
    SELECT COUNT(*) FROM `hibernate_sequence`
    HAVING COUNT(*) = 1
)   LIMIT 1;
