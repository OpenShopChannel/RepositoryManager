package org.oscwii.repositorymanager.model.app;

import org.springframework.util.Assert;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class InstalledApp
{
    private final String slug;
    private final OSCMeta meta;
    private final IndexComputedInfo computedInfo;

    private final Category category;
    private final Map<Peripheral, Integer> peripherals;
    private final List<Platform> supportedPlatforms;

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
        this.category = category;
        this.peripherals = peripherals;
        this.supportedPlatforms = supportedPlatforms;
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

    public Map<Peripheral, Integer> getPeripherals()
    {
        return peripherals;
    }

    public List<Platform> getSupportedPlatforms()
    {
        return supportedPlatforms;
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
        public long archiveSize, binarySize, iconSize, rawSize;
        public OffsetDateTime releaseDate;
        public String packageType, md5Hash, peripherals;
    }
}
