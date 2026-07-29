package org.trivait.minigamesmod.minigame.minesweeper.game;

public class SavedGame {
    public int w, h, mines;
    public boolean minesPlaced;
    public int remainingSafe;
    public boolean alive, won, firstClick;
    public boolean timerRunning;
    public int elapsedSeconds;
    public Cell[] cells;
}
