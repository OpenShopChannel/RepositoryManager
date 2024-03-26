package org.oscwii.repositorymanager.utils;

public class FormatUtil
{
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
}
