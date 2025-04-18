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
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.oscwii.repositorymanager.model.ModeratedBinary;
import org.oscwii.repositorymanager.model.ModeratedBinary.Status;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@JdbiRepository
@RegisterConstructorMapper(ModeratedBinary.class)
public interface ModerationDAO
{
    @SqlQuery("""
         SELECT m.checksum,
         m.app_slug,
         m.status,
         m.discovery_date,
         m.modified_date,
         m.moderated_by
         FROM moderated_binaries m WHERE m.checksum = ?1
    """)
    Optional<ModeratedBinary> findByChecksum(String checksum);

    @SqlUpdate("DELETE FROM moderated_binaries WHERE app_slug = :slug AND status = 'PENDING'")
    int removePendingEntries(String slug);

    default void insertEntry(ModeratedBinary entry)
    {
        insertEntry(entry.checksum(), entry.app(), entry.status());
    }

    @SqlUpdate("""
            INSERT INTO moderated_binaries
            VALUES (
                :checksum,
                :appSlug,
                :status,
                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                null)
            """)
    void insertEntry(String checksum, String appSlug, Status status);

    @SqlUpdate("""
            UPDATE moderated_binaries
            SET status = :status,
                moderated_by = :moderator,
                modified_date = CURRENT_TIMESTAMP
            WHERE checksum = :checksum
            """)
    void updateEntry(String checksum, Status status, int moderator);

    @SqlQuery("SELECT * FROM moderated_binaries ORDER BY modified_date DESC")
    List<ModeratedBinary> getAllEntries();

    @SqlQuery("SELECT COUNT(*) FROM moderated_binaries WHERE status = 'PENDING'")
    long getPendingEntries();
}
