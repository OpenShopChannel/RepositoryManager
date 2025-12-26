/*
 * Copyright (c) 2023-2025 Open Shop Channel
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
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.model.app.Platform;
import org.oscwii.repositorymanager.utils.FormatUtil;

import java.util.EnumSet;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PublishedAppV3(String slug, String name, String author, String category, String[] contributors,
                             Description description, FileSizes fileSize, EnumSet<OSCMeta.Flag> flags,
                             String packageType, List<String> peripherals, long releaseDate, ShopInfo shop,
                             List<String> subdirectories, List<String> supportedPlatforms, Resources url, String version)
{
    public PublishedAppV3(InstalledApp app)
    {
        this(app.getSlug(), app.getMeta().name(), getAuthors(app), app.getMeta().category(), app.getMeta().contributors(),
                new Description(app), new FileSizes(app), app.getMeta().flags(), app.getComputedInfo().packageType,
                app.getMeta().peripherals(), app.getComputedInfo().releaseDate, new ShopInfo(app),
                app.getComputedInfo().subdirectories, getPlatforms(app), new Resources(app),
                app.getEffectiveVersion());
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Description(@JsonProperty("short") String shortDesc, @JsonProperty("long") String longDesc)
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

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ShopInfo(long contentsSize, String titleId, int inodes, int titleVersion, long tmdSize)
    {
        public ShopInfo(InstalledApp app)
        {
            this(app.getComputedInfo().shopContentsSize, app.getTitleInfo().getTitleId(), app.getComputedInfo().inodes,
                    app.getTitleInfo().getVersion(), app.getComputedInfo().shopTmdSize);
        }
    }

    public record Resources(String icon, String zip)
    {
        public Resources(InstalledApp app)
        {
            this(FormatUtil.iconUrl(app.getSlug()), FormatUtil.zipUrl(app.getSlug(), true));
        }
    }

    private static String getAuthors(InstalledApp app)
    {
        return String.join(", ", app.getAllAuthors());
    }

    static List<String> getPlatforms(InstalledApp app)
    {
        return app.getSupportedPlatforms().stream()
                .map(Platform::name)
                .toList();
    }
}
