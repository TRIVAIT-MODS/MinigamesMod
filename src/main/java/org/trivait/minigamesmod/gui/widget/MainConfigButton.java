package org.trivait.minigamesmod.gui.widget;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.api.AbstractMinigame;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.config.Config;

public class MainConfigButton extends TextIconButtonWidget.IconOnly {

    private static final Identifier DEFAULT_TEXTURE = Identifier.of("minigamesmod", "icon/config");
    private static final Identifier HOVER_TEXTURE = Identifier.of("minigamesmod", "icon/config_hover");

    public MainConfigButton(int x, int y) {
        super(20, 20, net.minecraft.text.Text.empty(), 20, 20, new ButtonTextures(DEFAULT_TEXTURE, HOVER_TEXTURE), button -> {
            MinecraftClient client = MinecraftClient.getInstance();
            Screen screen = client.currentScreen;
            screen.close();
            client.setScreen(AutoConfigClient.getConfigScreen(Config.class, screen).get());
        }, null, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.setTooltip(Tooltip.of(net.minecraft.text.Text.translatable("minigame.config.btn")));
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
