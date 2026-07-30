package org.trivait.minigamesmod.leaderboard;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Leaderboard {
    private String gameName;
    @Nullable
    private List<Component> conditions;

    public Leaderboard(String gameName, @Nullable List<Component> conditions) {
        this.gameName = gameName;
        this.conditions = conditions;
    }

    public void doPost(String name, int value, boolean rewrite) {
        SheetsApi.submitAsync(gameName, name, value, rewrite);
    }

    public CompletableFuture<List<LeaderboardEntry>> getEntries() {
        return SheetsApi.fetchAsync(gameName);
    }

    @Nullable
    public List<Component> getConditions() {
        return conditions;
    }
}
