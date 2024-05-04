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

package org.oscwii.repositorymanager.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.oscwii.repositorymanager.model.app.OSCMeta;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

public class SourceTypeAdapter implements JsonDeserializer<OSCMeta.Source>
{
    @Override
    public OSCMeta.Source deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject obj = json.getAsJsonObject();
        String type = obj.get("type").getAsString();
        String url;
        String file = obj.has("file") ? obj.get("file").getAsString() : null;

        if(type.equalsIgnoreCase("manual"))
            url = null;
        else if(obj.has("location"))
            url = obj.get("location").getAsString();
        else if(obj.has("repository"))
            url = obj.get("repository").getAsString();
        else if(obj.has("project"))
            url = obj.get("project").getAsString();
        else if(type.equalsIgnoreCase("itchio"))
        {
            String creator = obj.get("creator").getAsString();
            String game = obj.get("game").getAsString();
            url = creator + "/" + game;
            file = obj.get("upload").getAsString();
        }
        else
            throw new IllegalStateException("Could not determine web location for source!");

        JsonArray additionalFilesArr = obj.getAsJsonArray("additional_files");
        Set<String> additionalFiles = additionalFilesArr == null ? Collections.emptySet() :
                context.deserialize(additionalFilesArr, new TypeToken<>(){}.getType());

        return new OSCMeta.Source(type,
                context.deserialize(obj.get("format"), OSCMeta.Source.Format.class),
                url,
                file,
                obj.has("user-agent") ? obj.get("user-agent").getAsString() : null,
                additionalFiles);
    }
}
