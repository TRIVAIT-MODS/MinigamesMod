package org.trivait.minigamesmod.minigame.bubbleshooter.background;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.minigame.bubbleshooter.BubbleShooterVisibleConfig;

public class CustomGradientBackground extends Background{
    public CustomGradientBackground() {
        super(Text.translatable("minigame.bubbleshooter.background.custom_gradient"));
    }

    @Override
    public void render(int x, int y, int width, int height, DrawContext ctx, float delta, int mouseX, int mouseY) {
        ctx.fillGradient(x, y, x+width, y+height, MinigameRegistry.getConfig(BubbleShooterVisibleConfig.class).startColor,MinigameRegistry.getConfig(BubbleShooterVisibleConfig.class).endColor);
    }
}
