package org.trivait.minigamesmod.minigame.chess;

import net.minecraft.text.Text;

public enum ChessBotMode {
    EASY(0, Text.translatable("minigame.chess.mode.easy")),
    NORMAL(1, Text.translatable("minigame.chess.mode.normal")),
    HARD(2, Text.translatable("minigame.chess.mode.hard")),
    CRAZY(3, Text.translatable("minigame.chess.mode.crazy")),
    ;

    public int mode;
    public Text name;

    ChessBotMode(int mode, Text name) {
        this.mode = mode;
        this.name = name;
    }

    @Override
    public String toString() {
        return name.getString();
    }
}