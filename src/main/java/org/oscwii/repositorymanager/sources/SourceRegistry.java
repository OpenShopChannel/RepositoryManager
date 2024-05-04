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
