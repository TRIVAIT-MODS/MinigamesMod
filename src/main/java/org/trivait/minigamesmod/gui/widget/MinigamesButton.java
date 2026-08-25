package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.gui.MinigameListScreen;

public class MinigamesButton extends TextIconButtonWidget.IconOnly {

    private static final Identifier DEFAULT_TEXTURE = Identifier.of("minigamesmod", "icon/button");
    private static final Identifier DEFAULT_STATIC_TEXTURE = Identifier.of("minigamesmod", "icon/button_static");
    private static final Identifier HOVER_TEXTURE = Identifier.of("minigamesmod", "icon/button_hover");
    private static final Identifier HOVER_STATIC_TEXTURE = Identifier.of("minigamesmod", "icon/button_hover_static");
    private static final Identifier EMPTY = Identifier.of("minigamesmod", "icon/empty");

    public MinigamesButton(int x, int y) {
        super(20, 20, Text.empty(), 16, 16, EMPTY, button -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(new MinigameListScreen(client.currentScreen));
        }, null);
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Identifier currentTexture = MinigamesMod.CONFIG.staticIconButton ? this.isSelected() ? HOVER_STATIC_TEXTURE : DEFAULT_STATIC_TEXTURE : this.isSelected() ? HOVER_TEXTURE : DEFAULT_TEXTURE;
        super.renderWidget(context, mouseX, mouseY, delta);
        int i = this.getX() + this.getWidth() / 2 - this.textureWidth / 2;
        int j = this.getY() + this.getHeight() / 2 - this.textureHeight / 2;
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, currentTexture, i, j, this.textureWidth, this.textureHeight);
    }
}
