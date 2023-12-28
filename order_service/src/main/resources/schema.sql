CREATE DATABASE IF NOT EXISTS `orderdb`;

CREATE TABLE IF NOT EXISTS `orders` (
    `order_id` BIGINT(20) NOT NULL,
    `buyer_id` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    `status` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    `purchase_price` DOUBLE(22,0) NOT NULL,
    PRIMARY KEY (`order_id`) USING BTREE
);

CREATE TABLE IF NOT EXISTS `purchased_product` (
    `purchased_product_id` BIGINT(20) NOT NULL,
    `name` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    `order_id` BIGINT(20) NOT NULL,
    `price` DOUBLE(22,0) NOT NULL,
    `product_id` BIGINT(20) NOT NULL,
    `quantity` INT(11) NOT NULL,
    PRIMARY KEY (`purchased_product_id`) USING BTREE
);

CREATE TABLE IF NOT EXISTS `delivery` (
    `delivery_id` BIGINT(20) NOT NULL,
    `city` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    `order_id` BIGINT(20) NOT NULL,
    `street` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    `warehouse_id` BIGINT(20) NULL DEFAULT NULL,
    `zip` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    PRIMARY KEY (`delivery_id`) USING BTREE,
    UNIQUE INDEX `UK_3bdrbd2jcybaaa5rxkj4s7vlk` (`order_id`) USING BTREE
);

CREATE TABLE IF NOT EXISTS `order_outbox` (
    `id` BIGINT(20) NOT NULL,
    `order_id` BIGINT(20) NOT NULL,
    `order_saga_status` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    `to_wallet_msg` VARCHAR(500) NOT NULL COLLATE 'latin1_swedish_ci',
    `to_warehouse_msg` VARCHAR(5000) NOT NULL COLLATE 'latin1_swedish_ci',
    `wallet_saga_status` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    `warehouse_id` BIGINT(20) NULL DEFAULT NULL,
    `warehouse_saga_status` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `UK_lgvdk2lonxu7q6yssrey85uq` (`order_id`) USING BTREE
);

CREATE TABLE IF NOT EXISTS `hibernate_sequence` (
    `next_val` BIGINT(20) NULL DEFAULT NULL
);
