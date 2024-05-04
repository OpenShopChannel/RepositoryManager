/*
 * Copyright (c) 2023-2024 Open Shop Channel
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

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
public class FeaturedAppService
{
    private final Supplier<InstalledApp> featured;
    private final RepositoryIndex index;

    private String slug = "";

    @Autowired
    public FeaturedAppService(RepositoryIndex index)
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
        if(index.getContents().isEmpty())
            return;

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
