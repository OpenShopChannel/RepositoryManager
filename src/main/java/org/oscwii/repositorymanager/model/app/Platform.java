package org.oscwii.repositorymanager.model.app;

import com.google.gson.annotations.SerializedName;

public record Platform(String name, @SerializedName("display_name") String displayName)
{
}
