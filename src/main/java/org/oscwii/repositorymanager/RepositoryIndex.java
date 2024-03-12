package org.oscwii.repositorymanager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.config.RepoManConfig;
import org.oscwii.repositorymanager.model.RepositoryInfo;
import org.oscwii.repositorymanager.model.app.Category;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.model.app.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Service
public class RepositoryIndex
{
    private final Logger logger;

    private List<Category> categories;
    private List<OSCMeta> contents;
    private List<Platform> platforms;
    private RepositoryInfo info;

    @Autowired
    private Gson gson;
    @Autowired
    private RepoManConfig config;

    public RepositoryIndex()
    {
        this.logger = LogManager.getLogger("Repository");

        this.info = RepositoryInfo.DEFAULT;
        this.categories = new ArrayList<>();
        this.contents = new ArrayList<>();
        this.platforms = new ArrayList<>();
    }

    public void update()
    {
        logger.info("Updating repository index");
        // TODO discord log

        // Load repository info
        loadRepositoryInfo();

        // Index categories and platforms
        indexCategories();
        indexPlatforms();


    }

    private void loadRepositoryInfo()
    {
        File file = config.getRepoDir().resolve("repository.json").toFile();
        try(Reader reader = new FileReader(file))
        {
            this.info = gson.fromJson(reader, RepositoryInfo.class);
        }
        catch(Exception ignored)
        {
            logger.warn("Unable to load repository info! Has the repository been initialized yet?");
            logger.warn("Falling back to no-op empty repository.");
        }
    }

    private void indexCategories()
    {
        File file = config.getRepoDir().resolve("categories.json").toFile();
        try(Reader reader = new FileReader(file))
        {
            //noinspection Convert2Diamond
            this.categories = gson.fromJson(reader, new TypeToken<List<Category>>(){});
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to load categories", e);
        }
    }

    private void indexContents()
    {

    }

    private void indexPlatforms()
    {
        File file = config.getRepoDir().resolve("platforms.json").toFile();
        try(Reader reader = new FileReader(file))
        {
            //noinspection Convert2Diamond
            this.platforms = gson.fromJson(reader, new TypeToken<List<Platform>>(){});
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to load platforms", e);
        }
    }
}
