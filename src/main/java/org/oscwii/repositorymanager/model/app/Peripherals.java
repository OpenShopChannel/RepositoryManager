package org.oscwii.repositorymanager.model.app;

public enum Peripherals
{
    WII_REMOTE         ("Wii Remote", "w"),
    NUNCHUK            ("Nunchuk", "n"),
    GAMECUBE_CONTROLLER("GameCube Controller", "g"),
    CLASSIC_CONTROLLER ("Classic Controller", "c"),
    SDHC               ("SDHC", "s"),
    USB_KEYBOARD       ("USB Keyboard", "k"),
    WII_ZAPPER         ("Wii Zapper", "z"),
    UNKNOWN            ("Unknown", "");

    private final String displayName, hbbKey;

    Peripherals(String displayName, String hbbKey)
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
}
