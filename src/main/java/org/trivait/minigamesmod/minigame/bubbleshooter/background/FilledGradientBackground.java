package org.trivait.minigamesmod.minigame.bubbleshooter.background;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class FilledGradientBackground extends Background{
    public FilledGradientBackground() {
        super(Component.translatable("minigame.bubbleshooter.background.filled_gradient"));
    }

    @Override
    public void render(int x, int y, int width, int height, GuiGraphicsExtractor ctx, float delta, int mouseX, int mouseY) {
        ctx.fillGradient(x, y, x+width, y+height, 0xFF3F3F49, 0xFF353535);
    }
}
