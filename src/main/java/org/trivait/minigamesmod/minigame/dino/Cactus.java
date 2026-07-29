package org.trivait.minigamesmod.minigame.dino;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Vector4d;
import org.trivait.minigamesmod.MinigamesMod;

import java.util.Random;

public class Cactus extends GameObject {
    public Cactus(int x, int y) {
        super(x, y, 34/2, 70/2, Identifier.of(MinigamesMod.MOD_ID, ("textures/minigame/dino/cactus"+1+".png")));
        int variant = new Random().nextInt(1, 5);
        this.texture = Identifier.of(MinigamesMod.MOD_ID, ("textures/minigame/dino/cactus"+variant+".png"));
    }

    @Override
    public void render(DrawContext ctx, float delta) {
        ctx.drawTexture(texture, (int) x, y-height, 0, 0, width, height, width, height);
    }

}
