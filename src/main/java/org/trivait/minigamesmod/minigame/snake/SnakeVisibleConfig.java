package org.trivait.minigamesmod.minigame.snake;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "minigames/snake_ui")
public class SnakeVisibleConfig implements ConfigData {
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int volume = 100;
}
