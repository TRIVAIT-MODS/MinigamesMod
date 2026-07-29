package org.trivait.minigamesmod.minigame.dino;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "minigames/dino_ui")
public class GoogleDinoVisibleConfig implements ConfigData {
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int volume = 100;
}
