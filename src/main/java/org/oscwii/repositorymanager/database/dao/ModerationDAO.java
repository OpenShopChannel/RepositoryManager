package org.oscwii.repositorymanager.database.dao;

import org.jdbi.v3.spring5.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.oscwii.repositorymanager.model.ModeratedBinary;
import org.springframework.stereotype.Service;

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
    ModeratedBinary findByChecksum(String checksum);
}