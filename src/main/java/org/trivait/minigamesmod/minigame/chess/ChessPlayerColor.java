package org.trivait.minigamesmod.minigame.chess;

import net.minecraft.text.Text;

public enum ChessPlayerColor {
    WHITE(0, Text.translatable("minigame.chess.color.white")),
    BLACK(1, Text.translatable("minigame.chess.color.black")),
    RANDOM(2, Text.translatable("minigame.chess.color.random"));

    public int mode;
    public Text name;

    ChessPlayerColor(int mode, Text name) {
        this.mode = mode;
        this.name = name;
    }

    @Override
    public String toString() {
        return name.getString();
    }
}
