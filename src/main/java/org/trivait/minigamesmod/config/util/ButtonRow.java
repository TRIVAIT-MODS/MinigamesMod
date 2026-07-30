package org.trivait.minigamesmod.config.util;

public enum ButtonRow {
    SINGLEPLAYER(24),
    MULTIPLAYER(48),
    REALMS(72),
    SETTINGS(120);

    private final int yOffset;

    ButtonRow(int yOffset) {
        this.yOffset = yOffset;
    }

    int getYOffset() {
        return yOffset;
    }
}