package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.api.MinigameDefinition;

public class MinigameCardWidget extends ClickableWidget {

    private static final int BORDER = 3;
    private static final int LABEL_HEIGHT = 16;

    private final MinigameDefinition minigame;
    private final Runnable onClick;
    private float scale = 1f;

    public MinigameCardWidget(int x, int y, int width, int height, MinigameDefinition minigame, Runnable onClick) {
        super(x, y, width, height, minigame.getDisplayName());
        this.minigame = minigame;
        this.onClick = onClick;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int cx = getX() + getWidth() / 2;
        int cy = getY() + getHeight() / 2;

        float mx = (mouseX - cx) / scale + cx;
        float my = (mouseY - cy) / scale + cy;
        boolean hovered = mx >= getX() && mx < getX() + getWidth()
                       && my >= getY() && my < getY() + getHeight();

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cx, cy);
        context.getMatrices().scale(scale, scale);
        context.getMatrices().translate(-cx, -cy);

        drawCard(context, hovered);

        context.getMatrices().popMatrix();
    }

    private void drawCard(DrawContext context, boolean hovered) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        int borderColor = hovered ? 0xFFFFFFFF : 0xFFCECECE;

        context.fill(x, y,x + w, y + BORDER, borderColor);
        context.fill(x, y + h - BORDER, x + w, y + h, borderColor);
        context.fill(x, y,x + BORDER, y + h, borderColor);
        context.fill(x + w - BORDER, y, x + w, y + h, borderColor);

        Identifier icon = minigame.getIcon();
        int imgArea = h - LABEL_HEIGHT - BORDER * 2;
        int imgX = x + BORDER;
        int imgY = y + BORDER;
        int imgW = w - BORDER * 2;

        context.fill(imgX, imgY, imgX + imgW, imgY + imgArea, 0xFF666666);

        if (icon != null) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, icon, imgX, imgY, 0, 0, imgW, imgArea, imgW, imgArea);
        } else {
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            context.drawCenteredTextWithShadow(tr,
                    Text.literal("?").styled(s -> s.withBold(true)),
                    x + w / 2, imgY + imgArea / 2, 0xFFFFFFFF);
        }

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int labelY = y + h - LABEL_HEIGHT - BORDER + 2;
        context.fill(x + BORDER, labelY - 2, x + w - BORDER, y + h - BORDER, 0xCC333333);
        context.drawCenteredTextWithShadow(tr, Text.literal(minigame.getDisplayName().getString()).styled(style -> style.withBold(true)), x + w / 2, labelY+1, 0xFFFFFFFF);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        onClick.run();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
