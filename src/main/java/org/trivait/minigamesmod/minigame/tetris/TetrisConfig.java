package org.trivait.minigamesmod.minigame.tetris;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "minigames/tetris")
public class TetrisConfig implements ConfigData {
    public int tetrisHighScore = 0;
}
