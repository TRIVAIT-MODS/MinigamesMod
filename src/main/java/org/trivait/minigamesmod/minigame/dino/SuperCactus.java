package org.trivait.minigamesmod.minigame.dino;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;

import java.util.Random;

public class SuperCactus extends GameObject {
    public SuperCactus(int x, int y) {
        super(x, y, 500/2, 100/2, Identifier.of(MinigamesMod.MOD_ID, ("textures/minigame/dino/super_cactus.png")));
    }

    @Override
    public void render(DrawContext ctx, float delta) {
        ctx.drawTexture(texture, (int) x, y-height, 0, 0, width, height, width, height);
    }
}
