package org.trivait.minigamesmod.minigame.tetris;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "minigames/tetris_ui")
public class TetrisVisibleConfig implements ConfigData {

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public HardDropMode hardDrop = HardDropMode.INSTANT;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int volume = 100;
}
