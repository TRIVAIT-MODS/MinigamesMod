package org.trivait.minigamesmod.minigame.minesweeper.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.trivait.minigamesmod.MinigamesMod;

public class DigitDisplayWidget {

    private static final Identifier TEX_DIGITS = Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/minesweeper/digits.png");

    private static final int DIGIT_W  = 14;
    private static final int DIGIT_H  = 23;
    private static final int SHEET_W  = 140;
    private static final int GAP      = 1;

    private final int digits;
    private int x, y;
    private int value;

    public DigitDisplayWidget(int digits) {
        this.digits = digits;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setValue(int value) {
        this.value = Math.max(0, Math.min((int) Math.pow(10, digits) - 1, value));
    }

    public int getWidth() {
        return digits * DIGIT_W + (digits - 1) * GAP;
    }

    public int getHeight() {
        return DIGIT_H;
    }

    public void render(GuiGraphicsExtractor context) {
        int[] out = new int[digits];
        int v = value;
        for (int i = digits - 1; i >= 0; i--) {
            out[i] = v % 10;
            v /= 10;
        }
        for (int i = 0; i < digits; i++) {
            int dx = x + i * (DIGIT_W + GAP);
            int u  = out[i] * DIGIT_W;
            context.blit(RenderPipelines.GUI_TEXTURED, TEX_DIGITS, dx, y, u, 0, DIGIT_W, DIGIT_H, SHEET_W, DIGIT_H);
        }
    }
}
