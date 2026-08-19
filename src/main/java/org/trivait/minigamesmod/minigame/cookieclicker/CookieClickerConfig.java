package org.trivait.minigamesmod.minigame.cookieclicker;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "minigames/cookie_clicker")
public class CookieClickerConfig implements ConfigData {
    public double cookies = 0;
    public float cookiesPerClick = 1;
    public int clickUpgradeCount = 0;
    public int clickUpgradePrice = 100;
    public int woodenCursorCount = 0;
    public int woodenCursorPrice = 450;
    public float CPS = 0;
    public int stoneCursorCount = 0;
    public int stoneCursorPrice = 750;
    public int copperCursorCount = 0;
    public int copperCursorPrice = 1500;
    public int goldenCursorCount = 0;
    public int goldenCursorPrice = 6500;
    public int ironCursorCount = 0;
    public int ironCursorPrice = 17000;
    public int diamondCursorCount = 0;
    public int diamondCursorPrice = 130000;
    public int netheriteCursorCount = 0;
    public int netheriteCursorPrice = 1500000;
    public int clickDoublerCount = 0;
    public int clickDoublerPrice = 35000;
}
