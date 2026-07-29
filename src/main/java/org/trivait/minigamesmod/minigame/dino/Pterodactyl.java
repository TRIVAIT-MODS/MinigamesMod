package org.trivait.minigamesmod.minigame.dino;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Vector4d;
import org.trivait.minigamesmod.MinigamesMod;

public class Pterodactyl extends GameObject {
    private boolean frame = false;
    private int i = 0;

    public Pterodactyl(int x, int y) {
        super(x, y, 92/2, 80/2, Identifier.of(MinigamesMod.MOD_ID, ("textures/minigame/dino/pterodactyl.png")));
    }

    @Override
    public void render(DrawContext ctx, float delta) {
        ctx.drawTexture(texture, (int) x, y - height, frame ? 0 : width, 0, width, height, (int) (184/2), height);
    }

    @Override
    public Vector4d getBox() {
        float offsetX = 6;
        float offsetY = 10;
        return new Vector4d(x + offsetX, y - height + offsetY, x + width - offsetX, y - offsetY);
    }

    @Override
    public void tick() {
        i++;
        if (i % 4 == 0) {
            frame = !frame;
        }
    }
}
