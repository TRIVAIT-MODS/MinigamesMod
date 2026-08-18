package org.trivait.minigamesmod.minigame.cookieclicker;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "minigames/cookie_clicker_ui")
public class CookieClickerVisualConfig implements ConfigData {
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int volume = 100;
    public boolean cookieClickerSounds = true;
    @ConfigEntry.BoundedDiscrete(min = 0, max = 500)
    public int maxCookieRate = 300;
}
