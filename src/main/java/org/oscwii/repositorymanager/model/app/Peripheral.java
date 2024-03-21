package org.oscwii.repositorymanager.model.app;

import java.util.Map;

public enum Peripheral
{
    WII_REMOTE("Wii Remote", "w"),
    NUNCHUK("Nunchuk", "n"),
    GAMECUBE_CONTROLLER("GameCube Controller", "g"),
    CLASSIC_CONTROLLER("Classic Controller", "c"),
    SDHC("SDHC", "s"),
    USB_KEYBOARD("USB Keyboard", "k"),
    WII_ZAPPER("Wii Zapper", "z"),
    UNKNOWN("Unknown", "");

    private final String displayName, hbbKey;

    Peripheral(String displayName, String hbbKey)
    {
        this.displayName = displayName;
        this.hbbKey = hbbKey;
    }

    public String displayName()
    {
        return displayName;
    }

    public String key()
    {
        return hbbKey;
    }

    public static Peripheral fromDisplay(String displayName)
    {
        for(Peripheral peripheral : values())
        {
            if(peripheral.displayName().equalsIgnoreCase(displayName))
                return peripheral;
        }

        return UNKNOWN;
    }

    public static String buildHBBList(Map<Peripheral, Integer> peripherals)
    {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Peripheral, Integer> entry : peripherals.entrySet())
        {
            Peripheral peripheral = entry.getKey();
            int amount = entry.getValue();
            sb.append(peripheral.key().repeat(Math.max(1, amount)));
        }
        return sb.toString();
    }
}
