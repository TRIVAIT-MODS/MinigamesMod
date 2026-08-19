package org.trivait.minigamesmod.minigame.cookieclicker;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.AbstractMinigame;
import org.trivait.minigamesmod.leaderboard.Leaderboard;

public class CookieClicker extends AbstractMinigame {
    public CookieClicker() {
        super("cookie_clicker",
                Component.translatable("minigame.cookieclicker.title"),
                Identifier.withDefaultNamespace("textures/item/cookie.png"));
    }

    @Override
    public Screen createScreen(Screen parent) {
        return new CookieClickerScreen(parent, this);
    }

    @Override
    public @Nullable Leaderboard getLeaderboard() {
        return new Leaderboard("CookieClicker", null, false);
    }

    @Override
    public @Nullable Class<? extends ConfigData> getVisibleConfigClass() {
        return CookieClickerVisualConfig.class;
    }

    @Override
    public @Nullable Class<? extends ConfigData> getConfigClass() {
        return CookieClickerConfig.class;
    }
}
