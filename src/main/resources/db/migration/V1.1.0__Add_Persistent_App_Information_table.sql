CREATE TABLE persistent_app_information
(
    `app_slug` VARCHAR(255) NOT NULL
        CONSTRAINT persistent_app_information_pkey
            PRIMARY KEY,
    `title_id` VARCHAR(255)
);