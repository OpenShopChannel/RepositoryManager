package org.oscwii.repositorymanager.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.oscwii.repositorymanager.model.app.OSCMeta.Source.Format;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

    public static void zipDirectory(Path source, Path destination) throws IOException
    {
        try(Stream<Path> stream = Files.walk(source);
            ZipArchiveOutputStream zos = new ZipArchiveOutputStream(destination))
        {
            List<Path> files = stream.toList();
            for(Path path : files)
            {
                if(path.toFile().isFile())
                {
                    String entryPath = source.relativize(path).toFile().getPath();
                    ZipArchiveEntry entry = zos.createArchiveEntry(path, entryPath);
                    zos.putArchiveEntry(entry);

                    try(InputStream fis = Files.newInputStream(path))
                    {
                        IOUtils.copy(fis, zos);
                    }
                    finally
                    {
                        zos.closeArchiveEntry();
                    }
                }
            }
            
            zos.finish();
        }
    }

    public static String md5Hash(Path file) throws IOException
    {
        byte[] bytes = Files.readAllBytes(file);
        return DigestUtils.md5DigestAsHex(bytes);
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
