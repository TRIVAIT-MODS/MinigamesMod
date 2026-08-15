package org.trivait.minigamesmod.minigame.bubbleshooter.background;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class FilledBackground extends Background{
    public FilledBackground() {
        super(Component.translatable("minigame.bubbleshooter.background.filled"));
    }

    @Override
    public void render(int x, int y, int width, int height, GuiGraphicsExtractor ctx, float delta, int mouseX, int mouseY) {
        ctx.fill(x, y, x+width, y+height, 0xFF494949);
    }
}
