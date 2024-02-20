package org.oscwii.repositorymanager.model;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.LocalDateTime;

public record ModeratedBinary(String checksum, @ColumnName("app_slug") String app,
                              Status status, LocalDateTime discoveryDate,
                              LocalDateTime modifiedDate, @ColumnName("moderated_by") int moderator)
{
    public enum Status
    {
        PENDING,
        APPROVED,
        REJECTED
    }
}
