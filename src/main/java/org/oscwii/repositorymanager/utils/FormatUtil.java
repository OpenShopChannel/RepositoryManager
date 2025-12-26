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

package org.oscwii.repositorymanager.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtil
{
    public static String baseUrl = "";

    public static String secondsToTime(long timeseconds)
    {
        StringBuilder builder = new StringBuilder();

        int years = (int) (timeseconds / (60 * 60 * 24 * 365));
        if(years > 0)
        {
            builder.append(years).append(" years, ");
            timeseconds = timeseconds % (60 * 60 * 24 * 365);
        }

        int weeks = (int) (timeseconds / (60 * 60 * 24 * 365));
        if(weeks > 0)
        {
            builder.append(weeks).append(" weeks, ");
            timeseconds = timeseconds % (60 * 60 * 24 * 7);
        }

        int days = (int) (timeseconds / (60 * 60 * 24));
        if(days > 0)
        {
            builder.append(days).append(" days, ");
            timeseconds = timeseconds % (60 * 60 * 24);
        }

        int hours = (int) (timeseconds / (60 * 60));
        if(hours > 0)
        {
            builder.append(hours).append(" hours, ");
            timeseconds = timeseconds % (60 * 60);
        }

        int minutes = (int) (timeseconds / (60));
        if(minutes > 0)
        {
            builder.append(minutes).append(" minutes, ");
            timeseconds = timeseconds % (60);
        }

        if(timeseconds > 0)
            builder.append(timeseconds).append(" seconds");

        String str = builder.toString();
        if(str.endsWith(", "))
            str = str.substring(0, str.length() - 2);
        if(str.isEmpty())
            str = "No time";

        return str;
    }

    public static String binaryUrl(String slug, String type)
    {
        return publicUrl(BINARY_PATH.formatted(slug, slug, type));
    }

    public static String iconUrl(String slug)
    {
        return publicUrl(ICON_PATH.formatted(slug));
    }

    public static String logUrl(String log)
    {
        return LOG_PATH.formatted(log);
    }

    public static String metaXmlUrl(String slug)
    {
        return publicUrl(META_XML.formatted(slug, slug));
    }

    public static String zipUrl(String slug, boolean publicUrl)
    {
        String s = ZIP_PATH.formatted(slug, slug);
        if(publicUrl)
            s = baseUrl + s;
        return s;
    }

    public static String publicUrl(String path)
    {
        return baseUrl + path;
    }

    public static String formatDate(Date date)
    {
        return DATE_FORMAT.format(date);
    }

    private static final String BINARY_PATH = "/unzipped_apps/%s/apps/%s/boot.%s";
    private static final String ICON_PATH   = "/api/contents/%s/icon.png";
    private static final String LOG_PATH    = "/admin/log/%s";
    private static final String META_XML    = "/unzipped_apps/%s/apps/%s/meta.xml";
    private static final String ZIP_PATH    = "/api/contents/%s/%s.zip";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
