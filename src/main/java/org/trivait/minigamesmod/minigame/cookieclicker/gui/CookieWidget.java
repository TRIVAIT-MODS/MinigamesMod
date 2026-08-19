package org.trivait.minigamesmod.minigame.cookieclicker.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.trivait.minigamesmod.ModSounds;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.minigame.cookieclicker.CookieClickerScreen;
import org.trivait.minigamesmod.minigame.cookieclicker.CookieClickerVisualConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CookieWidget extends AbstractWidget {
    private static final Identifier COOKIE_TEXTURE = Identifier.withDefaultNamespace("textures/item/cookie.png");
    private List<ClickEffect> clickEffects = new ArrayList<>();

    private float animationTime = 0.0f;
    private boolean isAnimating = false;

    private CookieClickerScreen screen;

    public CookieWidget(int x, int y, int width, int height, CookieClickerScreen screen) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        this.animationTime = 0.0f;
        this.isAnimating = true;
        clickEffects.add(new ClickEffect(screen.cookiesPerClick, (int) mouseX, (int) mouseY));
        screen.cookies+=screen.cookiesPerClick;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        float scale = 1.0f;

        if (isAnimating) {
            animationTime += delta * 0.4f;
            if (animationTime >= 2.0f) {
                isAnimating = false;
                animationTime = 0.0f;
            } else {
                float x = animationTime * (float) Math.PI;
                scale = 1.0f - (float) (Math.sin(x) / (1.0f + animationTime * 2.0f)) * 0.15f;
            }
        }

        int centerX = this.getX() + this.width / 2;
        int centerY = this.getY() + this.height / 2;

        context.pose().pushMatrix();
        context.pose().translate(centerX, centerY);
        context.pose().scale(scale, scale);
        context.pose().translate(-centerX, -centerY);

        context.blit(RenderPipelines.GUI_TEXTURED, COOKIE_TEXTURE, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);

        context.pose().popMatrix();

        List<ClickEffect> toRemove = new ArrayList<>();
        for (ClickEffect clickEffect : clickEffects) {
            if (clickEffect.alpha<=0) {
                toRemove.add(clickEffect);
            } else {
                clickEffect.render(context);
            }
        } clickEffects.removeAll(toRemove);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        SoundEvent sound = MinigameRegistry.getConfig(CookieClickerVisualConfig.class).cookieClickerSounds ? ModSounds.CLICK : SoundEvents.UI_BUTTON_CLICK.value();
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (this.active && this.visible) {
            if (this.isValidClickButton(click.buttonInfo())) {
                boolean bl = this.isMouseOver(mouseX, mouseY);
                if (bl) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1, CookieClickerScreen.vol()));
                    this.onClick(click, doubled);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }

    public class ClickEffect {
        private static final Identifier COOKIE_TEXTURE = Identifier.withDefaultNamespace("textures/item/cookie.png");

        private float count;
        private int x;
        private int y;

        public float alpha = 1;

        public ClickEffect(float count, int x, int y){
            this.count = count;
            this.x = x;
            this.y = y;
        }

        public void render(GuiGraphicsExtractor ctx) {
            Font tr = Minecraft.getInstance().font;
            y--;

            String countStr = (count == (long) count) ? String.format("%.0f", count) : String.valueOf(count);
            String message = "+"+countStr;
            int width = tr.width(message)+2;
            int allWidth = width+16;
            int textAlpha = ((int) (Math.max(0, alpha-=0.02f) * 255)) << 24;
            int textColor = 0xFFFFFF | textAlpha;

            ctx.text(tr, message, x-(allWidth/2), y-4, textColor, true);

            ctx.blit(RenderPipelines.GUI_TEXTURED, COOKIE_TEXTURE, x-(allWidth/2)+width, y-8, 0, 0, 16, 16, 16, 16, new Color(1, 1, 1, Math.max(0, alpha-=0.02f)).getRGB());

            alpha-=0.02f;
        }
    }
}