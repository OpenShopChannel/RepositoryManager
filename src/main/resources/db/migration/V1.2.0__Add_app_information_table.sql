/*
 * Copyright (c) 2023-2024 Open Shop Channel
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

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