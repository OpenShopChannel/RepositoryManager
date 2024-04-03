package org.oscwii.repositorymanager.sources;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class SourceRegistry
{
    private final Map<String, SourceDownloader> sources = new HashMap<>();

    public void registerDownloader(SourceDownloader downloader)
    {
        Assert.notNull(downloader, "Downloader cannot be null!");
        sources.put(downloader.getType().toLowerCase(), downloader);
    }

    @Nullable
    public SourceDownloader getDownloader(String type)
    {
        return sources.get(type.toLowerCase());
    }

    public Collection<SourceDownloader> getSources()
    {
        return Collections.unmodifiableCollection(sources.values());
    }
}
