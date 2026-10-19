package com.rasmus.booksmart;

import com.mojang.blaze3d.platform.InputConstants;

/** 26.3 numbers the left mouse button 1. Overlays the 26.1/26.2 class of the same name. */
public final class MouseButtons {
    private MouseButtons() {
    }

    public static boolean isLeft(int button) {
        return button == InputConstants.MOUSE_BUTTON_LEFT;
    }
}
