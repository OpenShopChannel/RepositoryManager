package org.oscwii.repositorymanager.utils;

import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

public class AppUtil
{
    public static String generateTID()
    {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int rand = random.nextInt(0x000000, 0xFFFFFF);

        // Convert to hexadecimal
        String randHex = Integer.toHexString(rand);

        // Add zeros if necessary
        if(randHex.length() < 6)
        {
            int diff = 6 - randHex.length();
            randHex = randHex + "0".repeat(diff);
        }

        return TID_PREFIX + randHex.toUpperCase();
    }

    public static ResponseEntity<ByteArrayResource> getIcon(InstalledApp app) throws IOException
    {
        byte[] image = Files.readAllBytes(app.getAppFilesPath().resolve("icon.png"));
        return ResponseEntity.ok()
                .contentLength(image.length)
                .contentType(MediaType.IMAGE_PNG)
                .body(new ByteArrayResource(image));
    }

    public static ResponseEntity<Resource> getContent(Path path)
    {
        if(Files.notExists(path))
            return ResponseEntity.notFound().build();

        FileSystemResource res = new FileSystemResource(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.toFile().getName() + "\"")
                .body(res);
    }

    private static final String TID_PREFIX = "000100014E";
}
