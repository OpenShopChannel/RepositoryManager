package org.oscwii.repositorymanager.model.app;

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
}
