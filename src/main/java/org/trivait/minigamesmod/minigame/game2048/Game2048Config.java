package org.trivait.minigamesmod.minigame.game2048;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "minigames/2048")
public class Game2048Config implements ConfigData {
    public long bestScore = 0;
}
