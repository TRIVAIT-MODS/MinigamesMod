package org.trivait.minigamesmod.minigame.dino;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;

import java.util.Random;

public class Cloud extends GameObject {
    public Cloud(int x, int y) {
        super(x, y, 92/2, 28/2, Identifier.of(MinigamesMod.MOD_ID, ("textures/minigame/dino/cloud.png")));
        this.collidable = false;
        int variant = new Random().nextInt(1, 5);
        this.texture = Identifier.of(MinigamesMod.MOD_ID, ("textures/minigame/dino/cloud.png"));
    }

    @Override
    public void render(DrawContext ctx, float delta) {
        ctx.drawTexture(texture, (int) x, y-height, 0, 0, width, height, width, height);
    }
}
