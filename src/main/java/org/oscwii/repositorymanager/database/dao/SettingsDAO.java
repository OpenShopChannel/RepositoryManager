package org.oscwii.repositorymanager.database.dao;

import org.jdbi.v3.spring5.JdbiRepository;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

@JdbiRepository
public interface SettingsDAO
{
    @SqlQuery("SELECT `value` FROM settings WHERE `key` = :key")
    Optional<String> getSetting(String key);

    @SqlUpdate("INSERT INTO settings VALUES (:key, :value) ON DUPLICATE KEY UPDATE `value` = :value")
    void insertSetting(String key, String value);
}
