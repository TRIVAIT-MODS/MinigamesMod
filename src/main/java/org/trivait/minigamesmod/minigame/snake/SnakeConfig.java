package org.trivait.minigamesmod.minigame.snake;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "minigames/snake")
public class SnakeConfig implements ConfigData {
    public int snakeHighScore = 0;
}
