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
