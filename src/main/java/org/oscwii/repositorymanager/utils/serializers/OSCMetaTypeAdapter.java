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

package org.oscwii.repositorymanager.utils.serializers;

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
        JsonArray authorsArr = info.getAsJsonArray("authors");
        JsonArray contributorsArr = info.getAsJsonArray("contributors");
        JsonArray platformsArr = info.getAsJsonArray("supported_platforms");
        JsonArray peripheralsArr = info.getAsJsonArray("peripherals");
        JsonArray flagsArr = info.getAsJsonArray("flags");
        JsonArray treatmentsArr = obj.getAsJsonArray("treatments");

        List<String> platforms = platformsArr == null ? Collections.emptyList() :
                context.deserialize(platformsArr, new TypeToken<List<String>>(){}.getType());
        List<String> peripherals = peripheralsArr == null ? Collections.emptyList() :
                context.deserialize(peripheralsArr, new TypeToken<List<String>>(){}.getType());
        EnumSet<OSCMeta.Flag> flags = flagsArr == null ? EnumSet.noneOf(OSCMeta.Flag.class) :
                context.deserialize(flagsArr, new TypeToken<EnumSet<OSCMeta.Flag>>(){}.getType());
        List<OSCMeta.Treatment> treatments = treatmentsArr == null ? Collections.emptyList() :
                context.deserialize(treatmentsArr, new TypeToken<List<OSCMeta.Treatment>>(){}.getType());

        return new OSCMeta(
                Objects.requireNonNull(info.get("name")).getAsString(),
                Objects.requireNonNull(info.get("author")).getAsString(),
                authorsArr == null ? new String[0] : toArray(authorsArr),
                Objects.requireNonNull(info.get("category")).getAsString(),
                info.has("author_preferred_contact") ? info.get("author_preferred_contact").getAsString() : null,
                contributorsArr == null ? new String[0] : toArray(contributorsArr),
                platforms, peripherals,
                Objects.requireNonNull(info.getAsJsonPrimitive("version")).getAsString(),
                flags,
                context.deserialize(obj.getAsJsonObject("source"), OSCMeta.Source.class),
                treatments);
    }

    private String[] toArray(JsonArray arr)
    {
        return arr.asList().stream().map(JsonElement::getAsString).toArray(String[]::new);
    }
}
