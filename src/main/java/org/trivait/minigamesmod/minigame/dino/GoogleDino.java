package org.trivait.minigamesmod.minigame.dino;

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
import org.trivait.minigamesmod.minigame.game2048.Game2048Config;
import org.trivait.minigamesmod.minigame.game2048.Game2048Screen;
import org.trivait.minigamesmod.minigame.game2048.Game2048VisibleConfig;
import org.trivait.minigamesmod.minigame.tetris.TetrisVisibleConfig;

import java.util.List;

public class GoogleDino extends AbstractMinigame {

    public GoogleDino() {
        super("google_dino", Component.translatable("minigame.dino.title"), Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/dino_icon.png"));
    }

    @Override
    public Class<? extends ConfigData> getConfigClass() {
        return GoogleDinoConfig.class;
    }

    @Override
    public @Nullable Class<? extends ConfigData> getVisibleConfigClass() {
        return GoogleDinoVisibleConfig.class;
    }

    @Override
    public @Nullable Leaderboard getLeaderboard() {
        return new Leaderboard("Dino", null, true);
    }

    @Override
    public Screen createScreen(Screen parent) {
        return new GoogleDinoScreen(this, parent);
    }

    public static float vol() {
        return PlayingSoundManager.vol(MinigameRegistry.getConfig(GoogleDinoVisibleConfig.class).volume);
    }
}
