package org.trivait.minigamesmod.minigame.tetris.mino;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.trivait.minigamesmod.minigame.tetris.TetrisScreen;

import java.awt.*;

public class Block {

    public int x, y;
    public static int SIZE = 16;
    public Identifier texture;
    public MutableComponent name;
    public String mino;
    public float destroying;

    public Block(Identifier texture, MutableComponent name, String mino) {
        this.texture = texture;
        this.name = name;
        this.destroying = -1;
        this.mino = mino;
    }

    public void draw(@NotNull GuiGraphicsExtractor context) {

        Color color = new Color(1F, 1F, 1F, destroying == -1 ? 1 : 1 - ((int) destroying * 0.1f));
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(texture);
        context.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, TetrisScreen.leftX + x, TetrisScreen.topY + y, Block.SIZE, Block.SIZE, color.getRGB());
        if ((int) destroying != -1) {
            context.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse("textures/block/destroy_stage_" + (int) destroying + ".png"), TetrisScreen.leftX + x, TetrisScreen.topY + y, 0, Block.SIZE * (int) (TetrisScreen.animation / 30f), Block.SIZE, Block.SIZE, Block.SIZE, Block.SIZE);
        }

        if (TetrisScreen.paused && TetrisScreen.active) {
            double mouseX = Minecraft.getInstance().mouseHandler.xpos() / Minecraft.getInstance().getWindow().getGuiScale();
            double mouseY = Minecraft.getInstance().mouseHandler.ypos() / Minecraft.getInstance().getWindow().getGuiScale();

            if (mouseX >= TetrisScreen.leftX + x && mouseX < TetrisScreen.leftX + x + SIZE && mouseY >= TetrisScreen.topY + y && mouseY < TetrisScreen.topY + y + SIZE) {
                context.setTooltipForNextFrame(Minecraft.getInstance().font, name, (int) mouseX, (int) mouseY);
            }
        }
    }

    public void draw(@NotNull GuiGraphicsExtractor context, int yOffset) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(texture);
        context.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, TetrisScreen.leftX + x, TetrisScreen.topY + y + yOffset, Block.SIZE, Block.SIZE, new Color(1, 1, 1, 0.3f).getRGB());
    }
}