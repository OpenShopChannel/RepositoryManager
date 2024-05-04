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

package org.oscwii.repositorymanager.model;

import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.LocalDateTime;

public record ModeratedBinary(String checksum, @ColumnName("app_slug") String app,
                              Status status, LocalDateTime discoveryDate,
                              LocalDateTime modifiedDate, @ColumnName("moderated_by") int moderator)
{
    @JdbiConstructor
    public ModeratedBinary {}

    public ModeratedBinary(String checksum, String app)
    {
        this(checksum, app, Status.PENDING, LocalDateTime.now(), LocalDateTime.now(), 0);
    }

    public enum Status
    {
        PENDING,
        APPROVED,
        REJECTED
    }
}
