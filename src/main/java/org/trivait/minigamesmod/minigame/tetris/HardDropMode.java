package org.trivait.minigamesmod.minigame.tetris;

import net.minecraft.network.chat.Component;

public enum HardDropMode {
    OFF(0, Component.translatable("minigame.tetris.mode.off")),
    INSTANT(1, Component.translatable("minigame.tetris.mode.instant")),
    OUTLINE(2, Component.translatable("minigame.tetris.mode.outline")),
    GHOST(3, Component.translatable("minigame.tetris.mode.ghost"));

    public int mode;
    public Component name;

    HardDropMode(int mode, Component name) {
        this.mode = mode;
        this.name = name;
    }

    @Override
    public String toString() {
        return name.getString();
    }
}