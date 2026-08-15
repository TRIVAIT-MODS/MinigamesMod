package org.trivait.minigamesmod.minigame.bubbleshooter.background;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class Background {
    public final Component name;

    public Background(Component name) {
        this.name = name;
    }

    public abstract void render(int x, int y, int width, int height, GuiGraphicsExtractor ctx, float delta, int mouseX, int mouseY);
}
