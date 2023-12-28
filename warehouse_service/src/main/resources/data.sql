# INSERTION OF TEN WAREHOUSES
DROP PROCEDURE IF EXISTS createWarehouses;
CREATE PROCEDURE createWarehouses()
BEGIN
    DECLARE var_warehouse_id int DEFAULT 1;
    DECLARE var_location VARCHAR(255) DEFAULT 'location1';
    DECLARE var_name VARCHAR(255) default CONCAT('w', var_warehouse_id);

    WHILE var_warehouse_id <= 10 DO
            INSERT INTO `warehouse` (`warehouse_id`, `location`, `name`)
            SELECT * FROM (
                SELECT
                  var_warehouse_id AS `warehouse_id`,
                  var_location AS `location`,
                  var_name AS `name`
                )   AS tmp
            WHERE NOT EXISTS (
                SELECT `warehouse_id` FROM `warehouse` WHERE `warehouse_id` = var_warehouse_id
            )   LIMIT 1;
            SET var_warehouse_id = var_warehouse_id + 1;
            SET var_location = CONCAT('location', var_warehouse_id);
            SET var_name = CONCAT('w', var_warehouse_id);
    END WHILE;
END;
CALL createWarehouses();

# INSERTION OF ONE HUNDRED PRODUCTS AND CHOOSE RANDOMLY SOME WAREHOUSES
DROP PROCEDURE IF EXISTS createProducts;
CREATE PROCEDURE createProducts()
BEGIN
    DECLARE var_product_id int DEFAULT 11;
    DECLARE var_category VARCHAR(255) DEFAULT 'CATEGORY1';
    DECLARE var_description VARCHAR(255) default CONCAT('desc', var_product_id);
    DECLARE var_name VARCHAR(255) default CONCAT('p', var_product_id);
    DECLARE var_price DECIMAL(19,2) default 1;

    DECLARE var_product_warehouse_id int DEFAULT 111;
    DECLARE var_w_counter int DEFAULT 1;
    DECLARE var_choose int DEFAULT 0;
    DECLARE var_quantity int DEFAULT 1;
    DECLARE var_alarm int DEFAULT 1;

    DECLARE var_count int DEFAULT 0;
    SELECT @var_count := COUNT(*) FROM `product_warehouse`;

    WHILE var_product_id <= 110 DO
        INSERT INTO `product` (`product_id`, `category`, `description`, `name`, `price`)
        SELECT * FROM (
            SELECT
                var_product_id AS `product_id`,
                var_category AS `category`,
                var_description AS `description`,
                var_name AS `name`,
                var_price AS `price`
            )   AS tmp
        WHERE NOT EXISTS (
            SELECT `product_id` FROM `product` WHERE `product_id` = var_product_id
        )   LIMIT 1;

        IF (var_count = 0) THEN
            SET var_w_counter = 1;
            WHILE var_w_counter <= 10 DO
                SET var_choose = FLOOR(RAND()*(2-1+1)+1); #Select a number between 1 and 2
                SET var_quantity = FLOOR(RAND()*(10-5+1)+5); #Select a number between 5 and 10
                SET var_alarm = FLOOR(RAND()*(3-1+1)+1); #Select a number between 1 and 3
                IF (var_choose = 1) THEN
                    INSERT INTO `product_warehouse` (`id`, alarm_on_quantity, `quantity`, `product_id`, `warehouse_id`)
                    SELECT * FROM (
                        SELECT
                            var_product_warehouse_id AS `id`,
                            var_alarm AS `alarm_on_quantity`,
                            var_quantity AS `quantity`,
                            var_product_id AS `product_id`,
                            var_w_counter AS `warehouse_id`
                    )   AS tmp
                    WHERE NOT EXISTS (
                        SELECT `id` FROM `product_warehouse` WHERE `id` = var_product_warehouse_id
                    )   LIMIT 1;
                    SET var_product_warehouse_id = var_product_warehouse_id + 1;
                END IF;
                SET var_w_counter = var_w_counter + 1;
            END WHILE;
        END IF;

        SET var_product_id = var_product_id + 1;
        SET var_description = CONCAT('desc', var_product_id);
        SET var_name = CONCAT('p', var_product_id);
        SET var_price = FLOOR(RAND()*(100-1) + 1);
        SET var_category = IF (var_product_id <= 61, 'CATEGORY1', 'CATEGORY2');
    END WHILE;
END;
CALL createProducts();

INSERT INTO `hibernate_sequence` (next_val)
SELECT * FROM (
    SELECT
        MAX(id) + 1 FROM product_warehouse AS next_va
    )   AS tmp
WHERE NOT EXISTS (
    SELECT COUNT(*) FROM `hibernate_sequence`
    HAVING COUNT(*) = 1
)   LIMIT 1;
