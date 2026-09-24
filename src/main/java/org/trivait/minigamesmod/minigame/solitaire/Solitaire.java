package org.trivait.minigamesmod.minigame.solitaire;

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

import java.util.List;

public class Solitaire extends AbstractMinigame {
    public Solitaire() {
        super("solitaire",
                Text.translatable("minigame.solitaire.title"),
                Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/solitaire_icon.png")
        );
    }

    @Override
    public @Nullable Class<? extends ConfigData> getConfigClass() {
        return SolitaireConfig.class;
    }

    @Override
    public @Nullable Class<? extends ConfigData> getVisibleConfigClass() {
        return SolitaireVisibleConfig.class;
    }

    @Override
    public @Nullable Leaderboard getLeaderboard() {
        return new Leaderboard("Solitaire", null, false);
    }

    @Override
    public Screen createScreen(Screen parent) {
        return new SolitaireScreen(parent, this);
    }

    @Override
    public void onWin() {
        PlayingSoundManager.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, vol());
    }

    private float vol() {
        return PlayingSoundManager.vol(MinigameRegistry.getConfig(SolitaireVisibleConfig.class).volume);
    }
}
