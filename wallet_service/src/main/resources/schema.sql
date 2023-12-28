CREATE DATABASE IF NOT EXISTS `walletdb`;

CREATE TABLE IF NOT EXISTS `customer` (
    `id` BIGINT(20) NOT NULL,
    `username` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `UK_irnrrncatp2fvw52vp45j7rlw` (`username`) USING BTREE
);

CREATE TABLE IF NOT EXISTS `wallet` (
    `id` BIGINT(20) NOT NULL,
    `current_amount` DECIMAL(19,2) NOT NULL,
    `customer_id` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `FKpb5ltxtks766lq2b9hgvnr2bq` (`customer_id`) USING BTREE,
    CONSTRAINT `FKpb5ltxtks766lq2b9hgvnr2bq` FOREIGN KEY (`customer_id`) REFERENCES `walletdb`.`customer` (`id`) ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS `transaction` (
    `id` BIGINT(20) NOT NULL,
    `amount` DECIMAL(19,2) NOT NULL,
    `reason` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    `time_instant` DATETIME NOT NULL,
    `beneficiary_wallet` BIGINT(20) NULL DEFAULT NULL,
    `payer_wallet` BIGINT(20) NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `FKtet11tv36kx4lj490ulq5i6i7` (`beneficiary_wallet`) USING BTREE,
    INDEX `FKqcjme73cro20nve9al9qxwa9r` (`payer_wallet`) USING BTREE,
    CONSTRAINT `FKqcjme73cro20nve9al9qxwa9r` FOREIGN KEY (`payer_wallet`) REFERENCES `walletdb`.`wallet` (`id`) ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `FKtet11tv36kx4lj490ulq5i6i7` FOREIGN KEY (`beneficiary_wallet`) REFERENCES `walletdb`.`wallet` (`id`) ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS `customer_wallet_list` (
    `customer_id` BIGINT(20) NOT NULL,
    `wallet_list_id` BIGINT(20) NOT NULL,
    PRIMARY KEY (`customer_id`, `wallet_list_id`) USING BTREE,
    UNIQUE INDEX `UK_lstpwrwueuesaq3d85r3q71sa` (`wallet_list_id`) USING BTREE,
    CONSTRAINT `FK7uuts3k6i52sdln4vr0h882ty` FOREIGN KEY (`customer_id`) REFERENCES `walletdb`.`customer` (`id`) ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `FKjgt12xyamfsg0qqorjv8gh2g9` FOREIGN KEY (`wallet_list_id`) REFERENCES `walletdb`.`wallet` (`id`) ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS `wallet_outbox` (
    `id` BIGINT(20) NOT NULL,
    `order_id` BIGINT(20) NOT NULL,
    `to_order_msg` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    `wallet_id` BIGINT(20) NULL DEFAULT NULL,
    `wallet_saga_status` VARCHAR(255) NOT NULL COLLATE 'latin1_swedish_ci',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `UK_a8khtnlmhe34g47s2dpphcsi0` (`order_id`) USING BTREE
);

CREATE TABLE IF NOT EXISTS `hibernate_sequence` (
    `next_val` BIGINT(20) NULL DEFAULT NULL
);
