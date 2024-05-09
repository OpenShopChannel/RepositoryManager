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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.model.app.Peripheral;
import org.oscwii.repositorymanager.utils.FormatUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.oscwii.repositorymanager.model.api.PublishedAppV3.getPlatforms;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PublishedAppV4(String slug, String name, String author, String category,
                             Description description, Map<String, Asset> assets, EnumSet<OSCMeta.Flag> flags,
                             String packageType, List<String> peripherals, long releaseDate, ShopInfo shop,
                             List<String> subdirectories, List<String> supportedPlatforms, long uncompressedSize,
                             String version)
{
    public PublishedAppV4(InstalledApp app)
    {
        this(app.getSlug(), app.getMeta().name(), app.getMeta().author(), app.getMeta().category(),
                new Description(app), getAssets(app), app.getMeta().flags(), app.getComputedInfo().packageType,
                getPeripherals(app), app.getComputedInfo().releaseDate, new ShopInfo(app),
                app.getComputedInfo().subdirectories, getPlatforms(app), app.getComputedInfo().rawSize,
                app.getEffectiveVersion());
    }

    public record Asset(String url, @JsonInclude(NON_NULL) String hash, @JsonInclude(NON_DEFAULT) long size) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Description(@JsonProperty("short") String shortDesc, @JsonProperty("long") String longDesc)
    {
        public Description(InstalledApp app)
        {
            this(app.getMetaXml().shortDesc, app.getMetaXml().longDesc);
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

    private static Map<String, Asset> getAssets(InstalledApp app)
    {
        return Map.of("archive", new Asset(FormatUtil.zipUrl(app.getSlug()), app.getComputedInfo().archiveHash,
                        app.getComputedInfo().archiveSize),
                "binary", new Asset(FormatUtil.binaryUrl(app.getSlug(), app.getComputedInfo().packageType),
                        app.getComputedInfo().binaryHash, app.getComputedInfo().binarySize),
                "icon", new Asset(FormatUtil.iconUrl(app.getSlug()), null, app.getComputedInfo().iconSize),
                "meta", new Asset(FormatUtil.metaXmlUrl(app.getSlug()), null, 0)
        );
    }

    private static List<String> getPeripherals(InstalledApp app)
    {
        return app.getMeta().peripherals().stream()
                .map(Peripheral::fromDisplay)
                .map(Peripheral::name)
                .map(String::toLowerCase)
                .toList();
    }
}
