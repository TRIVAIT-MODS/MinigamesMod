package org.trivait.minigamesmod.minigame.minesweeper.leaderboard;

import org.trivait.minigamesmod.minigame.minesweeper.game.GameSettings;

public enum BoardCategory {
    S8x8("8x8", 8),
    S16x16("16x16", 30),
    S26x18("26x18", 65);

    public final String label;
    public final int mines;

    BoardCategory(String label, int mines) {
        this.label = label;
        this.mines = mines;
    }

    public static BoardCategory from(int w, int h) {
        if (w <= 8  && h <= 8)  return S8x8;
        if (w <= 16 && h <= 16) return S16x16;
        return S26x18;
    }

    public GameSettings toGameSettings() {
        return switch (this) {
            case S8x8   -> new GameSettings(8,  8,  8);
            case S16x16 -> new GameSettings(16, 16, 30);
            case S26x18 -> new GameSettings(26, 18, 65);
        };
    }

    @Override public String toString() { return label; }
}
