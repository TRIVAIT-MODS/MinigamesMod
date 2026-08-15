package org.trivait.minigamesmod.minigame.bubbleshooter.background;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class FilledBackground extends Background{
    public FilledBackground() {
        super(Text.translatable("minigame.bubbleshooter.background.filled"));
    }

    @Override
    public void render(int x, int y, int width, int height, DrawContext ctx, float delta, int mouseX, int mouseY) {
        ctx.fill(x, y, x+width, y+height, 0xFF494949);
    }
}
