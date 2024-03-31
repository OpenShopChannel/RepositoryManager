package org.oscwii.repositorymanager.model.app;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.gson.annotations.SerializedName;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Category(String name, @SerializedName("display_name") String displayName,
                       String singular, String plural)
{
    @Override
    public String toString()
    {
        return displayName;
    }
}
