package com.rasmus.booksmart.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "booksmart")
public class BookSmartConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean stickyCrafting = true;

    public enum ShiftClickMode {
        VANILLA,
        ONE_STACK,
        ALL_WHILE_HELD,
        ALL
    }

    @ConfigEntry.Gui.Tooltip(count = 4)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public ShiftClickMode shiftClickCrafts = ShiftClickMode.ALL_WHILE_HELD;

    public enum DropMode {
        STOP,
        DROP_EXCESS,
        DROP_ALL
    }

    @ConfigEntry.Gui.Tooltip(count = 3)
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public DropMode dropMode = DropMode.DROP_EXCESS;

    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean rememberVariants = true;

    @ConfigEntry.Gui.Excluded
    public boolean introShown = false;

    @ConfigEntry.Gui.Excluded
    public java.util.Map<String, String> preferredVariants = new java.util.HashMap<>();
}
