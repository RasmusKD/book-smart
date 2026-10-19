package com.rasmus.booksmart;

/** 26.1 and 26.2 number the left mouse button 0. The 26.3 build overlays this class (it uses 1). */
public final class MouseButtons {
    private MouseButtons() {
    }

    public static boolean isLeft(int button) {
        return button == 0;
    }
}
