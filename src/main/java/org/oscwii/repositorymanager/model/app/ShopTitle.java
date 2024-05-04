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

package org.oscwii.repositorymanager.model.app;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class ShopTitle
{
    private final String slug;

    private int version;
    private String titleId;

    public ShopTitle(@ColumnName("app_slug") String slug,
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
        return "ShopTitle{" +
                "slug='" + slug + '\'' +
                ", version=" + version +
                ", titleId='" + titleId + '\'' +
                '}';
    }
}
