package org.trivait.minigamesmod.minigame.bubbleshooter.background;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class Background {
    public final Text name;

    public Background(Text name) {
        this.name = name;
    }

    public abstract void render(int x, int y, int width, int height, DrawContext ctx, float delta, int mouseX, int mouseY);
}
