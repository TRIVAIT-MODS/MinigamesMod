package org.trivait.minigamesmod.minigame.bubbleshooter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.gui.widget.ConfigButton;

public class BubbleShooterScreen extends Screen {

    private BubbleShooter minigame;
    private Screen parent;

    private static final Identifier GUI_TEXTURE = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/bubble_shooter/gui.png");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 250;
    private static final int GAME_WIDTH = GUI_WIDTH-16;
    private static final int GAME_HEIGHT = GUI_HEIGHT-16;

    public BubbleShooterScreen(BubbleShooter minigame, Screen parent) {
        super(Text.empty());
        this.minigame = minigame;
        this.parent = parent;
    }

    @Override
    protected void init() {
        ButtonWidget returnButton = TextIconButtonWidget.builder(Text.empty(), button -> this.close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15).build();
        returnButton.setTooltip(Tooltip.of(Text.translatable("minigame.2048.undo")));
        returnButton.setDimensionsAndPosition(20, 20, 10, 10);

        ButtonWidget restartButton = TextIconButtonWidget.builder(Text.empty(), button -> restart(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15).build();
        restartButton.setTooltip(Tooltip.of(Text.translatable("minigame.restart")));
        restartButton.setDimensionsAndPosition(20, 20, 35, 10);

        this.addDrawableChild(restartButton);
        this.addDrawableChild(returnButton);
        this.addDrawableChild(new ConfigButton(60, 10, minigame));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int guiX = (width-GUI_WIDTH)/2;
        int guiY = (height-GUI_HEIGHT)/2;
        int gameX = guiX+8;
        int gameY = guiY+8;

        context.drawTexture(GUI_TEXTURE, guiX, guiY, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        MinigameRegistry.getConfig(BubbleShooterVisibleConfig.class).background.background.render(gameX, gameY, GAME_WIDTH, GAME_HEIGHT, context, delta, mouseX, mouseY);
    }

    private void restart() {
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
