package org.trivait.minigamesmod.minigame.dino;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.trivait.minigamesmod.MinigamesMod;

import java.util.Random;

public class SuperCactus extends GameObject {
    public SuperCactus(int x, int y) {
        super(x, y, 500/2, 100/2, Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, ("textures/minigame/dino/super_cactus.png")));
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, float delta) {
        ctx.blit(RenderPipelines.GUI_TEXTURED, texture, (int) x, y-height, 0, 0, width, height, width, height);
    }
}
