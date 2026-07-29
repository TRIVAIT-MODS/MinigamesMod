package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.api.AbstractMinigame;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.gui.MinigameListScreen;

public class ConfigButton extends TextIconButtonWidget.IconOnly {

    private static final Identifier DEFAULT_TEXTURE = Identifier.of("minigamesmod", "icon/config");
    private static final Identifier HOVER_TEXTURE = Identifier.of("minigamesmod", "icon/config_hover");

    public ConfigButton(int x, int y, AbstractMinigame minigame) {
        super(20, 20, Text.empty(), 20, 20, DEFAULT_TEXTURE, button -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(MinigameRegistry.openVisibleConfig(minigame, client.currentScreen));
        }, null);
        this.setTooltip(Tooltip.of(Text.translatable("minigame.config.btn")));
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Identifier currentTexture = this.isSelected() ? HOVER_TEXTURE : DEFAULT_TEXTURE;
        super.renderWidget(context, mouseX, mouseY, delta);
        int i = this.getX() + this.getWidth() / 2 - this.textureWidth / 2;
        int j = this.getY() + this.getHeight() / 2 - this.textureHeight / 2;
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, currentTexture, i, j, this.textureWidth, this.textureHeight);
    }
}
