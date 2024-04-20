package org.oscwii.repositorymanager.model.app;

import org.oscwii.repositorymanager.model.UpdateLevel;
import org.springframework.util.Assert;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class InstalledApp
{
    private final String slug;
    private final OSCMeta meta;
    private final IndexComputedInfo computedInfo;
    private final MetaXml metaXml;

    private final Category category;
    private final List<Platform> supportedPlatforms;
    private final Map<Peripheral, Integer> peripherals;

    private ShopTitle titleInfo;

    public InstalledApp(String slug, OSCMeta meta, Category category,
                        Map<Peripheral, Integer> peripherals,
                        List<Platform> supportedPlatforms)
    {
        Assert.hasLength(slug, "App slug cannot be empty!");
        Assert.notNull(meta, "OSCMeta cannot be null!");
        Assert.notNull(category, "App category cannot be null!");
        this.slug = slug;
        this.meta = meta;
        this.computedInfo = new IndexComputedInfo();
        this.metaXml = new MetaXml();
        this.category = category;
        this.supportedPlatforms = supportedPlatforms;
        this.peripherals = peripherals;
    }

    public String getSlug()
    {
        return slug;
    }

    public OSCMeta getMeta()
    {
        return meta;
    }

    public IndexComputedInfo getComputedInfo()
    {
        return computedInfo;
    }

    public Category getCategory()
    {
        return category;
    }

    public List<Platform> getSupportedPlatforms()
    {
        return supportedPlatforms;
    }

    public Map<Peripheral, Integer> getPeripherals()
    {
        return peripherals;
    }

    public MetaXml getMetaXml()
    {
        return metaXml;
    }

    public ShopTitle getTitleInfo()
    {
        return titleInfo;
    }

    public void setTitleInfo(ShopTitle titleInfo)
    {
        this.titleInfo = titleInfo;
    }

    public Path getDataPath()
    {
        return Path.of("data", "contents", slug);
    }

    public Path getAppFilesPath()
    {
        return getDataPath().resolve("apps").resolve(slug);
    }

    public UpdateLevel compare(InstalledApp other)
    {
        if(other == null)
            return UpdateLevel.FIRST_RUN;
        if(other.computedInfo.rawSize != this.computedInfo.rawSize)
            return UpdateLevel.MODIFIED;
        else if(!other.computedInfo.binaryHash.equals(this.computedInfo.binaryHash))
            return UpdateLevel.NEW_BINARY;
        else if(!other.metaXml.version.equals(this.metaXml.version))
            return UpdateLevel.NEW_VERSION;
        return UpdateLevel.SAME;
    }

    public String describe()
    {
        return "InstalledApp{" +
                "slug='" + slug + '\'' +
                ", meta=" + meta +
                ", computedInfo=" + computedInfo +
                ", metaXml=" + metaXml +
                ", category=" + category +
                ", supportedPlatforms=" + supportedPlatforms +
                ", peripherals=" + peripherals +
                ", titleInfo=" + titleInfo +
                '}';
    }

    @Override
    public String toString()
    {
        return slug;
    }

    public static class IndexComputedInfo
    {
        public int releaseDate;
        public List<String> subdirectories;
        public long archiveSize, binarySize, iconSize, rawSize, shopBannerSize;
        public String packageType, archiveHash, binaryHash, peripherals;

        @Override
        public String toString()
        {
            return "IndexComputedInfo{" +
                    "releaseDate=" + releaseDate +
                    ", subdirectories=" + subdirectories +
                    ", archiveSize=" + archiveSize +
                    ", binarySize=" + binarySize +
                    ", iconSize=" + iconSize +
                    ", rawSize=" + rawSize +
                    ", shopBannerSize=" + shopBannerSize +
                    ", packageType='" + packageType + '\'' +
                    ", archiveHash='" + archiveHash + '\'' +
                    ", binaryHash='" + binaryHash + '\'' +
                    ", peripherals='" + peripherals + '\'' +
                    '}';
        }
    }

    public static class MetaXml
    {
        public String name, coder, version, shortDesc, longDesc;

        public String name()
        {
            return name;
        }

        public String coder()
        {
            return coder;
        }

        public String version()
        {
            return version;
        }

        @Override
        public String toString()
        {
            return "MetaXml{" +
                    "name='" + name + '\'' +
                    ", coder='" + coder + '\'' +
                    ", version='" + version + '\'' +
                    ", shortDesc='" + shortDesc + '\'' +
                    ", longDesc='" + longDesc + '\'' +
                    '}';
        }
    }
}
