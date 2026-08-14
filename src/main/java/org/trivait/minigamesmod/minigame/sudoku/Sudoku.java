package org.trivait.minigamesmod.minigame.sudoku;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.AbstractMinigame;
import org.trivait.minigamesmod.leaderboard.Leaderboard;

import java.util.List;

public class Sudoku extends AbstractMinigame {
    public Sudoku() {
        super("sudoku",
                Text.translatable("minigame.sudoku.title"),
                Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/sudoku_icon.png"));
    }

    @Override
    public @Nullable Class<? extends ConfigData> getVisibleConfigClass() {
        return SudokuVisibleConfig.class;
    }

    @Override
    public @Nullable Leaderboard getLeaderboard() {
        return new Leaderboard("Sudoku", List.of(
                Text.translatable("minigame.sudoku.condition.first")
        ), false);
    }

    @Override
    public Screen createScreen(Screen parent) {
        return new SudokuScreen(parent, this);
    }
}
