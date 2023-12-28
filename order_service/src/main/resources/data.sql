INSERT INTO `hibernate_sequence` (next_val)
SELECT * FROM (
    SELECT
        1 AS next_val
    )   AS tmp
WHERE NOT EXISTS (
    SELECT COUNT(*) FROM `hibernate_sequence`
    HAVING COUNT(*) = 1
)   LIMIT 1;
