package org.trivait.minigamesmod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.config.Config;
import org.trivait.minigamesmod.minigame.game2048.Game2048;
import org.trivait.minigamesmod.minigame.tetris.Tetris;

public class MinigamesMod implements ClientModInitializer {
    public static final String MOD_ID = "minigamesmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Config CONFIG;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(Config.class).getConfig();

        MinigameRegistry.register(new Game2048());
        MinigameRegistry.register(new Tetris());
    }
}
