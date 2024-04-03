package org.oscwii.repositorymanager.utils;

import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
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

    private static final String TID_PREFIX = "000100014E";
}
