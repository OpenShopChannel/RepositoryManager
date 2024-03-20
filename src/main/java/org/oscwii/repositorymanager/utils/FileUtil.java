package org.oscwii.repositorymanager.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.oscwii.repositorymanager.model.app.OSCMeta.Source.Format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static void extractArchive(File archive, Format format, Path destination) throws IOException
    {
        ArchiveEntry entry;

        try(ArchiveInputStream<?> is = createArchiveInputStream(archive, format))
        {
            while((entry = is.getNextEntry()) != null)
            {
                if(entry.isDirectory())
                    continue;

                Path extractedPath = destination.resolve(entry.getName());
                Files.createDirectories(extractedPath.getParent());
                Files.copy(is, extractedPath);
            }
        }
    }

    private static ArchiveInputStream<?> createArchiveInputStream(File archive, Format format) throws IOException
    {
        FileInputStream fis = new FileInputStream(archive);
        return switch(format)
        {
            case ZIP   -> new ZipArchiveInputStream(fis);
            case TAR   -> new TarArchiveInputStream(fis);
            case GZTAR -> new TarArchiveInputStream(new GzipCompressorInputStream(fis));
            case XZTAR -> new TarArchiveInputStream(new XZCompressorInputStream(fis));
            case BZTAR -> new TarArchiveInputStream(new BZip2CompressorInputStream(fis));
            default    -> throw new UnsupportedOperationException("Unsupported archive format: " + format);
        };
    }
}
