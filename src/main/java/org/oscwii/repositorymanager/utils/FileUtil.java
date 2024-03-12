package org.oscwii.repositorymanager.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.function.Consumer;

public class FileUtil
{
    public static <T> T loadJson(File file, Class<T> type, Gson gson, Consumer<Exception> exceptionHandler)
    {
        return loadJson(file, TypeToken.get(type), gson, exceptionHandler);
    }

    public static <T> T loadJson(File file, TypeToken<T> type, Gson gson, Consumer<Exception> exceptionHandler)
    {
        try(Reader reader = new FileReader(file))
        {
            return gson.fromJson(reader, type);
        }
        catch(Exception e)
        {
            exceptionHandler.accept(e);
            return null;
        }
    }
}
