package org.trivait.minigamesmod.minigame.solitaire;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "minigames/solitaire_ui")
public class SolitaireVisibleConfig implements ConfigData {
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int volume = 100;
}
