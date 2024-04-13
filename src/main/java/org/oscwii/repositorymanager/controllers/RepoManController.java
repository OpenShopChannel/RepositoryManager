package org.oscwii.repositorymanager.controllers;

import org.oscwii.repositorymanager.RepositoryIndex;
import org.oscwii.repositorymanager.security.Anyone;
import org.springframework.beans.factory.annotation.Autowired;

@Anyone
public abstract class RepoManController
{
    @Autowired
    protected RepositoryIndex index;
}
