package org.trivait.minigamesmod.minigame.bubbleshooter;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.trivait.minigamesmod.minigame.bubbleshooter.background.Backgrounds;

@Config(name = "minigames/bubble_shooter_ui")
public class BubbleShooterVisibleConfig implements ConfigData {
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public Backgrounds background = Backgrounds.SPACE;
    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int startColor = 0xFFCC0000;
    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int endColor = 0xFFFF5000;
}