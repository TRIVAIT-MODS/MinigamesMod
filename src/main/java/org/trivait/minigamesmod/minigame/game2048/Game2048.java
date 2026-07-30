package org.trivait.minigamesmod.minigame.game2048;

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
import org.trivait.minigamesmod.minigame.tetris.TetrisVisibleConfig;

import java.util.List;

public class Game2048 extends AbstractMinigame {

    public Game2048() {
        super("2048", Component.literal("2048"), Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/2048_icon.png"));
    }

    @Override
    public Class<? extends ConfigData> getConfigClass() {
        return Game2048Config.class;
    }

    @Override
    public Class<? extends ConfigData> getVisibleConfigClass() {
        return Game2048VisibleConfig.class;
    }

    @Override
    public @Nullable Leaderboard getLeaderboard() {
        return new Leaderboard("Game2048", List.of(
                Component.translatable("minigame.2048.condition")
        ));
    }

    @Override
    public Screen createScreen(Screen parent) {
        return new Game2048Screen(this, parent);
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
        return PlayingSoundManager.vol(MinigameRegistry.getConfig(TetrisVisibleConfig.class).volume);
    }
}
