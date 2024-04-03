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
