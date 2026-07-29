package org.trivait.minigamesmod.minigame.snake;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.AbstractMinigame;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.leaderboard.Leaderboard;

public class Snake extends AbstractMinigame {

    public Snake() {
        super("snake", Text.translatable("minigame.snake.title"), Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/snake_icon.png"));
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
        PlayingSoundManager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1, vol());
    }

    @Override
    public void onWin() {
        PlayingSoundManager.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 1, vol());
    }

    private float vol() {
        return MinigameRegistry.getConfig(SnakeVisibleConfig.class).volume / 100f;
    }
}
