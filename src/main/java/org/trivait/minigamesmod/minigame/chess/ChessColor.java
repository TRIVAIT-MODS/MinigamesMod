package org.trivait.minigamesmod.minigame.chess;

public enum ChessColor {
    WHITE,
    BLACK;

    public ChessColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
