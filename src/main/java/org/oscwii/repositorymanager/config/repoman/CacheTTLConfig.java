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

package org.oscwii.repositorymanager.config.repoman;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "repository-manager.cache-ttl")
public class CacheTTLConfig
{
    private static Duration iconDuration;
    private static Duration packageDuration;

    public static Duration getIconDuration()
    {
        return iconDuration;
    }

    public void setIcon(long duration)
    {
        CacheTTLConfig.iconDuration = Duration.ofSeconds(duration);
    }

    public static Duration getPackageDuration()
    {
        return packageDuration;
    }

    public void setPackage(long duration)
    {
        CacheTTLConfig.packageDuration = Duration.ofSeconds(duration);
    }
}
