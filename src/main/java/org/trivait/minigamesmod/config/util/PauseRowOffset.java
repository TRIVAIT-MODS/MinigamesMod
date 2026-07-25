package org.trivait.minigamesmod.config.util;

public enum PauseRowOffset {
    SAME(0),
    NEXT(24),
    TWO_DOWN(48),
    THREE_DOWN(72),
    FOUR_DOWN(96);

    private final int yOffset;

    PauseRowOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    int getYOffset() {
        return yOffset;
    }
}