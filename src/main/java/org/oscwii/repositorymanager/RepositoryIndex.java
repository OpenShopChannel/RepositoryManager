package org.oscwii.repositorymanager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.config.RepoManConfig;
import org.oscwii.repositorymanager.model.RepositoryInfo;
import org.oscwii.repositorymanager.model.app.Category;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.model.app.Peripheral;
import org.oscwii.repositorymanager.model.app.Platform;
import org.oscwii.repositorymanager.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class RepositoryIndex
{
    private final Logger logger;

    private List<Category> categories;
    private List<InstalledApp> contents;
    private Map<String, Platform> platforms;
    private Platform defaultPlatform;
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
        this.platforms = new HashMap<>();
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
        this.info = FileUtil.loadJson(file, RepositoryInfo.class, gson, e ->
        {
            logger.warn("Unable to load repository info! Has the repository been initialized yet?");
            logger.warn("Falling back to no-op empty repository.");
            logger.warn(e);
        });
    }

    private void indexCategories()
    {
        File file = config.getRepoDir().resolve("categories.json").toFile();
        this.categories = FileUtil.loadJson(file, new TypeToken<List<Category>>(){},
                gson, e -> handleFatalException(e, "Failed to load categories:"));
    }

    private void indexPlatforms()
    {
        File file = config.getRepoDir().resolve("platforms.json").toFile();
        List<Platform> platformList = FileUtil.loadJson(file, new TypeToken<List<Platform>>(){}, gson,
                e -> handleFatalException(e, "Failed to load platforms:"));
        if(platformList == null)
            return;

        for(Platform platform : platformList)
            platforms.put(platform.name(), platform);

        this.defaultPlatform = platforms.get(config.getDefaultPlatform());
        Assert.notNull(defaultPlatform, "Unknown default platform: " + config.getDefaultPlatform());
    }

    private void indexContents()
    {
        AtomicInteger i = new AtomicInteger();
        Path folder = config.getRepoDir().resolve("contents");

        try(Stream<Path> walk = Files.walk(folder, 1))
        {
            walk.map(Path::toFile)
                    .filter(File::isFile)
                    .filter(file -> file.getName().endsWith(".oscmeta"))
                    .forEach(meta ->
                    {
                        logger.info("Loading manifest \"{}\" for processing ({}/{})",
                                meta, i.incrementAndGet(), walk.count());

                        try
                        {
                            processMeta(meta);
                        }
                        catch(Exception e)
                        {
                            logger.error("Failed to process oscmeta {}:", meta, e);
                        }
                    });
        }
        catch(IOException e)
        {
            throw new RuntimeException("Failed to access repository contents:", e);
        }

        logger.info("Finished indexing application manifests");
    }

    private void processMeta(File meta)
    {
        OSCMeta oscMeta = FileUtil.loadJson(meta, OSCMeta.class, gson,
                e -> handleFatalException(e, "Failed to process meta \"" + meta + "\":"));

        // this means the JSON parsing failed
        if(oscMeta == null)
            return;

        Category category = null;
        List<Platform> supportedPlatforms = new ArrayList<>();
        Map<Peripheral, Integer> peripherals = new EnumMap<>(Peripheral.class);
        String categoryRaw = oscMeta.category();

        // Parse the category
        for(Category registeredCategory : categories)
            if(categoryRaw.equalsIgnoreCase(registeredCategory.name()))
                category = registeredCategory;
        Assert.notNull(category, "Unknown app category: " + categoryRaw);

        // Parse peripherals
        Assert.notEmpty(oscMeta.peripherals(), "App supports zero peripherals. This is unsupported.");
        for(String peripheralRaw : oscMeta.peripherals())
        {
            Peripheral peripheral = Peripheral.fromDisplay(peripheralRaw);
            if(peripheral == Peripheral.UNKNOWN)
                logger.warn("  - Unknown peripheral: {}", peripheralRaw);
            peripherals.put(peripheral, peripherals.getOrDefault(peripheral, 0) + 1);
        }

        // Parse supported platforms
        for(String platformRaw : oscMeta.supportedPlatforms())
        {
            Platform platform = platforms.get(platformRaw);
            if(platform == null)
            {
                logger.warn("  - Unknown platform: {}", platformRaw);
                continue;
            }

            supportedPlatforms.add(platform);
        }

        if(supportedPlatforms.isEmpty())
        {
            logger.warn("  - Detected empty supported platforms, falling back to default.");
            supportedPlatforms.add(defaultPlatform);
        }

        InstalledApp app = new InstalledApp(meta.getName().replace(".oscmeta", ""), oscMeta,
                category, peripherals, supportedPlatforms);
    }

    private void handleFatalException(Exception e, String msg)
    {
        throw new RuntimeException(msg, e);
    }
}
