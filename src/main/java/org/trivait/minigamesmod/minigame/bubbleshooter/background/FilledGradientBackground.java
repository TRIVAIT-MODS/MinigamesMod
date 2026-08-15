package org.trivait.minigamesmod.minigame.bubbleshooter.background;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class FilledGradientBackground extends Background{
    public FilledGradientBackground() {
        super(Text.translatable("minigame.bubbleshooter.background.filled_gradient"));
    }

    @Override
    public void render(int x, int y, int width, int height, DrawContext ctx, float delta, int mouseX, int mouseY) {
        ctx.fillGradient(x, y, x+width, y+height, 0xFF3F3F49, 0xFF353535);
    }
}
