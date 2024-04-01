package org.oscwii.repositorymanager.database.dao;

import org.jdbi.v3.spring5.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.ShopTitle;

import java.util.Optional;

@JdbiRepository
@RegisterConstructorMapper(ShopTitle.class)
public interface AppDAO
{
    @SqlQuery("SELECT 1 FROM app_information WHERE slug = :slug")
    Optional<Boolean> appExists(String slug);

    default void insertApp(InstalledApp app)
    {
        insertApp(app.getSlug(), app.getMetaXml().coder, app.getMetaXml().version);
    }

    @SqlUpdate("INSERT INTO app_information (slug, author, version) VALUES (:slug, :author, :version)")
    void insertApp(String slug, String author, String version);

    default void updateApp(InstalledApp app)
    {
        updateApp(app.getSlug(), app.getMetaXml().coder, app.getMetaXml().version);
    }

    @SqlUpdate("""
            UPDATE app_information
            SET author = :author,
                version = :version,
                last_index = CURRENT_TIMESTAMP
            WHERE slug = :slug
            """)
    void updateApp(String slug, String author, String version);

    @SqlQuery("SELECT 1 FROM shop_title_information WHERE title_id = :titleId")
    Optional<Boolean> isTIDInUse(String titleId);

    @SqlQuery("""
            SELECT a.app_slug,
                   a.title_id,
                   a.version
            FROM shop_title_information a
            WHERE a.app_slug = :slug
            """)
    ShopTitle getShopTitle(String slug);

    @GetGeneratedKeys({"app_slug", "version", "title_id"})
    @SqlUpdate("INSERT INTO shop_title_information (app_slug) VALUES (:slug)")
    ShopTitle insertShopTitle(String slug);

    @SqlUpdate("UPDATE shop_title_information SET title_id = :titleId WHERE app_slug = :slug")
    void insertTID(String slug, String titleId);

    @SqlUpdate("UPDATE shop_title_information SET version = :version WHERE app_slug = :slug")
    void setTitleVersion(String slug, int version);
}
