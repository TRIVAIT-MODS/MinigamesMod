package org.trivait.minigamesmod.minigame.bubbleshooter.background;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SpaceBackground extends Background {
    private final List<Star> stars = new ArrayList<>();
    private final Random random = new Random();
    private float spawnTimer = 0;

    public SpaceBackground() {
        super(Component.translatable("minigame.bubbleshooter.background.space"));
    }

    @Override
    public void render(int x, int y, int width, int height, GuiGraphicsExtractor ctx, float delta, int mouseX, int mouseY) {
        ctx.fillGradient(x, y, x + width, y + height, 0xFF15158E, 0xFF15153A);

        spawnTimer += delta;
        if (spawnTimer >= 5.0f) {
            spawnTimer = 0;
            if (stars.size() < 100) {
                stars.add(new Star(
                        x + random.nextInt(width),
                        y + random.nextInt(height),
                        8.0f + random.nextFloat() * 10.0f
                ));
            }
        }

        Iterator<Star> iterator = stars.iterator();
        while (iterator.hasNext()) {
            Star star = iterator.next();
            star.update(delta);
            if (star.isDead()) {
                iterator.remove();
            } else {
                star.render(ctx);
            }
        }
    }

    private static class Star {
        private final int x;
        private final int y;
        private final float maxAge;
        private float age = 0;
        private final float fadeTime = 2.0f;

        public Star(int x, int y, float maxAge) {
            this.x = x;
            this.y = y;
            this.maxAge = maxAge;
        }

        public void update(float delta) {
            this.age += delta / 20.0f;
        }

        public boolean isDead() {
            return this.age >= this.maxAge;
        }

        public void render(GuiGraphicsExtractor ctx) {
            float alpha = 1.0f;
            if (age < fadeTime) {
                alpha = age / fadeTime;
            } else if (age > maxAge - fadeTime) {
                alpha = (maxAge - age) / fadeTime;
            }
            alpha = Math.max(0.0f, Math.min(1.0f, alpha));

            int color = ((int) (alpha * 255) << 24) | 0xFFFFFF;
            ctx.fill(x, y, x + 2, y + 2, color);
        }
    }
}
