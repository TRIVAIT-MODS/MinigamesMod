package org.trivait.minigamesmod.minigame.pinball;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "minigames/pinball")
public class PinballConfig implements ConfigData {
    public long highScore = 0;
}
