package org.trivait.minigamesmod.minigame.tetris;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.minigame.tetris.mino.Block;

public class HardDropAnimation extends Animation {
    public HardDropAnimation(int x, int y, int width, int height, int frames) {
        super(x, y, width, height, "hard_drop", frames);
    }

    @Override
    public void draw(DrawContext context) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, (1 - frame * ((float)1 / frames)) / 4);
        context.drawTexture(Identifier.of(MinigamesMod.MOD_ID, "animation/hard_drop/0.png"), x + TetrisScreen.left_x - width / 2, y + TetrisScreen.top_y + Block.SIZE, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }
}
