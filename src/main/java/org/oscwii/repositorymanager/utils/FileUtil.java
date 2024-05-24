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

import com.github.junrar.Junrar;
import com.github.junrar.exception.RarException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.oscwii.repositorymanager.exceptions.QuietException;
import org.oscwii.repositorymanager.model.app.OSCMeta.Source.Format;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.function.Predicate;
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
        if(format == Format.SEVEN_ZIP)
            extract7z(archive, destination);
        else if(format == Format.RAR)
            extractRar(archive, destination);
        else
            extractGeneralArchive(archive, format, destination);
    }

    public static void zipDirectory(Path source, Path destination) throws IOException
    {
        Files.createDirectories(destination.getParent());
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

    public static List<File> getFiles(Path dir, int depth, Predicate<Path> filter) throws IOException
    {
        try(Stream<Path> stream = Files.walk(dir, depth))
        {
            return stream.filter(filter).map(Path::toFile).toList();
        }
    }

    public static ResponseEntity<Resource> getContent(Path path)
    {
        if(Files.notExists(path))
            return ResponseEntity.notFound().build();

        FileSystemResource res = new FileSystemResource(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.toFile().getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(res);
    }

    private static ArchiveInputStream<?> createArchiveInputStream(File archive, Format format) throws IOException
    {
        FileInputStream fis = new FileInputStream(archive);
        return switch(format)
        {
            case ZIP   -> new ZipArchiveInputStream(fis);
            case TAR   -> new TarArchiveInputStream(fis);
            case BZTAR -> new TarArchiveInputStream(new BZip2CompressorInputStream(fis));
            case GZTAR -> new TarArchiveInputStream(new GzipCompressorInputStream(fis));
            case XZTAR -> new TarArchiveInputStream(new XZCompressorInputStream(fis));
            default    -> throw new UnsupportedOperationException("Unsupported archive format: " + format);
        };
    }

    private static void extract7z(File archive, Path destination) throws IOException
    {
        SevenZFile sevenZArchive = SevenZFile.builder().setFile(archive).get();
        SevenZArchiveEntry entry;

        try(sevenZArchive)
        {
            while((entry = sevenZArchive.getNextEntry()) != null)
            {
                if(entry.isDirectory())
                    continue;

                extractEntry(sevenZArchive.getInputStream(entry), destination, entry);
            }
        }
    }

    private static void extractRar(File archive, Path destination) throws IOException
    {
        try
        {
            // Man it should be this easy for all formats...
            // Ironically, the most obscure and proprietary format is the easiest to work with
            Junrar.extract(archive, destination.toFile());
        }
        catch(RarException e)
        {
            throw new QuietException("Failed to unrar archive: " + archive.getName(), e);
        }
    }

    private static void extractGeneralArchive(File archive, Format format, Path destination) throws IOException
    {
        ArchiveEntry entry;

        try(ArchiveInputStream<?> is = createArchiveInputStream(archive, format))
        {
            while((entry = is.getNextEntry()) != null)
            {
                if(entry.isDirectory())
                    continue;

                extractEntry(is, destination, entry);
            }
        }
    }

    private static void extractEntry(InputStream is, Path destination, ArchiveEntry entry) throws IOException
    {
        Path extractedPath = destination.resolve(entry.getName());
        Files.createDirectories(extractedPath.getParent());
        Files.copy(is, extractedPath);
    }
}
