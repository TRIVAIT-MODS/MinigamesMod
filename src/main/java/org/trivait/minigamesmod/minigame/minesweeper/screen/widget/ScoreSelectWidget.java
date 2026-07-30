package org.trivait.minigamesmod.minigame.minesweeper.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.minigame.minesweeper.screen.ScoreLeaderboardScreen;

public class ScoreSelectWidget extends ClickableWidget {
    private float scale = 1.0f;
    private float targetScale = 1.0f;
    private final float speed = 0.30f;

    private final Screen parent;

    public ScoreSelectWidget(int x, int y, int width, int height, Screen parent) {
        super(x, y, width, height, Text.empty());
        this.parent = parent;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        Matrix3x2fStack matrixStack = ctx.getMatrices();

        if (this.isHovered()) {
            targetScale = 1.05f;
        } else {
            targetScale = 1.0f;
        }

        scale += (targetScale - scale) * speed;

        int cx = getX() + getWidth() / 2;
        int cy = getY() + getHeight() / 2;

        matrixStack.pushMatrix();
        matrixStack.translate(cx, cy);
        matrixStack.scale(scale, scale);
        matrixStack.translate(-cx, -cy);

        ctx.fill(getX(), getY(), getX()+width, getY()+2, -2);
        ctx.fill(getX(), getY()+height, getX()+width, getY()+height-2, -1);
        ctx.fill(getX(), getY(), getX()+2, getY()+height-2, -2);
        ctx.fill(getX()+width, getY(), getX()+width-2, getY()+height-2, -1);

        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/minesweeper/score.png"), getX() + 1, getY() + 1, 0, 0, width - 2, height - 2, width - 2, height - 2);

        MinecraftClient mc = MinecraftClient.getInstance();
        MutableText label = Text.translatable("minigame.minesweeper.leaderboard.mode.score").styled(s -> s.withBold(true));
        int tw = mc.textRenderer.getWidth(label) * 2;
        int tx = getX() + (width - tw) / 2;
        int ty = getY() + height - mc.textRenderer.fontHeight * 2 - 8;
        matrixStack.pushMatrix();
        matrixStack.translate(tx, ty);
        matrixStack.scale(2f, 2f);
        ctx.drawText(mc.textRenderer, label, 0, 0, -1, true);
        matrixStack.popMatrix();

        matrixStack.popMatrix();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        MinecraftClient.getInstance().setScreen(new ScoreLeaderboardScreen(parent));
        return super.mouseClicked(click, doubled);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
