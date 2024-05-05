CREATE TYPE app_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

CREATE TABLE settings
(
    `key`   VARCHAR(255) NOT NULL
        CONSTRAINT settings_pk
            PRIMARY KEY,
    `value` VARCHAR(255)
);

CREATE TABLE users
(
    `id`            INT NOT NULL AUTO_INCREMENT
        CONSTRAINT users_pk
            PRIMARY KEY,
    `email`         VARCHAR(80)
        CONSTRAINT users_email_key
            UNIQUE,
    `username`      VARCHAR(20)
        CONSTRAINT users_username_key
            UNIQUE,
    `password_hash` VARCHAR(255)
);

CREATE TABLE moderated_binaries
(
    `checksum`       VARCHAR(255) NOT NULL
        CONSTRAINT moderated_binaries_pk
            PRIMARY KEY,
    `app_slug`       VARCHAR(255) NOT NULL,
    `status`         app_status DEFAULT 'PENDING',
    `discovery_date` DATETIME     NOT NULL,
    `modified_date`  DATETIME     NOT NULL,
    `moderated_by`   INT
        CONSTRAINT moderated_binaries_users_fk
            REFERENCES users (`id`)
);