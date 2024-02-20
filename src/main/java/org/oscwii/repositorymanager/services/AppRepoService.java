package org.oscwii.repositorymanager.services;

import org.oscwii.repositorymanager.database.dao.ModerationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppRepoService
{
    private final ModerationDAO moderationDAO;

    @Autowired
    public AppRepoService(ModerationDAO moderationDAO)
    {
        this.moderationDAO = moderationDAO;
    }
}
