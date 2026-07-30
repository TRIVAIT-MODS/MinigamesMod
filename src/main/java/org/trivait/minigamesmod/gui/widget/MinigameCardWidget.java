package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.trivait.minigamesmod.api.MinigameDefinition;

public class MinigameCardWidget extends AbstractWidget {

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
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int cx = getX() + getWidth() / 2;
        int cy = getY() + getHeight() / 2;

        float mx = (mouseX - cx) / scale + cx;
        float my = (mouseY - cy) / scale + cy;
        boolean hovered = mx >= getX() && mx < getX() + getWidth()
                       && my >= getY() && my < getY() + getHeight();

        context.pose().pushMatrix();
        context.pose().translate(cx, cy);
        context.pose().scale(scale, scale);
        context.pose().translate(-cx, -cy);

        drawCard(context, hovered);

        context.pose().popMatrix();
    }

    private void drawCard(GuiGraphicsExtractor context, boolean hovered) {
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
            context.blit(RenderPipelines.GUI_TEXTURED, icon, imgX, imgY, 0, 0, imgW, imgArea, imgW, imgArea);
        } else {
            Font tr = Minecraft.getInstance().font;
            context.centeredText(tr,
                    Component.literal("?").withStyle(s -> s.withBold(true)),
                    x + w / 2, imgY + imgArea / 2, 0xFFFFFFFF);
        }

        Font tr = Minecraft.getInstance().font;
        int labelY = y + h - LABEL_HEIGHT - BORDER + 2;
        context.fill(x + BORDER, labelY - 2, x + w - BORDER, y + h - BORDER, 0xCC333333);
        context.centeredText(tr, Component.literal(minigame.getDisplayName().getString()).withStyle(style -> style.withBold(true)), x + w / 2, labelY+1, 0xFFFFFFFF);
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        onClick.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        defaultButtonNarrationText(builder);
    }
}
