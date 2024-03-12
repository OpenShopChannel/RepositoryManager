package org.oscwii.repositorymanager;

import com.google.gson.Gson;
import org.oscwii.repositorymanager.model.app.Category;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.model.app.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryIndex
{
    private final List<Category> categories;
    private final List<OSCMeta> contents;
    private final List<Platform> platforms;

    private final String repositoryName, repositoryProvider, repositoryDescription;

    @Autowired
    private Gson gson;

    public RepositoryIndex()
    {

    }
}
