package org.trivait.minigamesmod.minigame.solitaire;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "minigames/solitaire")
public class SolitaireConfig implements ConfigData {
    public long highScore = 0;
}
