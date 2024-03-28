package org.oscwii.repositorymanager.utils;

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

    private static final String TID_PREFIX = "000100014E";
}
