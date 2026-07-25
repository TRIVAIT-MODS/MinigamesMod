package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.api.AbstractMinigame;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.gui.MinigameListScreen;

public class ConfigButton extends TextIconButtonWidget.IconOnly {

    private static final Identifier TEXTURE = Identifier.of("minigamesmod", "icon/config");

    public ConfigButton(int x, int y, AbstractMinigame minigame) {
        super(20, 20, Text.empty(), 16, 16, TEXTURE, button -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(MinigameRegistry.openVisibleConfig(minigame, client.currentScreen));
        }, null);
        this.setTooltip(Tooltip.of(Text.translatable("minigame.config.btn")));
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        int i = this.getX() + this.getWidth() / 2 - this.textureWidth / 2;
        int j = this.getY() + this.getHeight() / 2 - this.textureHeight / 2;
        context.drawGuiTexture(TEXTURE, i, j, this.textureWidth, this.textureHeight);
    }
}
