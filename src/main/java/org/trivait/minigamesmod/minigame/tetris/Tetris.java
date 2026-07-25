package org.trivait.minigamesmod.minigame.tetris;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.AbstractMinigame;

public class Tetris extends AbstractMinigame {

    public Tetris() {
        super("tetris", Text.translatable("minigame.tetris.title"), Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/tetris_icon.png"));
    }

    @Override
    public Class<? extends ConfigData> getConfigClass() {
        return TetrisConfig.class;
    }

    @Override
    public Class<? extends ConfigData> getVisibleConfigClass() {
        return TetrisVisibleConfig.class;
    }

    @Override
    public Screen createScreen(Screen parent) {
        return new TetrisScreen(this, parent);
    }
}
