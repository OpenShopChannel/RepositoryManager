package org.oscwii.repositorymanager.model.api;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta.Flag;
import org.oscwii.repositorymanager.utils.FormatUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record App(String slug, String name, String author, String category,
                  Description description, Map<String, Long> fileSize,
                  EnumSet<Flag> flags, String packageType, List<String> peripherals,
                  int releaseDate, ShopInfo shop, List<String> subdirectories,
                  List<String> supportedPlatforms, Resources url, String version)
{
    public App(InstalledApp app)
    {
        this(app.getSlug(), app.getMeta().name(), app.getMeta().author(), app.getMeta().category(),
                new Description(app), Map.of("binary", app.getComputedInfo().binarySize,
                        "icon", app.getComputedInfo().iconSize,
                        "zip_compressed", app.getComputedInfo().archiveSize,
                        "zip_uncompressed", app.getComputedInfo().rawSize),
                app.getMeta().flags(), app.getComputedInfo().packageType, app.getMeta().peripherals(),
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
