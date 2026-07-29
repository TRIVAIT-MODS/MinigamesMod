package org.trivait.minigamesmod.minigame.dino;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;

import java.util.Random;

public class CactusLarge extends GameObject {
    public CactusLarge(int x, int y) {
        super(x, y, 50/2, 100/2, Identifier.of(MinigamesMod.MOD_ID, ("textures/minigame/dino/cactus_large"+1+".png")));
        int variant = new Random().nextInt(1, 4);
        this.texture = Identifier.of(MinigamesMod.MOD_ID, ("textures/minigame/dino/cactus_large"+variant+".png"));
    }

    @Override
    public void render(DrawContext ctx, float delta) {
        ctx.drawTexture(RenderLayer::getGuiTextured, texture, (int) x, y-height, 0, 0, width, height, width, height);
    }
}
