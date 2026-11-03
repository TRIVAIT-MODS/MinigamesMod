package org.trivait.minigamesmod.minigame.chess;

import net.minecraft.text.Text;

public enum ChessRenderMode {
    CLASSIC(0, Text.translatable("minigame.chess.mode.classic")),
    WOODEN(1, Text.translatable("minigame.chess.mode.wooden"))
    ;

    public int mode;
    public Text name;

    ChessRenderMode(int mode, Text name) {
        this.mode = mode;
        this.name = name;
    }

    @Override
    public String toString() {
        return name.getString();
    }
}