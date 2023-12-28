CREATE DATABASE IF NOT EXISTS catalogdb;

CREATE TABLE IF NOT EXISTS user (
    id BIGINT(20) AUTO_INCREMENT PRIMARY KEY,
    username varchar(255) UNIQUE,
    password varchar(255),
    email varchar(255) UNIQUE,
    city varchar(255),
    street varchar(255),
    zip varchar(255),
    enabled boolean,
    roles varchar(255),
    is_admin boolean
);

CREATE TABLE IF NOT EXISTS email_verification_token (
    id BIGINT(20) AUTO_INCREMENT PRIMARY KEY,
    expiry_date datetime,
    token varchar(255) UNIQUE,
    username varchar(255)
);

#DROP TABLE IF EXISTS hibernate_sequence;
#CREATE TABLE hibernate_sequence (
    #next_val BIGINT(20) NULL DEFAULT NULL
#)
