package org.oscwii.repositorymanager.database.dao;

import org.jdbi.v3.spring5.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.oscwii.repositorymanager.model.app.PersistentAppInfo;

import java.util.Optional;

@JdbiRepository
@RegisterConstructorMapper(PersistentAppInfo.class)
public interface AppDAO
{
    @SqlQuery("SELECT 1 FROM persistent_app_information WHERE title_id = :titleId")
    Optional<Boolean> isTIDInUse(String titleId);

    @SqlQuery("""
            SELECT a.app_slug,
                   a.title_id,
                   a.version
            FROM persistent_app_information a
            WHERE a.app_slug = :slug
            """)
    PersistentAppInfo getBySlug(String slug);

    @GetGeneratedKeys({"app_slug", "version", "title_id"})
    @SqlUpdate("INSERT INTO persistent_app_information (app_slug) VALUES (:slug)")
    PersistentAppInfo insertApp(String slug);

    @SqlUpdate("UPDATE persistent_app_information SET title_id = :titleId WHERE app_slug = :slug")
    void insertTID(String slug, String titleId);
}
