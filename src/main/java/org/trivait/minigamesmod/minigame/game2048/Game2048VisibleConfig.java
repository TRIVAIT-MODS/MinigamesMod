package org.trivait.minigamesmod.minigame.game2048;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "minigames/2048_ui")
public class Game2048VisibleConfig implements ConfigData {

    @ConfigEntry.BoundedDiscrete(min = 3, max = 8)
    public int gridSize = 4;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int volume = 4;
}
