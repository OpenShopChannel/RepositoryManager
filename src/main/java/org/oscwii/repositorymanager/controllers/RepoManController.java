package org.oscwii.repositorymanager.controllers;

import org.oscwii.repositorymanager.RepositoryIndex;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class RepoManController
{
    @Autowired
    protected RepositoryIndex index;
}
