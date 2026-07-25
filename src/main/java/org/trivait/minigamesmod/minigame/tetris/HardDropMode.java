package org.trivait.minigamesmod.minigame.tetris;

import net.minecraft.text.Text;

public enum HardDropMode {
    OFF(0, Text.translatable("minigame.tetris.mode.off")),
    INSTANT(1, Text.translatable("minigame.tetris.mode.instant")),
    OUTLINE(2, Text.translatable("minigame.tetris.mode.outline")),
    GHOST(3, Text.translatable("minigame.tetris.mode.ghost"));

    public int mode;
    public Text name;

    HardDropMode(int mode, Text name) {
        this.mode = mode;
        this.name = name;
    }

    @Override
    public String toString() {
        return name.getString();
    }
}