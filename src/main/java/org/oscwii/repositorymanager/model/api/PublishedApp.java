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

package org.oscwii.repositorymanager.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    public record Description(@JsonProperty("short") String shortDescription,
                              @JsonProperty("long") String longDescription)
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
    public record ShopInfo(long contentsSize, String titleId, int inodes, int titleVersion, long tmdSize)
    {
        public ShopInfo(InstalledApp app)
        {
            this(app.getComputedInfo().shopContentsSize, app.getTitleInfo().getTitleId(),
                    app.getComputedInfo().inodes, app.getTitleInfo().getVersion(),
                    app.getComputedInfo().shopTmdSize);
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
