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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.oscwii.repositorymanager.exceptions.QuietException;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class HttpUtil
{
    public static void downloadFile(OkHttpClient httpClient, Request request, Path destination) throws IOException
    {
        try(Response response = fetchSite(httpClient, request))
        {
            Assert.notNull(response.body(), "Response body is null!");
            Files.copy(response.body().byteStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static JsonObject fetchJson(Gson gson, OkHttpClient httpClient, Request request) throws IOException
    {
        try(Response response = fetchSite(httpClient, request))
        {
            Assert.notNull(response.body(), "Response body is null!");
            return gson.fromJson(new InputStreamReader(response.body().byteStream()), JsonObject.class);
        }
    }

    public static Response fetchSite(OkHttpClient httpClient, Request request) throws IOException
    {
        Call call = httpClient.newCall(request);
        Response response = call.execute();
        if(!response.isSuccessful())
            throw new QuietException("HTTP request failed with status code " + response.code());

        return response;
    }
}
