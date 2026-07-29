package org.trivait.minigamesmod.minigame.minesweeper.game;

public class Cell {
    public boolean mine;
    public boolean revealed;
    public boolean flagged;
    public int adjacent;
    public float revealProgress = -1f;
    public int delayTicks = -1;
    public boolean scheduled = false;
}
