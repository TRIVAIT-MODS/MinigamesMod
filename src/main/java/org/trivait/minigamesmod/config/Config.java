package org.trivait.minigamesmod.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.trivait.minigamesmod.config.util.MainMenuButtonPosition;
import org.trivait.minigamesmod.config.util.PauseMenuButtonPosition;

@me.shedaniel.autoconfig.annotation.Config(name = "minigames")
public class Config implements ConfigData {

    public boolean mainMenuButton = true;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public MainMenuButtonPosition mainMenuButtonPosition = MainMenuButtonPosition.ICON_BUTTON;

    public boolean pauseMenuButton = true;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public PauseMenuButtonPosition pauseMenuButtonPosition = PauseMenuButtonPosition.ICON_BUTTON;
}
