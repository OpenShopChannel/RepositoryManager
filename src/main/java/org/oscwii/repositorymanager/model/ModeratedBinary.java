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
