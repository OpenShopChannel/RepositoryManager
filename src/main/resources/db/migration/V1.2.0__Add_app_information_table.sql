ALTER TABLE persistent_app_information RENAME TO shop_title_information;

CREATE TABLE app_information
(
    `slug`        VARCHAR(255) NOT NULL
        CONSTRAINT app_information_pk
            PRIMARY KEY,
    `author`      VARCHAR(255)          DEFAULT NULL,
    `version`     VARCHAR(30)           DEFAULT NULL,
    `first_index` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_index`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO app_information (`slug`)
    SELECT `app_slug` FROM shop_title_information;

ALTER TABLE shop_title_information
    ADD CONSTRAINT shop_title_information_app_information_fk
        FOREIGN KEY (`app_slug`) REFERENCES app_information (`slug`)
            ON DELETE CASCADE;

ALTER TABLE moderated_binaries
    ADD CONSTRAINT moderated_binaries_app_information_fk
        FOREIGN KEY (`app_slug`) REFERENCES app_information (`slug`)
            ON DELETE CASCADE;