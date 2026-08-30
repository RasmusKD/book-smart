package com.rasmus.booksmart.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "booksmart")
public class BookSmartConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean stickyCrafting = true;

    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean craftAllOnShiftClick = true;

    public enum DropMode {
        STOP,
        DROP_EXCESS,
        DROP_ALL
    }

    @ConfigEntry.Gui.Tooltip(count = 3)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public DropMode dropMode = DropMode.STOP;

    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean rememberVariants = true;

    @ConfigEntry.Gui.Excluded
    public boolean introShown = false;

    @ConfigEntry.Gui.Excluded
    public java.util.Map<String, String> preferredVariants = new java.util.HashMap<>();
}
