package com.rasmus.booksmart;

import com.rasmus.booksmart.config.BookSmartConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * A row of four small buttons under the recipe book that picks what a
 * shift click crafts, so the mode lives where it is used instead of in
 * a config screen. The active mode is highlighted; clicking writes the
 * config, so the strip and Mod Menu stay one and the same setting.
 */
public final class ModeStrip {

    private static final BookSmartConfig.ShiftClickMode[] MODES = {
            BookSmartConfig.ShiftClickMode.VANILLA,
            BookSmartConfig.ShiftClickMode.ONE_STACK,
            BookSmartConfig.ShiftClickMode.ALL_WHILE_HELD,
            BookSmartConfig.ShiftClickMode.ALL,
    };
    private static final String[] LABELS = {"Off", "1x", "Hold", "All"};
    private static final BookSmartConfig.DropMode[] DROP_MODES = {
            BookSmartConfig.DropMode.STOP,
            BookSmartConfig.DropMode.DROP_EXCESS,
            BookSmartConfig.DropMode.DROP_ALL,
    };
    private static final String[] DROP_LABELS = {"Stop", "Excess", "Drop all"};
    private static final String[] TOOLTIPS = {
            "Vanilla shift click: fills the grid, crafts nothing",
            "Craft one stack into your inventory",
            "Craft everything while shift is held",
            "One click crafts everything",
    };
    private static final String[] DROP_TOOLTIPS = {
            "Stop when the inventory is full",
            "Bank what fits, drop the rest on the ground",
            "Drop everything on the ground, never banking",
    };
    private static final int[] DROP_WIDTHS = {40, 48, 54};
    private static final int BUTTON_WIDTH = 35;
    private static final int BUTTON_HEIGHT = 13;
    private static final int GAP = 2;
    private static final int STRIP_WIDTH = 4 * BUTTON_WIDTH + 3 * GAP;
    // The book panel is 147x166 from its origin.
    private static final int OFFSET_X = (147 - STRIP_WIDTH) / 2;
    private static final int OFFSET_Y = 166 + 2;
    private static final int ROW2_Y = OFFSET_Y + BUTTON_HEIGHT + GAP;
    private static final int ROW3_Y = ROW2_Y + BUTTON_HEIGHT + GAP;

    private ModeStrip() {
    }

    private static int buttonX(int xo, int index) {
        return xo + OFFSET_X + index * (BUTTON_WIDTH + GAP);
    }

    private static int dropButtonX(int xo, int index) {
        int total = DROP_WIDTHS[0] + DROP_WIDTHS[1] + DROP_WIDTHS[2] + 2 * GAP;
        int x = xo + (147 - total) / 2;
        for (int i = 0; i < index; i++) {
            x += DROP_WIDTHS[i] + GAP;
        }
        return x;
    }

    private static void drawButton(GuiGraphicsExtractor graphics, int x, int y, int width,
            String label, String tooltip, boolean isActive, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + BUTTON_HEIGHT;
        if (hovered && tooltip != null) {
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font,
                    Component.literal(tooltip), mouseX, mouseY);
        }
        graphics.fill(x, y, x + width, y + BUTTON_HEIGHT,
                isActive ? 0xE8404550 : 0xC0202329);
        graphics.outline(x, y, width, BUTTON_HEIGHT,
                isActive ? 0xFFFFD75F : (hovered ? 0xFFFFFFFF : 0xFF5A5F6A));
        graphics.centeredText(Minecraft.getInstance().font, label, x + width / 2, y + 3,
                isActive ? 0xFFFFD75F : (hovered ? 0xFFFFFFFF : 0xFFB8BCC4));
    }

    public static void render(GuiGraphicsExtractor graphics, int xo, int yo, int mouseX, int mouseY) {
        BookSmartConfig.ShiftClickMode active = BookSmart.shiftClickMode();
        for (int i = 0; i < MODES.length; i++) {
            drawButton(graphics, buttonX(xo, i), yo + OFFSET_Y, BUTTON_WIDTH,
                    LABELS[i], TOOLTIPS[i], MODES[i] == active, mouseX, mouseY);
        }
        BookSmartConfig.DropMode drop = BookSmart.dropMode();
        for (int i = 0; i < DROP_MODES.length; i++) {
            drawButton(graphics, dropButtonX(xo, i), yo + ROW2_Y, DROP_WIDTHS[i],
                    DROP_LABELS[i], DROP_TOOLTIPS[i], DROP_MODES[i] == drop, mouseX, mouseY);
        }
        drawButton(graphics, xo + OFFSET_X, yo + ROW3_Y, STRIP_WIDTH,
                "Book settings", "Open the Book Smart config", false, mouseX, mouseY);
    }

    /**
     * The screen setter was renamed setScreen -> setScreenAndShow in 26.2,
     * so resolve it by name at runtime; the runtime uses Mojang names, so
     * both spellings are reachable. Reflection is safer than signature
     * matching here, since clearClientLevel shares the same signature.
     */
    private static void openScreen(Minecraft minecraft, net.minecraft.client.gui.screens.Screen screen) {
        for (String name : new String[] {"setScreenAndShow", "setScreen"}) {
            try {
                minecraft.getClass().getMethod(name, net.minecraft.client.gui.screens.Screen.class)
                        .invoke(minecraft, screen);
                return;
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

    /** Returns true when the click landed on the strip and was handled. */
    public static boolean mouseClicked(double mouseX, double mouseY, int xo, int yo) {
        int rowY = yo + OFFSET_Y;
        if (mouseY >= rowY && mouseY < rowY + BUTTON_HEIGHT) {
            for (int i = 0; i < MODES.length; i++) {
                int x = buttonX(xo, i);
                if (mouseX >= x && mouseX < x + BUTTON_WIDTH) {
                    BookSmart.setShiftClickMode(MODES[i]);
                    return true;
                }
            }
            return false;
        }
        rowY = yo + ROW2_Y;
        if (mouseY >= rowY && mouseY < rowY + BUTTON_HEIGHT) {
            for (int i = 0; i < DROP_MODES.length; i++) {
                int x = dropButtonX(xo, i);
                if (mouseX >= x && mouseX < x + DROP_WIDTHS[i]) {
                    BookSmart.setDropMode(DROP_MODES[i]);
                    return true;
                }
            }
            return false;
        }
        rowY = yo + ROW3_Y;
        if (mouseY >= rowY && mouseY < rowY + BUTTON_HEIGHT
                && mouseX >= xo + OFFSET_X && mouseX < xo + OFFSET_X + STRIP_WIDTH) {
            Minecraft minecraft = Minecraft.getInstance();
            // The container must be closed properly before switching
            // screens, or its menu is left dangling server side.
            if (minecraft.player != null) {
                minecraft.player.closeContainer();
            }
            openScreen(minecraft, AutoConfigClient.getConfigScreen(
                    com.rasmus.booksmart.config.BookSmartConfig.class, null).get());
            return true;
        }
        return false;
    }
}
