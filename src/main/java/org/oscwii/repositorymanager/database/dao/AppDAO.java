/*
 * Copyright (c) 2023-2025 Open Shop Channel
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
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.ShopTitle;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Service
@JdbiRepository
@RegisterConstructorMapper(ShopTitle.class)
public interface AppDAO
{
    @SqlQuery("SELECT 1 FROM app_information WHERE slug = :slug")
    Optional<Boolean> appExists(String slug);

    @SqlQuery("SELECT downloads FROM app_information WHERE slug = :slug")
    int getDownloads(String slug);

    default void setDownloads(InstalledApp app)
    {
        setDownloads(app.getSlug(), app.getDownloads());
    }

    @SqlUpdate("UPDATE app_information SET downloads = :downloads WHERE slug = :slug")
    void setDownloads(String slug, int downloads);

    default void insertApp(InstalledApp app)
    {
        Timestamp releaseDate = Timestamp.from(Instant.ofEpochSecond(app.getComputedInfo().releaseDate));
        insertApp(app.getSlug(), app.getMeta().author(), app.getMetaXml().version, releaseDate);
    }

    @SqlUpdate("""
            INSERT INTO app_information (slug, author, version, last_update)
            VALUES (:slug, :author, :version, :releaseDate)
            """)
    void insertApp(String slug, String author, String version, Timestamp releaseDate);

    default void updateApp(InstalledApp app)
    {
        Timestamp releaseDate = Timestamp.from(Instant.ofEpochSecond(app.getComputedInfo().releaseDate));
        updateApp(app.getSlug(), app.getMeta().author(), app.getMetaXml().version, releaseDate);
    }

    @SqlUpdate("""
            UPDATE app_information
            SET author = :author,
                version = :version,
                last_index = CURRENT_TIMESTAMP,
                last_update = :releaseDate
            WHERE slug = :slug
            """)
    void updateApp(String slug, String author, String version, Timestamp releaseDate);

    @SqlQuery("SELECT last_update FROM app_information WHERE slug = :slug")
    Timestamp getReleaseDate(String slug);

    @SqlQuery("SELECT first_index FROM app_information WHERE slug = :slug")
    Timestamp getFirstIndex(String slug);

    @SqlQuery("SELECT last_index FROM app_information WHERE slug = :slug")
    Timestamp getLastIndex(String slug);

    default void setReleaseDate(String slug, int lastUpdate)
    {
        setReleaseDate(slug, Timestamp.from(Instant.ofEpochSecond(lastUpdate)));
    }

    @SqlUpdate("UPDATE app_information SET last_update = :lastUpdate WHERE slug = :slug")
    void setReleaseDate(String slug, Timestamp lastUpdate);

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
