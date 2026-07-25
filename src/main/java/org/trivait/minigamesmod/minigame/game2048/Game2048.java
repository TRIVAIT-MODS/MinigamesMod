package org.trivait.minigamesmod.minigame.game2048;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.AbstractMinigame;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.MinigameTag;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.minigame.tetris.TetrisVisibleConfig;

public class Game2048 extends AbstractMinigame {

    public Game2048() {
        super("2048", Text.literal("2048"), Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/2048_icon.png"));
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
    public Screen createScreen(Screen parent) {
        return new Game2048Screen(this, parent);
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
        return PlayingSoundManager.vol(MinigameRegistry.getConfig(TetrisVisibleConfig.class).volume);
    }
}
