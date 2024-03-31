package org.oscwii.repositorymanager.services;

import org.oscwii.repositorymanager.RepositoryIndex;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.Peripheral;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Service
public class FeaturedApp
{
    private final Supplier<InstalledApp> featured;
    private final RepositoryIndex index;

    private String slug;

    @Autowired
    public FeaturedApp(RepositoryIndex index)
    {
        this.index = index;
        this.featured = () -> index.getApp(slug);
    }

    public InstalledApp getFeatured()
    {
        return featured.get();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void pickFeaturedApp()
    {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        while(true)
        {
            int i = rand.nextInt(0, index.getContents().size());
            InstalledApp app = index.getContents().get(i);

            // Quality assurance
            if(app.getMetaXml().shortDesc.isEmpty())
                continue;
            if(app.getMeta().category().equals("demos"))
                continue;
            if(!app.getPeripherals().containsKey(Peripheral.WII_REMOTE))
                continue;
            if(app.getMeta().author().equals("Danbo"))
                continue;

            // We found a good one
            this.slug = app.getSlug();
            break;
        }
    }
}
