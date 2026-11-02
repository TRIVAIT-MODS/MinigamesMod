package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.BooleanSupplier;

public class RearrangeButton extends TextIconButtonWidget.IconOnly {

    private static final Identifier DEFAULT_TEXTURE = Identifier.of("minigamesmod", "icon/rearrange");
    private static final Identifier HOVER_TEXTURE = Identifier.of("minigamesmod", "icon/rearrange_hover");

    public RearrangeButton(int x, int y, PressAction onPress) {
        super(20, 20, Text.empty(), 20, 20, new ButtonTextures(DEFAULT_TEXTURE, HOVER_TEXTURE), onPress, null, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.setTooltip(Tooltip.of(Text.translatable("minigame.layout.btn")));
        this.setX(x);
        this.setY(y);
    }

    /*@Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Identifier currentTexture = (this.isSelected() || activeSupplier.getAsBoolean()) ? HOVER_TEXTURE : DEFAULT_TEXTURE;
        super.renderWidget(context, mouseX, mouseY, delta);
        int i = this.getX() + this.getWidth() / 2 - this.textureWidth / 2;
        int j = this.getY() + this.getHeight() / 2 - this.textureHeight / 2;
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, currentTexture, i, j, this.textureWidth, this.textureHeight);
    }*/
}
