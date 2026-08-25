package org.trivait.minigamesmod.minigame.pinball;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.ConfigButton;

import java.awt.*;

public class PinballScreen extends Screen {

    private Screen parent;
    private Pinball minigame;

    private int lastKey = 0;
    private boolean debug = false;

    public PinballScreen(Screen parent, Pinball minigame) {
        super(Text.empty());
        this.parent = parent;
        this.minigame = minigame;
    }

    @Override
    protected void init() {
        clearChildren();

        ButtonWidget returnButton = TextIconButtonWidget.builder(Text.empty(), b -> close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15)
                .build();
        returnButton.setTooltip(Tooltip.of(Text.translatable("minigame.2048.undo")));
        returnButton.setDimensionsAndPosition(20, 20, 10, 10);

        ButtonWidget restartButton = TextIconButtonWidget.builder(Text.empty(), b -> init(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15)
                .build();
        restartButton.setTooltip(Tooltip.of(Text.translatable("minigame.restart")));
        restartButton.setDimensionsAndPosition(20, 20, 35, 10);

        addDrawableChild(restartButton);
        addDrawableChild(returnButton);
        addDrawableChild(new ConfigButton(60, 10, minigame));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        if (debug) {
            context.drawCenteredTextWithShadow(tr, "DEBUG", width/2, 5, Color.YELLOW.getRGB());
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (lastKey==0&&keyCode==GLFW.GLFW_KEY_D) {
            lastKey = GLFW.GLFW_KEY_D;
            return false;
        } else if (lastKey == GLFW.GLFW_KEY_D && keyCode == GLFW.GLFW_KEY_E) {
            lastKey = GLFW.GLFW_KEY_E;
            return false;
        } else if (lastKey == GLFW.GLFW_KEY_E && keyCode == GLFW.GLFW_KEY_B) {
            lastKey = GLFW.GLFW_KEY_B;
            return false;
        } else if (lastKey == GLFW.GLFW_KEY_B && keyCode == GLFW.GLFW_KEY_U) {
            lastKey = GLFW.GLFW_KEY_U;
            return false;
        } else if (lastKey == GLFW.GLFW_KEY_U && keyCode == GLFW.GLFW_KEY_G) {
            lastKey = 0;
            debug = !debug;
            PlayingSoundManager.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, vol());
            return false;
        } else {
            lastKey=0;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static float vol() {
        return PlayingSoundManager.vol(MinigameRegistry.getConfig(PinballVisibleConfig.class).volume);
    }

    @Override
    public void close() {
        super.close();
        MinecraftClient.getInstance().setScreen(parent);
    }
}
