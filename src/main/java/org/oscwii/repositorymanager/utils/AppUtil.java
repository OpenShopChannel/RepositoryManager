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

package org.oscwii.repositorymanager.utils;

import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.exceptions.QuietException;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    public static void generateBanners(Logger logger, Path generator, Path contentsDir, Path workingDir) throws IOException
    {
        Process proc = new ProcessBuilder(List.of(generator.toString(), contentsDir.toString() + File.separatorChar))
                .directory(workingDir.toFile())
                .start();

        try(BufferedReader in = proc.inputReader();
            BufferedReader errIn = proc.errorReader())
        {
            int exitCode;
            String line;

            while((line = in.readLine()) != null)
                logger.trace("GENERATOR OUTPUT: {}", line);

            exitCode = proc.waitFor();
            if(exitCode != 0)
                throw new QuietException("Failure while creating banner: " + errIn.readLine());
        }
        catch(InterruptedException e)
        {
            throw new QuietException("Banner creation was interrupted", e);
        }
    }

    private static final String TID_PREFIX = "000100014E";
}
