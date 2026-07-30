package org.trivait.minigamesmod.minigame.snake;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.AbstractMinigame;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.leaderboard.Leaderboard;

public class Snake extends AbstractMinigame {

    public Snake() {
        super("snake", Component.translatable("minigame.snake.title"), Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/snake_icon.png"));
    }

    @Override
    public Class<? extends ConfigData> getConfigClass() {
        return SnakeConfig.class;
    }

    @Override
    public Class<? extends ConfigData> getVisibleConfigClass() {
        return SnakeVisibleConfig.class;
    }

    @Override
    public @Nullable Leaderboard getLeaderboard() {
        return new Leaderboard("Snake", null);
    }

    @Override
    public Screen createScreen(Screen parent) {
        return new SnakeScreen(this, parent);
    }

    @Override
    public void onLose() {
        PlayingSoundManager.playSound(SoundEvents.VILLAGER_NO, 1, vol());
    }

    @Override
    public void onWin() {
        PlayingSoundManager.playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 1, vol());
    }

    private float vol() {
        return MinigameRegistry.getConfig(SnakeVisibleConfig.class).volume / 100f;
    }
}
