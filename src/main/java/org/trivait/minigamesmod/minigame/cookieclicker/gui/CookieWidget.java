package org.trivait.minigamesmod.minigame.cookieclicker.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.ModSounds;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.minigame.cookieclicker.CookieClickerScreen;
import org.trivait.minigamesmod.minigame.cookieclicker.CookieClickerVisualConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CookieWidget extends ClickableWidget {
    private static final Identifier COOKIE_TEXTURE = Identifier.ofVanilla("textures/item/cookie.png");
    private List<ClickEffect> clickEffects = new ArrayList<>();

    private float animationTime = 0.0f;
    private boolean isAnimating = false;

    private CookieClickerScreen screen;

    public CookieWidget(int x, int y, int width, int height, CookieClickerScreen screen) {
        super(x, y, width, height, Text.empty());
        this.screen = screen;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.animationTime = 0.0f;
        this.isAnimating = true;
        clickEffects.add(new ClickEffect(screen.cookiesPerClick, (int) mouseX, (int) mouseY));
        screen.cookies+=screen.cookiesPerClick;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
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

        context.getMatrices().push();
        context.getMatrices().translate(centerX, centerY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-centerX, -centerY, 0);

        context.drawTexture(RenderLayer::getGuiTextured, COOKIE_TEXTURE, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);

        context.getMatrices().pop();

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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        SoundEvent sound = MinigameRegistry.getConfig(CookieClickerVisualConfig.class).cookieClickerSounds ? ModSounds.CLICK : SoundEvents.UI_BUTTON_CLICK.value();
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                boolean bl = this.isMouseOver(mouseX, mouseY);
                if (bl) {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, 1, CookieClickerScreen.vol()));
                    this.onClick(mouseX, mouseY);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    public class ClickEffect {
        private static final Identifier COOKIE_TEXTURE = Identifier.ofVanilla("textures/item/cookie.png");

        private float count;
        private int x;
        private int y;

        public float alpha = 1;

        public ClickEffect(float count, int x, int y){
            this.count = count;
            this.x = x;
            this.y = y;
        }

        public void render(DrawContext ctx) {
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            y--;

            String countStr = (count == (long) count) ? String.format("%.0f", count) : String.valueOf(count);
            String message = "+"+countStr;
            int width = tr.getWidth(message)+2;
            int allWidth = width+16;
            int textAlpha = ((int) (Math.max(0, alpha-=0.02f) * 255)) << 24;
            int textColor = 0xFFFFFF | textAlpha;

            ctx.drawText(tr, message, x-(allWidth/2), y-4, textColor, true);

            ctx.drawTexture(RenderLayer::getGuiTextured, COOKIE_TEXTURE, x-(allWidth/2)+width, y-8, 0, 0, 16, 16, 16, 16, new Color(1, 1, 1, Math.max(0, alpha-=0.02f)).getRGB());

            alpha-=0.02f;
        }
    }
}