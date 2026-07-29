package org.trivait.minigamesmod.minigame.minesweeper;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.AbstractMinigame;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.minigame.minesweeper.game.GameMode;
import org.trivait.minigamesmod.minigame.minesweeper.game.GameSettings;
import org.trivait.minigamesmod.minigame.minesweeper.game.SavedGame;

public class MinesweeperGame extends AbstractMinigame {
    private static SavedGame savedGame;

    public MinesweeperGame() {
        super("minesweeper",
                Text.translatable("minigame.minesweeper.title"),
                Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/minesweeper_icon.png"));
    }

    public static SavedGame getSavedGame() { return savedGame; }
    public static void setSavedGame(SavedGame game) { savedGame = game; }

    @Override
    public Screen createScreen(Screen parent) {
        if (savedGame==null) {
            MinesweeperVisibleConfig cfg = MinigameRegistry.getConfig(MinesweeperVisibleConfig.class);
            return new MinesweeperScreen(new GameSettings(cfg.gridWidth, cfg.gridHeight, cfg.mines), GameMode.DEFAULT, this, parent);
        } else {
            return new MinesweeperScreen(savedGame, GameMode.DEFAULT, this, parent);
        }
    }

    @Override
    public @Nullable Class<? extends ConfigData> getVisibleConfigClass() {
        return MinesweeperVisibleConfig.class;
    }

    public Screen createScreen(Screen parent, GameMode gameMode) {
        if (savedGame==null) {
            MinesweeperVisibleConfig cfg = MinigameRegistry.getConfig(MinesweeperVisibleConfig.class);
            return new MinesweeperScreen(new GameSettings(cfg.gridWidth, cfg.gridHeight, cfg.mines), gameMode, this, parent);
        } else {
            return new MinesweeperScreen(savedGame, gameMode, this, parent);
        }
    }
}
