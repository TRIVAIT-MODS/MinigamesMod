package org.trivait.minigamesmod.api;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.trivait.minigamesmod.leaderboard.Leaderboard;

import java.util.Set;

public interface MinigameDefinition {

    String getId();

    Component getDisplayName();

    @Nullable
    Identifier getIcon();

    Screen createScreen(Screen parent);

    @Nullable
    default Class<? extends ConfigData> getConfigClass() {
        return null;
    }

    @Nullable
    default Class<? extends ConfigData> getVisibleConfigClass() {
        return null;
    }

    @Nullable
    default Leaderboard getLeaderboard() {return null;}

    default void onStart() {}

    default void onStop() {}

    default void onWin() {}

    default void onLose() {}
}
