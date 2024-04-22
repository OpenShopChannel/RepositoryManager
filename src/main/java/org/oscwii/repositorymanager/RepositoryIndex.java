package org.oscwii.repositorymanager;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedTitle;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.sentry.ISpan;
import io.sentry.MeasurementUnit;
import io.sentry.Sentry;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.oscwii.repositorymanager.config.repoman.RepoManConfig;
import org.oscwii.repositorymanager.database.dao.AppDAO;
import org.oscwii.repositorymanager.database.dao.ModerationDAO;
import org.oscwii.repositorymanager.exceptions.AppFilesMissingException;
import org.oscwii.repositorymanager.exceptions.ModerationException;
import org.oscwii.repositorymanager.exceptions.QuietException;
import org.oscwii.repositorymanager.factory.DiscordWebhookFactory;
import org.oscwii.repositorymanager.logging.DiscordAppender;
import org.oscwii.repositorymanager.logging.DiscordMessage;
import org.oscwii.repositorymanager.logging.IndexTriggeringPolicy;
import org.oscwii.repositorymanager.logging.WebSocketAppender;
import org.oscwii.repositorymanager.model.ModeratedBinary;
import org.oscwii.repositorymanager.model.RepositoryInfo;
import org.oscwii.repositorymanager.model.UpdateLevel;
import org.oscwii.repositorymanager.model.app.Category;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.model.app.Peripheral;
import org.oscwii.repositorymanager.model.app.Platform;
import org.oscwii.repositorymanager.model.app.ShopTitle;
import org.oscwii.repositorymanager.services.FeaturedAppService;
import org.oscwii.repositorymanager.sources.SourceDownloader;
import org.oscwii.repositorymanager.sources.SourceRegistry;
import org.oscwii.repositorymanager.treatments.TreatmentRegistry;
import org.oscwii.repositorymanager.treatments.TreatmentRunnable;
import org.oscwii.repositorymanager.utils.AppUtil;
import org.oscwii.repositorymanager.utils.FileUtil;
import org.oscwii.repositorymanager.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.oscwii.repositorymanager.logging.DiscordAppender.ERROR_AVATAR;

@Service
public class RepositoryIndex
{
    private final AppDAO appDao;
    private final DiscordWebhookFactory discordWebhook;
    private final FeaturedAppService featuredAppService;
    private final Gson gson;
    private final Logger logger;
    private final ModerationDAO modDao;
    private final RepoManConfig config;
    private final RepositorySource repoSource;
    private final SourceRegistry sources;
    private final TreatmentRegistry treatments;

    private boolean logMissingFiles = false;
    private List<Category> categories;
    private List<InstalledApp> contents;
    private Map<String, Platform> platforms;
    private Platform defaultPlatform;
    private RepositoryInfo info;

    @Autowired
    public RepositoryIndex(AppDAO appDao, DiscordWebhookFactory discordWebhook, @Lazy FeaturedAppService featuredAppService, Gson gson,
                           ModerationDAO modDao, RepoManConfig config, RepositorySource repoSource, SourceRegistry sources,
                           TreatmentRegistry treatments, SimpMessageSendingOperations webSocket)
    {
        this.appDao = appDao;
        this.discordWebhook = discordWebhook;
        this.gson = gson;
        this.logger = LogManager.getLogger(RepositoryIndex.class);
        this.modDao = modDao;
        this.config = config;
        this.featuredAppService = featuredAppService;
        this.repoSource = repoSource;
        this.sources = sources;
        this.treatments = treatments;

        this.info = RepositoryInfo.DEFAULT;
        this.categories = Collections.emptyList();
        this.contents = Collections.emptyList();
        this.platforms = Collections.emptyMap();

        // Register web status appender
        var loggerImpl = (org.apache.logging.log4j.core.Logger) logger;
        WebSocketAppender webSocketAppender = new WebSocketAppender(webSocket);
        webSocketAppender.start();
        loggerImpl.addAppender(webSocketAppender);

        // Register the logger appender to notify Discord
        // if we have a valid webhook configured
        if(config.discordConfig.isLoggingEnabled())
        {
            DiscordAppender appender = new DiscordAppender(discordWebhook);
            appender.start();
            loggerImpl.addAppender(appender);
        }
    }

    public void initialize()
    {
        try
        {
            Configurator.setLevel(logger, Level.ERROR);
            index(false);
        }
        finally
        {
            // Ensure the logging level is back to normal
            Configurator.setLevel(logger, Level.INFO);

            if(logMissingFiles)
            {
                this.logMissingFiles = false;
                logger.info("Some app files are missing, they were skipped from indexing.");
                logger.info("You may want to force an update.");
            }
        }
    }

    @Scheduled(fixedDelay = 6, initialDelay = 6, timeUnit = HOURS)
    public void updateIndex()
    {
        repoSource.pull();
        index(true);

        if(featuredAppService.getFeatured() == null)
        {
            // The app has been removed, pick a new one
            featuredAppService.pickFeaturedApp();
        }
    }

    public void index(boolean updateApps)
    {
        long start = System.currentTimeMillis();
        logger.info(new DiscordMessage("Updating repository index", "Reindexing all applications..."));

        // Load repository info
        loadRepositoryInfo();

        // Index categories and platforms
        indexCategories();
        indexPlatforms();

        // Index applications
        int[] info = indexContents(updateApps);

        // Create icon cache
        createIconCache();

        // Print index summary
        printIndexSummary(info, start);

        logger.info("Finished updating repository index");
        IndexTriggeringPolicy.INSTANCE.trigger();
    }

    public void generateShopData()
    {
        logger.info("Generating Shop Data...");

        for(InstalledApp app : contents)
        {
            logger.debug("- Processing {}...", app);

            try
            {
                checkRequiredContents(app, app.getDataPath());
                generateShopBanner(app);
                calculateShopBannerSize(app, config.shopConfig.bannerOutputPath());
            }
            catch(Exception e)
            {
                logger.error("Failed to generate shop data for {}:", app, e);
            }
        }

        logger.info("Finished generating shop data for all apps!");
    }

    public List<InstalledApp> getContents()
    {
        return contents;
    }

    public List<Category> getCategories()
    {
        return categories;
    }

    public InstalledApp getApp(String slug)
    {
        for(InstalledApp app : contents)
        {
            if(app.getSlug().equalsIgnoreCase(slug))
                return app;
        }

        return null;
    }

    public RepositoryInfo getInfo()
    {
        return info;
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
        this.categories = FileUtil.loadJson(file, new TypeToken<>(){},
                gson, e -> handleFatalException(e, "Failed to load categories:"));
    }

    private void indexPlatforms()
    {
        File file = config.getRepoDir().resolve("platforms.json").toFile();
        List<Platform> platformList = FileUtil.loadJson(file, new TypeToken<>(){}, gson,
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

    private int[] indexContents(boolean updateApps)
    {
        int index = 0, errors = 0, indexed = 0;
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
            InstalledApp app;
            logger.info("Loading manifest \"{}\" for processing ({}/{})",
                    meta.getName(), ++index, manifests.size());

            try
            {
                app = processMeta(meta, updateApps);
                contents.add(requireNonNull(app));
                indexed++;
            }
            catch(AppFilesMissingException ignored)
            {
                this.logMissingFiles = true;
                continue;
            }
            catch(ModerationException e)
            {
                try(WebhookClient webhook = discordWebhook.logWebhook())
                {
                    if(webhook != null && updateApps)
                    {
                        var embed = new DiscordMessage(null, "App index failure",
                                "**FAILED TO PROCESS " + meta.getName() + ":**\n" + e.getMessage());
                        WebhookMessage message = new WebhookMessageBuilder()
                                .addEmbeds(embed.toEmbed())
                                .setUsername("Repository Manager: Error")
                                .setAvatarUrl(ERROR_AVATAR)
                                .build();
                        webhook.send(message);
                    }
                }

                continue;
            }
            catch(Exception e)
            {
                handleApplicationUpdateFailure(meta, e);
                errors++;

                // Try to use the old version
                app = getApp(meta.getName().replace(".oscmeta", ""));
                if(app == null)
                    continue;
            }

            // Notify if necessary
            determineUpdateLevel(app);
        }

        this.contents = contents;
        logger.info("Finished indexing application manifests");
        return new int[]{index, indexed, errors};
    }

    private void printIndexSummary(int[] info, long start)
    {
        long elapsed = (System.currentTimeMillis() - start) / 1000;
        Map<String, Object> summary = Map.of(
                "Installed Manifests", info[0],
                "Indexed Applications", info[1],
                "Indexing errors", info[2],
                "Elapsed time", FormatUtil.secondsToTime(elapsed));

        logger.info("** INDEX SUMMARY **");
        StringBuilder discordStr = new StringBuilder();
        for(Map.Entry<String, Object> entry : summary.entrySet())
        {
            logger.info("{}: {}", entry.getKey(), entry.getValue());
            discordStr.append(String.format("**%s:** %s\n", entry.getKey(), entry.getValue()));
        }

        // Send Sentry metrics
        ISpan span = Sentry.getSpan();
        if(span != null)
        {
            span.setMeasurement("indexed_applications", info[1]);
            span.setMeasurement("index_errors", info[2]);
            span.setMeasurement("index_completion_time", elapsed, MeasurementUnit.Duration.SECOND);
        }

        logger.info(new DiscordMessage("** INDEX SUMMARY **", "Index Summary", discordStr.toString()));
    }

    private void createIconCache()
    {
        try
        {
            Path iconsDir = Path.of("data", "icons");
            Files.createDirectories(iconsDir);

            for(InstalledApp app : contents)
            {
                Path appIcon = app.getAppFilesPath().resolve("icon.png");
                Path cachedIcon = iconsDir.resolve(app.getSlug() + ".png");
                Files.copy(appIcon, cachedIcon);
            }

            FileUtil.zipDirectory(iconsDir, Path.of("data", "icons.zip"));
            FileSystemUtils.deleteRecursively(iconsDir);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to create icon cache:", e);
        }
    }

    private InstalledApp processMeta(File meta, boolean updateApp) throws IOException
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

        // Check if we have to update the app
        if(!updateApp)
        {
            if(Files.notExists(app.getAppFilesPath()))
                throw new AppFilesMissingException();

            checkRequiredContents(app, app.getDataPath());
            checkModerationStatus(app, app.getDataPath());
            loadAppInformation(app, Path.of("data", "contents", app.getSlug() + ".zip"));
        }
        else
            updateApp(app);

        // Hurrah! we finished!
        logger.info("{} has been updated.", app.getMeta().name());
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

            // Check moderation status
            checkModerationStatus(app, tmpDir);

            // Cleanup the app directory and move the new files
            FileSystemUtils.deleteRecursively(appDir);
            FileUtils.moveDirectory(tmpDir.toFile(), appDir.toFile());

            // Create a zip archive for HBB and the API
            logger.info("- Creating ZIP file");
            Path appArchive = appDir.getParent().resolve(app + ".zip");
            FileUtil.zipDirectory(appDir, appArchive);

            // Load app information from database
            loadAppInformation(app, appArchive);

            // Generate WSC Banner
            if(config.shopConfig.generateBanner())
            {
                logger.info("- Creating banner for Wii Shop Channel");
                generateShopBanner(app);
            }
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
            try(InputStream placeholder = requireNonNull(getClass().getResourceAsStream(PLACEHOLDER_ICON)))
            {
                Files.copy(placeholder, appDir.resolve("icon.png"));
            }
        }

        // Check meta.xml exists
        if(Files.notExists(appDir.resolve("meta.xml")))
            throw new QuietException("Couldn't find meta.xml file");

        // Check binary file
        determineBinary(app, appDir);
    }

    private void determineBinary(InstalledApp app, Path appDir)
    {
        if(Files.exists(appDir.resolve("boot.dol")))
            app.getComputedInfo().packageType = "dol";
        else if(Files.exists(appDir.resolve("boot.elf")))
            app.getComputedInfo().packageType = "elf";
        else
            throw new QuietException("Couldn't find boot.dol or boot.elf binary.");
    }

    private void checkModerationStatus(InstalledApp app, Path tmpDir) throws IOException
    {
        Path appFilesDir = tmpDir.resolve("apps").resolve(app.getSlug());
        Path binary = appFilesDir.resolve("boot." + app.getComputedInfo().packageType);
        String hash = FileUtil.md5Hash(binary);
        app.getComputedInfo().binaryHash = hash;

        logger.info("- Checking moderation status");
        logger.info("  - Binary checksum: {}", hash);

        Path modArchive = Path.of("data", "moderation", hash + ".zip");
        Optional<ModeratedBinary> optEntry = modDao.findByChecksum(hash);
        if(optEntry.isPresent())
        {
            ModeratedBinary modEntry = optEntry.get();
            switch(modEntry.status())
            {
                case APPROVED:
                    logger.info("  - Application binary is approved by moderation");
                    break;
                case PENDING:
                    logger.info("  - Application binary is currently pending moderation");
                    FileUtil.zipDirectory(tmpDir, modArchive);
                    logger.info("  - Updated moderation archive.");
                    throw new ModerationException("Binary requires moderation");
                case REJECTED:
                    logger.info("  - Application binary has been rejected by moderation");
                    FileUtil.zipDirectory(tmpDir, modArchive);
                    logger.info("  - Updated moderation archive.");
                    throw new ModerationException("Binary rejected in moderation");
            }
        }
        else
        {
            ModeratedBinary modEntry = new ModeratedBinary(hash, app.getSlug());
            modDao.insertEntry(modEntry);

            // Notify Discord
            try(WebhookClient webhook = discordWebhook.modWebhook())
            {
                if(webhook == null)
                    return;
                WebhookEmbed embed = new WebhookEmbedBuilder()
                        .setTitle(new EmbedTitle("New version discovered and pending moderation!", null))
                        .setDescription(app.getSlug() + "-" + hash)
                        .build();
                webhook.send(embed);
            }

            logger.info("  - Submitted application binary for moderation");
            FileUtil.zipDirectory(tmpDir, modArchive);
            logger.info("  - Updated moderation archive.");
            throw new ModerationException("Binary requires moderation");
        }
    }

    private void generateShopBanner(InstalledApp app) throws IOException
    {
        Path generator = config.shopConfig.bannerGeneratorPath();
        Path workingDir = generator.getParent();
        Path contentsDir = Path.of("data", "contents").toAbsolutePath();

        // Generate the banner
        AppUtil.generateBanner(app, logger, generator, contentsDir, workingDir);

        // Calculate size for banner
        Path bannerOut = workingDir.resolve("out").resolve(app.getTitleInfo().getTitleId());
        calculateShopBannerSize(app, bannerOut.getParent());

        // Move the banner output
        Path outputPath = config.shopConfig.bannerOutputPath();
        FileSystemUtils.deleteRecursively(outputPath.resolve(app.getTitleInfo().getTitleId()));
        FileUtils.moveDirectoryToDirectory(bannerOut.toFile(), outputPath.toFile(), true);
    }

    private void loadAppInformation(InstalledApp app, Path appArchive) throws IOException
    {
        Path appFiles = app.getAppFilesPath();

        // Parse meta.xml
        logger.info("- Reading application metadata");
        Document metaxml = parseMetaXml(appFiles.resolve("meta.xml"));

        logger.info("- Computing information");
        Element root = metaxml.getRootElement();

        app.getMetaXml().name = requireNonNullElse(root.elementText("name"), app.getMeta().name());
        app.getMetaXml().coder = requireNonNullElse(root.elementText("coder"), app.getMeta().author());
        app.getMetaXml().version = requireNonNullElse(root.elementText("version"), "Unknown");
        app.getMetaXml().shortDesc = requireNonNullElse(root.elementText("short_description"), "");
        app.getMetaXml().longDesc = requireNonNullElse(root.elementText("long_description"), "");

        // Determine release date
        String dateText = root.elementText("release_date");
        if(dateText != null)
        {
            for(SimpleDateFormat format : DATE_FORMATS)
            {
                try
                {
                    // we want this in seconds
                    app.getComputedInfo().releaseDate = (int) format.parse(dateText).toInstant().getEpochSecond();
                    break;
                }
                catch(ParseException ignored) {}
            }
        }

        Path binary = appFiles.resolve("boot." + app.getComputedInfo().packageType);

        app.getComputedInfo().archiveHash = FileUtil.md5Hash(appArchive);
        app.getComputedInfo().archiveSize = Files.size(appArchive);
        app.getComputedInfo().binarySize  = Files.size(binary);
        app.getComputedInfo().iconSize    = Files.size(appFiles.resolve("icon.png"));
        app.getComputedInfo().rawSize     = FileUtils.sizeOfDirectory(appFiles.toFile());
        app.getComputedInfo().peripherals = Peripheral.buildHBBList(app.getPeripherals());

        // Create subdirectories list
        createSubdirectoriesList(app, appFiles);

        // Update app information in the database and register if it doesn't exist
        ShopTitle shopTitle = appDao.getShopTitle(app.getSlug());
        if(appDao.appExists(app.getSlug()).isEmpty())
        {
            appDao.insertApp(app);
            if(shopTitle == null)
                shopTitle = appDao.insertShopTitle(app.getSlug());
            logger.info("  - Registered app in database");
        }
        else
            appDao.updateApp(app);

        // Check the app has a TID assigned
        assignTID(app, shopTitle);
        app.setTitleInfo(shopTitle);

        if(app.getComputedInfo().shopTmdSize == 0)
        {
            try
            {
                calculateShopBannerSize(app, config.shopConfig.bannerOutputPath());
            }
            catch(Exception ignored) {}
        }
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

    private void assignTID(InstalledApp app, ShopTitle shopTitle)
    {
        if(shopTitle.getTitleId() == null)
        {
            // Assign a random TID
            boolean isInUse = false;
            String titleId = "";

            while(!isInUse)
            {
                titleId = AppUtil.generateTID();
                isInUse = appDao.isTIDInUse(titleId).isEmpty();
            }

            appDao.insertTID(app.getSlug(), titleId);
            shopTitle.setTitleId(titleId);
            logger.info("  - Assigned new title ID: {}", titleId);
        }
    }

    private void calculateShopBannerSize(InstalledApp app, Path working) throws IOException
    {
        Path bannerOut = working.resolve(app.getTitleInfo().getTitleId());
        long tmdSize = Files.size(bannerOut.resolve("tmd"));
        long contentSize = FileUtils.sizeOfDirectory(bannerOut.toFile()) - tmdSize;

        app.getComputedInfo().shopTmdSize = tmdSize;
        app.getComputedInfo().shopContentsSize = contentSize;
        app.getComputedInfo().inodes = FileUtil.getFiles(bannerOut, 1, (x) -> true).size() - 1;
    }

    private void determineUpdateLevel(InstalledApp newApp)
    {
        logger.info("- Checking if application was updated");
        InstalledApp oldApp = null;

        for(InstalledApp installedApp : contents)
        {
            if(installedApp.getSlug().equals(newApp.getSlug()))
            {
                oldApp = installedApp;
                break;
            }
        }

        boolean exists = oldApp != null;
        if(!exists)
        {
            // Check in the database
            exists = appDao.appExists(newApp.getSlug()).isPresent();
        }

        UpdateLevel level = !exists ? UpdateLevel.NEW_APP : newApp.compare(oldApp);
        try(WebhookClient webhook = discordWebhook.catalogWebhook())
        {
            switch(level)
            {
                case SAME -> logger.info("  - No Change");
                case FIRST_RUN -> logger.info("  - First Index");
                case MODIFIED ->
                {
                    logger.info("  - Modified Archive");
                    notifyCatalog("New Update!", newApp.getMetaXml().name +
                            " has been updated.", webhook);
                }
                case NEW_BINARY ->
                {
                    logger.info("  - New Binary");
                    notifyCatalog("New Update!", newApp.getMetaXml().name +
                            " has been updated.", webhook);
                }
                case NEW_VERSION ->
                {
                    logger.info("  - New Version");
                    //noinspection DataFlowIssue - cannot be null here
                    notifyCatalog("New Version!", newApp.getMetaXml().name +
                            " has been updated from " + oldApp.getMetaXml().version +
                            "to " +  newApp.getMetaXml().version, webhook);
                }
                case NEW_APP ->
                {
                    logger.info("  - New Application");
                    notifyCatalog("New Application!", newApp.getMetaXml().name +
                            " has been added to the repository.", webhook);
                }
            }
        }

        // Bump version in database if app was updated
        if(level.isUpdated())
        {
            ShopTitle titleInfo = newApp.getTitleInfo();
            int version = titleInfo.getVersion() + 1;
            appDao.setTitleVersion(newApp.getSlug(), version);
            titleInfo.setVersion(version);
            logger.info("  - Bumped title version to {}", version);
        }
    }

    private void notifyCatalog(String title, String description, WebhookClient webhook)
    {
        if(webhook == null)
            return;

        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setTitle(new EmbedTitle(title, null))
                .setDescription(description)
                .build();
        WebhookMessage message = new WebhookMessageBuilder()
                .addEmbeds(embed)
                .setUsername(info.name())
                .build();
        webhook.send(message);
    }

    private String buildSubdirectoryPath(Path appFiles, Path path)
    {
        return "/" + appFiles.getParent().getParent().relativize(path).toString()
                .replace(File.separatorChar, '/');
    }

    private void handleApplicationUpdateFailure(File meta, Exception ex)
    {
        if(ex instanceof QuietException quiet && quiet.getCause() != null)
            ex = (Exception) quiet.getCause();

        String stackTrace = Arrays.stream(ex.getStackTrace())
                .limit(3)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));

        String errorMessage = "**FAILED TO PROCESS " + meta.getName() + ":**\n" +
                "**EXCEPTION:** " + ex.getClass() + "\n" +
                "**ERROR MESSAGE:** " + ex.getMessage() + "\n" +
                "**STACK TRACE:** " + stackTrace + "\n" +
                "** MOVING ON. **";
        logger.error(new DiscordMessage("Failed to process oscmeta " + meta.getName() + ": ",
                "App index failure", errorMessage), ex);
    }

    private void handleFatalException(Exception e, String msg)
    {
        throw new QuietException(msg, e);
    }

    private static final SimpleDateFormat[] DATE_FORMATS = {
            new SimpleDateFormat("yyyyMMddHHmmss"),
            new SimpleDateFormat("yyyyMMddHHmm"),
            new SimpleDateFormat("yyyyMMdd")};
    private static final String PLACEHOLDER_ICON = "/static/assets/images/missing.png";
}
