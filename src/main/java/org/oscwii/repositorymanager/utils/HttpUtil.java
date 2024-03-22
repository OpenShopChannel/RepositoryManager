package org.oscwii.repositorymanager.utils;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class HttpUtil
{
    public static void downloadFile(OkHttpClient httpClient, Request request, Path destination) throws IOException
    {
        Call call = httpClient.newCall(request);
        try(Response response = call.execute())
        {
            if(!response.isSuccessful())
                throw new QuietException("HTTP request failed with status code " + response.code());

            Assert.notNull(response.body(), "Response body is null!");
            Files.copy(response.body().byteStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
