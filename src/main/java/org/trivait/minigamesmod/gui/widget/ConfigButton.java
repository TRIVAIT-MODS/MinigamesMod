package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.trivait.minigamesmod.api.AbstractMinigame;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.gui.MinigameListScreen;

public class ConfigButton extends SpriteIconButton.CenteredIcon {

    private static final Identifier DEFAULT_TEXTURE = Identifier.fromNamespaceAndPath("minigamesmod", "icon/config");
    private static final Identifier HOVER_TEXTURE = Identifier.fromNamespaceAndPath("minigamesmod", "icon/config_hover");

    public ConfigButton(int x, int y, AbstractMinigame minigame) {
        super(20, 20, net.minecraft.network.chat.Component.empty(), 20, 20, 0, 0, new WidgetSprites(DEFAULT_TEXTURE, HOVER_TEXTURE), button -> {
            Minecraft client = Minecraft.getInstance();
            client.gui.setScreen(MinigameRegistry.openVisibleConfig(minigame, client.gui.screen()));
        }, null, Button.DEFAULT_NARRATION, false);

        this.setTooltip(Tooltip.create(net.minecraft.network.chat.Component.translatable("minigame.config.btn")));
        this.setX(x);
        this.setY(y);
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
