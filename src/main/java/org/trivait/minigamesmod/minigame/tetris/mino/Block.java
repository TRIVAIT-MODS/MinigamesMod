package org.trivait.minigamesmod.minigame.tetris.mino;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.minigame.tetris.TetrisScreen;

public class Block {
    public int x, y;
    public static int SIZE = 16;
    public Identifier texture;
    public int textureWidth;
    public int textureHeight;
    public float destroying;

    public Block(Identifier t, int textureWidth, int textureHeight) {
        this.texture = t;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.destroying = -1;
    }

    public void draw(DrawContext context) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (destroying == -1) {
            context.drawTexture(texture, TetrisScreen.left_x + x, TetrisScreen.top_y + y, 0, SIZE * (int)(TetrisScreen.animation / 30f), SIZE, SIZE, SIZE, (int)(SIZE * (float)(textureWidth / textureHeight)));
        } else {
            MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
            RenderSystem.setShaderColor(1, 1, 1, 1 - (destroying * 0.1f));
            context.drawTexture(texture, TetrisScreen.left_x + x, TetrisScreen.top_y + y, 0, SIZE * (int)(TetrisScreen.animation / 30f), SIZE, SIZE, SIZE, (int)(SIZE * (float)(textureWidth / textureHeight)));
            RenderSystem.setShaderColor(1, 1, 1, 1);
            context.drawTexture(Identifier.of("textures/block/destroy_stage_" + (int)destroying + ".png"), TetrisScreen.left_x + x, TetrisScreen.top_y + y, 0, SIZE * (int)(TetrisScreen.animation / 30f), SIZE, SIZE, SIZE, (int)(SIZE * (float)(textureWidth / textureHeight)));
        }
        RenderSystem.disableBlend();
    }

    public void draw(DrawContext context, int yOffset) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 0.3f);
        context.drawTexture(texture, TetrisScreen.left_x + x, TetrisScreen.top_y + y + yOffset, 0, SIZE * (int)(TetrisScreen.animation / 30f), SIZE, SIZE, SIZE, (int)(SIZE * (float)(textureWidth / textureHeight)));
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }
}
