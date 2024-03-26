package org.oscwii.repositorymanager.model.app;

import org.springframework.util.Assert;

import java.nio.file.FileSystems;
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

    public Path getDataPath()
    {
        return FileSystems.getDefault().getPath("data", "contents", slug);
    }

    @Override
    public String toString()
    {
        return slug;
    }

    public static class IndexComputedInfo
    {
        public List<String> subdirectories;
        public long archiveSize, binarySize, iconSize, rawSize;
        public long releaseDate;
        public String packageType, md5Hash, peripherals;
    }

    public static class MetaXml
    {
        public String name, coder, version, shortDesc, longDesc;
    }
}
