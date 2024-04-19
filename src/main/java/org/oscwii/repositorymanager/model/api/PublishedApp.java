package org.oscwii.repositorymanager.model.api;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta.Flag;
import org.oscwii.repositorymanager.utils.FormatUtil;

import java.util.EnumSet;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PublishedApp(String slug, String name, String author, String category,
                           Description description, FileSizes fileSize, Hashes hashes,
                           EnumSet<Flag> flags, String packageType, List<String> peripherals,
                           int releaseDate, ShopInfo shop, List<String> subdirectories,
                           List<String> supportedPlatforms, Resources url, String version)
{
    public PublishedApp(InstalledApp app)
    {
        this(app.getSlug(), app.getMeta().name(), app.getMeta().author(), app.getMeta().category(),
                new Description(app), new FileSizes(app), new Hashes(app), app.getMeta().flags(),
                app.getComputedInfo().packageType, app.getMeta().peripherals(),
                app.getComputedInfo().releaseDate, new ShopInfo(app), app.getComputedInfo().subdirectories,
                app.getMeta().supportedPlatforms(), new Resources(app), app.getMetaXml().version);
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Description(String shortDescription, String longDescription)
    {
        public Description(InstalledApp app)
        {
            this(app.getMetaXml().shortDesc, app.getMetaXml().longDesc);
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record FileSizes(long binary, long icon, long zipCompressed, long zipUncompressed)
    {
        public FileSizes(InstalledApp app)
        {
            this(app.getComputedInfo().binarySize, app.getComputedInfo().iconSize,
                    app.getComputedInfo().archiveSize, app.getComputedInfo().rawSize);
        }
    }

    public record Hashes(String archive, String binary)
    {
        public Hashes(InstalledApp app)
        {
            this(app.getComputedInfo().archiveHash, app.getComputedInfo().binaryHash);
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ShopInfo(String titleId, int titleVersion)
    {
        public ShopInfo(InstalledApp app)
        {
            this(app.getTitleInfo().getTitleId(), app.getTitleInfo().getVersion());
        }
    }

    public record Resources(String icon, String zip)
    {
        public Resources(InstalledApp app)
        {
            this(FormatUtil.iconUrl(app.getSlug()), FormatUtil.zipUrl(app.getSlug()));
        }
    }
}
