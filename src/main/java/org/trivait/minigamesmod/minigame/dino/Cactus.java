package org.trivait.minigamesmod.minigame.dino;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Vector4d;
import org.trivait.minigamesmod.MinigamesMod;

import java.util.Random;

public class Cactus extends GameObject {
    public Cactus(int x, int y) {
        super(x, y, 34/2, 70/2, Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, ("textures/minigame/dino/cactus"+1+".png")));
        int variant = new Random().nextInt(1, 5);
        this.texture = Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, ("textures/minigame/dino/cactus"+variant+".png"));
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, float delta) {
        ctx.blit(RenderPipelines.GUI_TEXTURED, texture, (int) x, y-height, 0, 0, width, height, width, height);
    }

}
