package org.trivait.minigamesmod.leaderboard;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Leaderboard {
    private String gameName;
    @Nullable
    private List<Component> conditions;
    private boolean rewrite;

    public Leaderboard(String gameName, @Nullable List<Component> conditions, boolean rewrite) {
        this.gameName = gameName;
        this.conditions = conditions;
        this.rewrite = rewrite;
    }

    public void doPost(int value) {
        SheetsApi.submitAsync(gameName, getPlayerName(), value, rewrite);
    }
    public void doPost(long value) {SheetsApi.submitAsync(gameName, getPlayerName(), value, rewrite);}

    public static String getPlayerName() {
        return Minecraft.getInstance().getGameProfile().name();
    }

    public CompletableFuture<List<LeaderboardEntry>> getEntries() {
        return SheetsApi.fetchAsync(gameName);
    }

    @Nullable
    public List<Component> getConditions() {
        return conditions;
    }
}
