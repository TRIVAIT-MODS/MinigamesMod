package org.trivait.minigamesmod.minigame.sudoku;

public enum Difficulty {
    EASY(30),
    MEDIUM(40),
    HARD(50);

    private final int cellsToRemove;

    Difficulty(int cellsToRemove) {
        this.cellsToRemove = cellsToRemove;
    }

    public int getCellsToRemove() {
        return cellsToRemove;
    }
}