package org.trivait.minigamesmod.minigame.cookieclicker.util;

import net.minecraft.client.Minecraft;
import org.trivait.minigamesmod.minigame.cookieclicker.gui.ShopWidget;

@FunctionalInterface
public interface ShopEvent {
    void run(Minecraft mc, ShopWidget.ShopEntry shopEntry, int action);
}
