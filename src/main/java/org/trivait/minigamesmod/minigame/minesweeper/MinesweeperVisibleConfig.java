package org.trivait.minigamesmod.minigame.minesweeper;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@me.shedaniel.autoconfig.annotation.Config(name = "minigames/minesweeper")
public class MinesweeperVisibleConfig implements ConfigData {

    @ConfigEntry.BoundedDiscrete(min = 5, max = 32)
    public int gridWidth = 16;

    @ConfigEntry.BoundedDiscrete(min = 5, max = 32)
    public int gridHeight = 16;

    public int mines = 40;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int soundsVolume = 100;

    public boolean enableAnimations = true;
    public boolean enableExplosionAnimation = false;
}
