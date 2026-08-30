package com.rasmus.booksmart;

import com.rasmus.booksmart.config.BookSmartConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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
    private static final int BUTTON_WIDTH = 35;
    private static final int BUTTON_HEIGHT = 13;
    private static final int GAP = 2;
    private static final int STRIP_WIDTH = 4 * BUTTON_WIDTH + 3 * GAP;
    // The book panel is 147x166 from its origin.
    private static final int OFFSET_X = (147 - STRIP_WIDTH) / 2;
    private static final int OFFSET_Y = 166 + 2;

    private ModeStrip() {
    }

    private static int buttonX(int xo, int index) {
        return xo + OFFSET_X + index * (BUTTON_WIDTH + GAP);
    }

    public static void render(GuiGraphicsExtractor graphics, int xo, int yo, int mouseX, int mouseY) {
        var font = Minecraft.getInstance().font;
        int y = yo + OFFSET_Y;
        BookSmartConfig.ShiftClickMode active = BookSmart.shiftClickMode();
        for (int i = 0; i < MODES.length; i++) {
            int x = buttonX(xo, i);
            boolean isActive = MODES[i] == active;
            boolean hovered = mouseX >= x && mouseX < x + BUTTON_WIDTH
                    && mouseY >= y && mouseY < y + BUTTON_HEIGHT;
            graphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT,
                    isActive ? 0xE8404550 : 0xC0202329);
            graphics.outline(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                    isActive ? 0xFFFFD75F : (hovered ? 0xFFFFFFFF : 0xFF5A5F6A));
            graphics.centeredText(font, LABELS[i], x + BUTTON_WIDTH / 2, y + 3,
                    isActive ? 0xFFFFD75F : (hovered ? 0xFFFFFFFF : 0xFFB8BCC4));
        }
    }

    /** Returns true when the click landed on the strip and was handled. */
    public static boolean mouseClicked(double mouseX, double mouseY, int xo, int yo) {
        int y = yo + OFFSET_Y;
        if (mouseY < y || mouseY >= y + BUTTON_HEIGHT) {
            return false;
        }
        for (int i = 0; i < MODES.length; i++) {
            int x = buttonX(xo, i);
            if (mouseX >= x && mouseX < x + BUTTON_WIDTH) {
                BookSmart.setShiftClickMode(MODES[i]);
                return true;
            }
        }
        return false;
    }
}
