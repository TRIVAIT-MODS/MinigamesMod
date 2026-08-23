package org.trivait.minigamesmod.leaderboard;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Leaderboard {
    private String gameName;
    @Nullable
    private List<Text> conditions;
    private boolean rewrite;

    public Leaderboard(String gameName, @Nullable List<Text> conditions, boolean rewrite) {
        this.gameName = gameName;
        this.conditions = conditions;
        this.rewrite = rewrite;
    }

    public void doPost(int value) {
        SheetsApi.submitAsync(gameName, getPlayerName(), value, rewrite);
    }
    public void doPost(long value) {
        SheetsApi.submitAsync(gameName, getPlayerName(), value, rewrite);
    }

    public static String getPlayerName() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }

    public CompletableFuture<List<LeaderboardEntry>> getEntries() {
        return SheetsApi.fetchAsync(gameName);
    }

    @Nullable
    public List<Text> getConditions() {
        return conditions;
    }
}
