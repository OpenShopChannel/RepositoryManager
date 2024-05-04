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

package org.oscwii.repositorymanager.logging;

import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rolling.AbstractTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "IndexTriggeringPolicy", category = Core.CATEGORY_NAME)
public class IndexTriggeringPolicy extends AbstractTriggeringPolicy
{
    private RollingFileManager manager;

    @Override
    public void initialize(RollingFileManager manager)
    {
        this.manager = manager;

        manager.skipFooter(true);
        trigger();
        manager.skipFooter(false);
    }

    @Override
    public boolean isTriggeringEvent(LogEvent logEvent)
    {
        return false;
    }

    public void trigger()
    {
        manager.rollover();
    }

    @PluginFactory
    public static IndexTriggeringPolicy createPolicy()
    {
        return INSTANCE;
    }

    public static IndexTriggeringPolicy INSTANCE = new IndexTriggeringPolicy();
}
