package org.oscwii.repositorymanager.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.springframework.util.Assert;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class OSCMetaTypeAdapter implements JsonDeserializer<OSCMeta>
{
    @Override
    public OSCMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        Assert.isTrue(json.isJsonObject(), "OSCMeta must be a JSON object!");
        JsonObject obj = json.getAsJsonObject();

        JsonObject info = obj.getAsJsonObject("information");
        JsonArray platformsArr = info.getAsJsonArray("supported_platforms");
        JsonArray peripheralsArr = info.getAsJsonArray("peripherals");
        JsonArray flagsArr = info.getAsJsonArray("flags");

        List<String> platforms = platformsArr == null ? Collections.emptyList() :
                context.deserialize(platformsArr, new TypeToken<List<String>>(){}.getType());
        List<String> peripherals = peripheralsArr == null ? Collections.emptyList() :
                context.deserialize(peripheralsArr, new TypeToken<List<String>>(){}.getType());
        EnumSet<OSCMeta.Flag> flags = flagsArr == null ? EnumSet.noneOf(OSCMeta.Flag.class) :
                context.deserialize(flagsArr, new TypeToken<EnumSet<OSCMeta.Flag>>(){}.getType());

        return new OSCMeta(
                Objects.requireNonNull(info.get("name")).getAsString(),
                Objects.requireNonNull(info.get("author")).getAsString(),
                Objects.requireNonNull(info.get("category")).getAsString(),
                info.has("author_preferred_contact") ? info.get("author_preferred_contact").getAsString() : null,
                platforms, peripherals,
                Objects.requireNonNull(info.getAsJsonPrimitive("version")).getAsString(),
                flags);
    }
}
