package org.trivait.minigamesmod.minigame.dino;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.trivait.minigamesmod.MinigamesMod;

import java.util.Random;

public class CactusLarge extends GameObject {
    public CactusLarge(int x, int y) {
        super(x, y, 50/2, 100/2, Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, ("textures/minigame/dino/cactus_large"+1+".png")));
        int variant = new Random().nextInt(1, 4);
        this.texture = Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, ("textures/minigame/dino/cactus_large"+variant+".png"));
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, float delta) {
        ctx.blit(RenderPipelines.GUI_TEXTURED, texture, (int) x, y-height, 0, 0, width, height, width, height);
    }
}
