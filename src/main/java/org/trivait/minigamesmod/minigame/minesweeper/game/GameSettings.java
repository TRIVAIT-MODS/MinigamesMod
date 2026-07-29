package org.trivait.minigamesmod.minigame.minesweeper.game;

public record GameSettings(int width, int height, int mines) {
    public GameSettings {
        width = Math.max(5, Math.min(width, 40));
        height = Math.max(5, Math.min(height, 40));
        int maxMines = Math.max(1, width * height - 1);
        mines = Math.max(1, Math.min(mines, maxMines));
    }
}
