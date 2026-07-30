package org.trivait.minigamesmod.minigame.minesweeper.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.trivait.minigamesmod.MinigamesMod;

public class ExplosionAnimation {

    private static final int FRAMES = 21;
    private static final Identifier[] TEXTURES = new Identifier[FRAMES];
    static {
        for (int i = 0; i < FRAMES; i++)
            TEXTURES[i] = Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "animation/explosion/" + i + ".png");
    }

    private static final float ASPECT = 16f / 9f;
    private static final float TICKS_PER_FRAME = 20f * 0.7f / FRAMES;

    private final int x, y, w, h;
    private float elapsed = 0f;
    public boolean done = false;

    public ExplosionAnimation(int cellCenterX, int cellCenterY, int height) {
        this.h = height;
        this.w = Math.round(height * ASPECT);
        this.x = cellCenterX - this.w / 2;
        this.y = cellCenterY - this.h / 2;
    }

    public void tick() {
        if (done) return;
        elapsed += 1f;
        if (elapsed >= FRAMES * TICKS_PER_FRAME) done = true;
    }

    public void render(GuiGraphicsExtractor context) {
        if (done) return;
        int f = Math.min((int) (elapsed / TICKS_PER_FRAME), FRAMES - 1);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURES[f], x, y, 0, 0, w, h, w, h);
    }
}
