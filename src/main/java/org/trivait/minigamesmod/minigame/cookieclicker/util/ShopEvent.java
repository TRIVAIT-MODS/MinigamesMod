package org.trivait.minigamesmod.minigame.cookieclicker.util;

import net.minecraft.client.MinecraftClient;
import org.trivait.minigamesmod.minigame.cookieclicker.gui.ShopWidget;

@FunctionalInterface
public interface ShopEvent {
    void run(MinecraftClient mc, ShopWidget.ShopEntry shopEntry, int action);
}
