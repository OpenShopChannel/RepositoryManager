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

package org.oscwii.repositorymanager.database.dao;

import org.jdbi.v3.spring5.JdbiRepository;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Map;
import java.util.Optional;

@JdbiRepository
public interface SettingsDAO
{
    @KeyColumn("key")
    @ValueColumn("value")
    @SqlQuery("SELECT * FROM settings")
    Map<String, String> getAllSettings();

    @SqlQuery("SELECT `value` FROM settings WHERE `key` = :key")
    Optional<String> getSetting(String key);

    @SqlUpdate("INSERT INTO settings VALUES (:key, :value) ON DUPLICATE KEY UPDATE `value` = :value")
    void insertSetting(String key, String value);
}
