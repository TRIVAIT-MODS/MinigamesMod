package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.trivait.minigamesmod.gui.MinigameListScreen;

public class MinigamesButton extends SpriteIconButton.CenteredIcon {

    private static final Identifier DEFAULT_TEXTURE = Identifier.fromNamespaceAndPath("minigamesmod", "icon/button");
    private static final Identifier HOVER_TEXTURE = Identifier.fromNamespaceAndPath("minigamesmod", "icon/button_hover");

    public MinigamesButton(int x, int y) {
        super(20, 20, net.minecraft.network.chat.Component.empty(), 16, 16, new WidgetSprites(DEFAULT_TEXTURE, HOVER_TEXTURE), button -> {
            Minecraft client = Minecraft.getInstance();
            client.setScreen(new MinigameListScreen(client.screen));
        }, null, Button.DEFAULT_NARRATION);
        this.setX(x);
        this.setY(y);
    }

    @Override
    protected void extractSprite(final GuiGraphicsExtractor graphics, final int x, final int y) {
        Identifier currentTexture = this.isHovered ? HOVER_TEXTURE : DEFAULT_TEXTURE;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, currentTexture, x, y, this.spriteWidth, this.spriteHeight, this.alpha);
    }

    /*@Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Identifier currentTexture = this.isSelected() ? HOVER_TEXTURE : DEFAULT_TEXTURE;
        super.renderWidget(context, mouseX, mouseY, delta);
        int i = this.getX() + this.getWidth() / 2 - this.textureWidth / 2;
        int j = this.getY() + this.getHeight() / 2 - this.textureHeight / 2;
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, currentTexture, i, j, this.textureWidth, this.textureHeight);
    }*/
}
