package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class RearrangeButton extends SpriteIconButton.CenteredIcon {

    private static final Identifier DEFAULT_TEXTURE = Identifier.fromNamespaceAndPath("minigamesmod", "icon/rearrange");
    private static final Identifier HOVER_TEXTURE = Identifier.fromNamespaceAndPath("minigamesmod", "icon/rearrange_hover");

    public RearrangeButton(int x, int y, OnPress onPress) {
        super(20, 20, Component.empty(), 20, 20, new WidgetSprites(DEFAULT_TEXTURE, HOVER_TEXTURE), onPress, null, Button.DEFAULT_NARRATION);
        this.setTooltip(Tooltip.create(Component.translatable("minigame.layout.btn")));
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
