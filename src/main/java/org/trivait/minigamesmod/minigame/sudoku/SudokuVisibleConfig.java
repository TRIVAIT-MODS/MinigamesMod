package org.trivait.minigamesmod.minigame.sudoku;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.trivait.minigamesmod.minigame.tetris.HardDropMode;

@Config(name = "minigames/sudoku_ui")
public class SudokuVisibleConfig implements ConfigData {
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public Difficulty difficulty = Difficulty.MEDIUM;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int volume = 100;
}
