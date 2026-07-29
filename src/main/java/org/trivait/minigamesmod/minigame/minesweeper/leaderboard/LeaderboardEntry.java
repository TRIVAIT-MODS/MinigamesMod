package org.trivait.minigamesmod.minigame.minesweeper.leaderboard;

public record LeaderboardEntry(String name, String value, String category, double numericValue) {
    public LeaderboardEntry(String name, String value, String category) {
        this(name, value, category, 0);
    }
}
