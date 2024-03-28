package org.oscwii.repositorymanager.model.app;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class PersistentAppInfo
{
    private final String slug;

    private int version;
    private String titleId;

    public PersistentAppInfo(@ColumnName("app_slug") String slug,
                             int version, @ColumnName("title_id") String titleId)
    {
        this.slug = slug;
        this.version = version;
        this.titleId = titleId;
    }

    public String getSlug()
    {
        return slug;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public String getTitleId()
    {
        return titleId;
    }

    public void setTitleId(String titleId)
    {
        this.titleId = titleId;
    }

    @Override
    public String toString()
    {
        return "PersistentAppInfo{" +
                "slug='" + slug + '\'' +
                ", version=" + version +
                ", titleId='" + titleId + '\'' +
                '}';
    }
}
