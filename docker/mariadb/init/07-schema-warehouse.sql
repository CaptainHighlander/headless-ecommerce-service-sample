use warehousedb;

CREATE TABLE IF NOT EXISTS `product` (
    `product_id` BIGINT(20) NOT NULL,
    `average_rating` DECIMAL(19,2) NOT NULL DEFAULT 0,
    `category` VARCHAR(255) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',
    `comments_number` INT(11) NOT NULL DEFAULT 0,
    `creation_date` DATETIME NOT NULL DEFAULT NOW(),
    `description` VARCHAR(255) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',
    `name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',
    `picture` LONGBLOB NULL DEFAULT NULL,
    `pictureurl` VARCHAR(255) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',
    `price` DECIMAL(19,2) NULL DEFAULT NULL,
    PRIMARY KEY (`product_id`) USING BTREE
);

CREATE TABLE IF NOT EXISTS `warehouse` (
    `warehouse_id` BIGINT(20) NOT NULL,
    `location` VARCHAR(255) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',
    `name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',
    PRIMARY KEY (`warehouse_id`) USING BTREE
);

CREATE TABLE IF NOT EXISTS `comment` (
    `comment_id` BIGINT(20) NOT NULL,
    `body` VARCHAR(255) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',
    `creation_date` DATETIME NULL DEFAULT NULL,
    `stars` INT(11) NOT NULL,
    `title` VARCHAR(255) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',
    `product` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`comment_id`) USING BTREE,
    INDEX `FKoydhe9m3ml5p14fij0hevoisx` (`product`) USING BTREE,
    CONSTRAINT `FKoydhe9m3ml5p14fij0hevoisx` FOREIGN KEY (`product`) REFERENCES `warehousedb`.`product` (`product_id`) ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS `product_warehouse` (
    `id` BIGINT(20) NOT NULL,
    `alarm_on_quantity` INT(11) NOT NULL DEFAULT 1,
    `quantity` INT(11) NOT NULL,
    `product_id` BIGINT(20) NULL DEFAULT NULL,
    `warehouse_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `FKi45e6tbd5k7pu4g7walf5d3rc` (`product_id`) USING BTREE,
    INDEX `FKma5wwcpvatmrf7m725c451sbg` (`warehouse_id`) USING BTREE,
    CONSTRAINT `FKi45e6tbd5k7pu4g7walf5d3rc` FOREIGN KEY (`product_id`) REFERENCES `warehousedb`.`product` (`product_id`) ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `FKma5wwcpvatmrf7m725c451sbg` FOREIGN KEY (`warehouse_id`) REFERENCES `warehousedb`.`warehouse` (`warehouse_id`) ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS `warehouse_outbox` (
    `id` BIGINT(20) NOT NULL,
    `order_id` BIGINT(20) NOT NULL,
    `to_order_msg` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    `warehouse_id` BIGINT(20) NULL DEFAULT NULL,
    `warehouse_saga_status` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `UK_8aw2e3vqffi4b7wohsrfhv2ye` (`order_id`) USING BTREE
);

CREATE TABLE IF NOT EXISTS `hibernate_sequence` (
    `next_val` BIGINT(20) NULL DEFAULT NULL
);
