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

package org.oscwii.repositorymanager.utils.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.Peripheral;
import org.oscwii.repositorymanager.model.app.Platform;
import org.oscwii.repositorymanager.utils.FormatUtil;

import java.util.Map;

public class AppSerializer
{
    public JsonObject serializeV3(InstalledApp app)
    {
        JsonObject obj = baseSerialize(app);

        JsonObject fileSizes = new JsonObject();
        fileSizes.addProperty("binary", app.getComputedInfo().binarySize);
        fileSizes.addProperty("icon", app.getComputedInfo().iconSize);
        fileSizes.addProperty("zip_compressed", app.getComputedInfo().archiveSize);
        fileSizes.addProperty("zip_uncompressed", app.getComputedInfo().rawSize);
        obj.add("file_size", fileSizes);

        JsonObject hashes = new JsonObject();
        hashes.addProperty("archive", app.getComputedInfo().archiveHash);
        hashes.addProperty("binary", app.getComputedInfo().binaryHash);
        obj.add("hashes", hashes);

        JsonArray peripherals = new JsonArray();
        app.getMeta().peripherals().forEach(peripherals::add);
        obj.add("peripherals", peripherals);

        JsonArray platforms = new JsonArray();
        app.getMeta().supportedPlatforms().forEach(platforms::add);
        obj.add("supported_platforms", platforms);

        JsonObject urls = new JsonObject();
        urls.addProperty("icon", FormatUtil.iconUrl(app.getSlug()));
        urls.addProperty("zip", FormatUtil.zipUrl(app.getSlug()));
        obj.add("url", urls);

        return obj;
    }

    public JsonObject serializeV4(InstalledApp app)
    {
        JsonObject obj = baseSerialize(app);

        JsonArray assets = new JsonArray();
        assets.add(buildAsset("archive", FormatUtil.zipUrl(app.getSlug()),
                app.getComputedInfo().archiveHash, app.getComputedInfo().archiveSize));
        assets.add(buildAsset("binary", FormatUtil.binaryUrl(app.getSlug(), app.getComputedInfo().packageType),
                app.getComputedInfo().binaryHash, app.getComputedInfo().binarySize));
        assets.add(buildAsset("icon", FormatUtil.iconUrl(app.getSlug()), null, app.getComputedInfo().iconSize));
        assets.add(buildAsset("meta", FormatUtil.metaXmlUrl(app.getSlug()), null, 0));

        obj.add("assets", assets);
        obj.add("peripherals", buildPeripherals(app));
        obj.add("supported_platforms", buildSupportedPlatforms(app));
        return obj;
    }

    private JsonObject baseSerialize(InstalledApp app)
    {
        JsonObject obj = new JsonObject();
        obj.addProperty("slug", app.getSlug());
        obj.addProperty("name", app.getMeta().name());
        obj.addProperty("author", app.getMeta().author());
        obj.addProperty("category", app.getMeta().category());

        JsonObject description = new JsonObject();
        description.addProperty("short", app.getMetaXml().shortDesc);
        description.addProperty("long", app.getMetaXml().longDesc);
        obj.add("description", description);

        JsonArray flags = new JsonArray();
        app.getMeta().flags().forEach(flag -> flags.add(flag.toString()));
        obj.add("flags", flags);

        obj.addProperty("package_type", app.getComputedInfo().packageType);
        obj.addProperty("release_date", app.getComputedInfo().releaseDate);

        JsonObject shopInfo = new JsonObject();
        shopInfo.addProperty("contents_size", app.getComputedInfo().shopContentsSize);
        shopInfo.addProperty("title_id", app.getTitleInfo().getTitleId());
        shopInfo.addProperty("inodes", app.getComputedInfo().inodes);
        shopInfo.addProperty("title_version", app.getTitleInfo().getVersion());
        shopInfo.addProperty("tmd_size", app.getComputedInfo().shopTmdSize);
        obj.add("shop", shopInfo);

        JsonArray subdirectories = new JsonArray();
        app.getComputedInfo().subdirectories.forEach(subdirectories::add);
        obj.add("subdirectories", subdirectories);

        obj.addProperty("version", app.getMetaXml().version);
        return obj;
    }

    private JsonObject buildAsset(String type, String url, String hash, long size)
    {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type);
        obj.addProperty("url" , url);
        if(hash != null)
            obj.addProperty("hash", hash);
        if(size > 0)
            obj.addProperty("size", size);
        return obj;
    }

    private JsonArray buildPeripherals(InstalledApp app)
    {
        JsonArray peripherals = new JsonArray();
        for(Map.Entry<Peripheral, Integer> entry : app.getPeripherals().entrySet())
        {
            JsonObject peripheral = new JsonObject();
            peripheral.addProperty("name", entry.getKey().name());
            peripheral.addProperty("display_name", entry.getKey().displayName());
            peripheral.addProperty("amount", entry.getValue());
            peripherals.add(peripheral);
        }
        return peripherals;
    }

    private JsonArray buildSupportedPlatforms(InstalledApp app)
    {
        JsonArray platforms = new JsonArray();
        for(Platform supportedPlatform : app.getSupportedPlatforms())
        {
            JsonObject platform = new JsonObject();
            platform.addProperty("name", supportedPlatform.name());
            platform.addProperty("display_name", supportedPlatform.displayName());
            platforms.add(platform);
        }
        return platforms;
    }
}
