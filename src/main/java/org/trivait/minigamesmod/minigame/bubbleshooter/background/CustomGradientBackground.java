package org.trivait.minigamesmod.minigame.bubbleshooter.background;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.minigame.bubbleshooter.BubbleShooterVisibleConfig;

public class CustomGradientBackground extends Background{
    public CustomGradientBackground() {
        super(Component.translatable("minigame.bubbleshooter.background.custom_gradient"));
    }

    @Override
    public void render(int x, int y, int width, int height, GuiGraphicsExtractor ctx, float delta, int mouseX, int mouseY) {
        ctx.fillGradient(x, y, x+width, y+height, MinigameRegistry.getConfig(BubbleShooterVisibleConfig.class).startColor,MinigameRegistry.getConfig(BubbleShooterVisibleConfig.class).endColor);
    }
}
