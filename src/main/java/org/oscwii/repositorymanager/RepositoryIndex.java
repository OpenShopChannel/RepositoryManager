package org.oscwii.repositorymanager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.oscwii.repositorymanager.config.repoman.RepoManConfig;
import org.oscwii.repositorymanager.model.RepositoryInfo;
import org.oscwii.repositorymanager.model.app.Category;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.model.app.Peripheral;
import org.oscwii.repositorymanager.model.app.Platform;
import org.oscwii.repositorymanager.sources.SourceDownloader;
import org.oscwii.repositorymanager.sources.SourceRegistry;
import org.oscwii.repositorymanager.treatments.TreatmentRegistry;
import org.oscwii.repositorymanager.treatments.TreatmentRunnable;
import org.oscwii.repositorymanager.utils.FileUtil;
import org.oscwii.repositorymanager.utils.QuietException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class RepositoryIndex
{
    private final Gson gson;
    private final Logger logger;
    private final RepoManConfig config;
    private final SourceRegistry sources;
    private final TreatmentRegistry treatments;

    private List<Category> categories;
    private List<InstalledApp> contents;
    private Map<String, Platform> platforms;
    private Platform defaultPlatform;
    private RepositoryInfo info;

    @Autowired
    public RepositoryIndex(Gson gson, RepoManConfig config, SourceRegistry sources, TreatmentRegistry treatments)
    {
        this.gson = gson;
        this.logger = LogManager.getLogger(RepositoryIndex.class);
        this.config = config;
        this.sources = sources;
        this.treatments = treatments;

        this.info = RepositoryInfo.DEFAULT;
        this.categories = Collections.emptyList();
        this.contents = Collections.emptyList();
        this.platforms = Collections.emptyMap();
    }

    public void update(boolean updateApps)
    {
        logger.info("Updating repository index");
        // TODO discord log

        // Load repository info
        loadRepositoryInfo();

        // Index categories and platforms
        indexCategories();
        indexPlatforms();

        // Index applications
        indexContents(updateApps);
    }

    private void loadRepositoryInfo()
    {
        File file = config.getRepoDir().resolve("repository.json").toFile();
        this.info = FileUtil.loadJson(file, RepositoryInfo.class, gson, e ->
        {
            logger.error("Unable to load repository info! Has the repository been initialized yet?");
            logger.error("Falling back to no-op empty repository. Cannot continue.");
            handleFatalException(e, "Failed to load repository info:");
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
        Map<String, Platform> platforms = new HashMap<>();
        if(platformList == null)
            return;

        for(Platform platform : platformList)
            platforms.put(platform.name(), platform);

        this.platforms = platforms;
        this.defaultPlatform = platforms.get(config.getDefaultPlatform());
        Assert.notNull(defaultPlatform, "Unknown default platform: " + config.getDefaultPlatform());
    }

    private void indexContents(boolean updateApps)
    {
        AtomicInteger i = new AtomicInteger();
        List<File> manifests;
        List<InstalledApp> contents = new ArrayList<>();
        Path folder = config.getRepoDir().resolve("contents");

        try(Stream<Path> walk = Files.walk(folder, 1))
        {
            manifests = walk.map(Path::toFile)
                    .filter(File::isFile)
                    .filter(file -> file.getName().endsWith(".oscmeta"))
                    .toList();
        }
        catch(IOException e)
        {
            throw new RuntimeException("Failed to access repository contents:", e);
        }

        for(File meta : manifests)
        {
            logger.info("Loading manifest \"{}\" for processing ({}/{})",
                    meta.getName(), i.incrementAndGet(), manifests.size());

            try
            {
                InstalledApp app = processMeta(meta, updateApps);
                contents.add(Objects.requireNonNull(app));
            }
            catch(Exception e)
            {
                logger.error("Failed to process oscmeta {}:", meta.getName(), e);
            }
        }

        this.contents = contents;
        logger.info("Finished indexing application manifests");
    }

    private InstalledApp processMeta(File meta, boolean updateApp)
    {
        OSCMeta oscMeta = FileUtil.loadJson(meta, OSCMeta.class, gson,
                e -> handleFatalException(e, "Failed to process meta \"" + meta.getName() + "\":"));

        // this means the JSON parsing failed
        if(oscMeta == null)
            return null;

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

        // Attempt to update the app
        if(updateApp)
            updateApp(app);

        return app;
    }

    private void updateApp(InstalledApp app)
    {
        logger.info("Updating app {}...", app);
        Path appDir = app.getDataPath();

        try
        {
            // Create if it doesn't exist
            Files.createDirectories(appDir.getParent());

            Path tmpDir = Files.createTempDirectory("RepoMan-" + app.getSlug());
            File downloaded = downloadApp(app, tmpDir);

            // Extract the application files
            extractApp(app.getMeta().source(), downloaded, tmpDir);

            // Apply treatments
            applyTreatments(app, tmpDir);

            // Check for required contents
            checkRequiredContents(app, tmpDir);

            // TODO moderation

            // Cleanup the app directory and move the new files
            FileSystemUtils.deleteRecursively(appDir);
            FileUtils.moveDirectory(tmpDir.toFile(), appDir.toFile());

            // Create a zip archive for HBB and the API
            Path appArchive = appDir.getParent().resolve(app + ".zip");
            FileUtil.zipDirectory(appDir, appArchive);

            // TODO generate WSC info

            // Parse meta.xml
            Path appFiles = appDir.resolve("apps").resolve(app.getSlug());
            Document metaxml = parseMetaXml(appFiles.resolve("meta.xml"));

            logger.info("- Computing information");

            // Determine release date
            Element root = metaxml.getRootElement();
            Element element = root.element("release_date");
            if(element != null)
            {
                String dateText = element.getText();
                for(SimpleDateFormat format : DATE_FORMATS)
                {
                    try
                    {
                        // we want this in seconds
                        app.getComputedInfo().releaseDate = format.parse(dateText).getTime() / 1000;
                        break;
                    }
                    catch(ParseException ignored) {}
                }
            }
            else
                app.getComputedInfo().releaseDate = 0;

            Path binary = appFiles.resolve("boot." + app.getComputedInfo().packageType);

            app.getComputedInfo().archiveSize = Files.size(appArchive);
            app.getComputedInfo().binarySize = Files.size(binary);
            app.getComputedInfo().iconSize = Files.size(appFiles.resolve("icon.png"));
            app.getComputedInfo().rawSize = Files.size(appFiles);
            app.getComputedInfo().md5Hash = FileUtil.md5Hash(binary);
            app.getComputedInfo().peripherals = Peripheral.buildHBBList(app.getPeripherals());

            // Create subdirectories list
            createSubdirectoriesList(app, appFiles);

            // TODO store persistent information and generate TID

            // Hurrah! we finished!
            logger.info("{} has been updated.", app.getMeta().name());
        }
        catch(IOException e)
        {
            handleFatalException(e, "Failed to update app " + app);
        }
    }

    private File downloadApp(InstalledApp app, Path tmpDir) throws IOException
    {
        logger.info("- Downloading application files...");
        OSCMeta.Source source = app.getMeta().source();
        SourceDownloader downloader = sources.getDownloader(source.type());
        Assert.notNull(downloader, "Unsupported source type: " + source.type());
        return downloader.downloadFiles(app, tmpDir).toFile();
    }

    private void extractApp(OSCMeta.Source source, File downloaded, Path tmpDir) throws IOException
    {
        if(!source.type().equals("manual"))
        {
            logger.info("- Extracting application files...");
            FileUtil.extractArchive(downloaded, source.format(), tmpDir);
            // TODO 7zip and rar formats
            Files.delete(downloaded.toPath());
        }
    }

    private void applyTreatments(InstalledApp app, Path tmpDir) throws IOException
    {
        logger.info("- Applying Treatments:");
        List<OSCMeta.Treatment> treatments = app.getMeta().treatments();
        for(OSCMeta.Treatment treatment : treatments)
        {
            TreatmentRunnable runnable = this.treatments.getTreatment(treatment.id());
            Assert.notNull(runnable, "Unsupported treatment: " + treatment.id());
            runnable.run(app, tmpDir, treatment);
        }
    }

    private void checkRequiredContents(InstalledApp app, Path tmpDir) throws IOException
    {
        Path appDir = tmpDir.resolve("apps").resolve(app.getSlug());

        // Check we have an icon
        if(Files.notExists(appDir.resolve("icon.png")))
        {
            logger.info("- icon.png is missing. Using placeholder instead.");
            Files.copy(Objects.requireNonNull(
                    getClass().getResourceAsStream("/static/assets/images/missing.png")),
                    appDir.resolve("icon.png"));
        }

        // Check meta.xml exists
        if(Files.notExists(appDir.resolve("meta.xml")))
            throw new QuietException("Couldn't find meta.xml file");

        // Check binary file
        if(Files.exists(appDir.resolve("boot.dol")))
            app.getComputedInfo().packageType = "dol";
        else if(Files.exists(appDir.resolve("boot.elf")))
            app.getComputedInfo().packageType = "elf";
        else
            throw new QuietException("Couldn't find boot.dol or boot.elf binary.");
    }

    private Document parseMetaXml(Path file) throws IOException
    {
        try(InputStream is = Files.newInputStream(file))
        {
            SAXReader reader = SAXReader.createDefault();
            return reader.read(is);
        }
        catch(DocumentException e)
        {
            throw new QuietException("Failed to load meta.xml", e);
        }
    }

    private void createSubdirectoriesList(InstalledApp app, Path appFiles) throws IOException
    {
        List<String> subdirectories = new ArrayList<>();

        try(Stream<Path> stream = Files.walk(appFiles))
        {
            stream.filter(Files::isDirectory)
                    .filter(path -> !path.equals(appFiles))
                    .forEach(path -> subdirectories.add(buildSubdirectoryPath(appFiles, path)));
        }

        app.getComputedInfo().subdirectories = subdirectories;
    }

    private void handleFatalException(Exception e, String msg)
    {
        throw new QuietException(msg, e);
    }

    private String buildSubdirectoryPath(Path appFiles, Path path)
    {
        return "/" + appFiles.getParent().getParent().relativize(path).toString()
                .replace(File.separatorChar, '/');
    }

    private static final SimpleDateFormat[] DATE_FORMATS = {
            new SimpleDateFormat("yyyyMMddHHmmss"),
            new SimpleDateFormat("yyyyMMddHHmm"),
            new SimpleDateFormat("yyyyMMdd")};
}
