package org.trivait.minigamesmod.api;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.trivait.minigamesmod.MinigamesMod;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MinigameRegistry {

    private static final Map<String, MinigameDefinition> REGISTRY = new LinkedHashMap<>();

    private MinigameRegistry() {}

    public static void register(MinigameDefinition minigame) {
        String id = minigame.getId();
        if (REGISTRY.containsKey(id)) {
            MinigamesMod.LOGGER.warn("Minigame '{}' already registered, skipping.", id);
            return;
        }
        tryRegisterConfig(id, minigame.getConfigClass());
        tryRegisterConfig(id, minigame.getVisibleConfigClass());
        REGISTRY.put(id, minigame);
        MinigamesMod.LOGGER.info("Registered minigame: {}", id);
    }

    private static void tryRegisterConfig(String id, @Nullable Class<? extends ConfigData> cls) {
        if (cls == null) return;
        try {
            AutoConfig.register(cls, GsonConfigSerializer::new);
        } catch (Exception e) {
            MinigamesMod.LOGGER.warn("Config for '{}' already registered: {}", id, e.getMessage());
        }
    }

    public static <T extends ConfigData> T getConfig(Class<T> configClass) {
        return AutoConfig.getConfigHolder(configClass).getConfig();
    }

    @Nullable
    public static Screen openVisibleConfig(MinigameDefinition minigame, Screen parent) {
        Class<? extends ConfigData> cls = minigame.getVisibleConfigClass();
        if (cls == null) return null;
        return AutoConfig.getConfigScreen(cls, parent).get();
    }

    public static MinigameDefinition get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<MinigameDefinition> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }
}
