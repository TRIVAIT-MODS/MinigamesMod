package org.trivait.minigamesmod.minigame.bubbleshooter;

public class Effect {
    public final float x;
    public final float y;
    public final int color;
    public final BubbleShooterScreen.EffectType type;

    public float delay;
    public float progress;

    public Effect(float x, float y, int color, BubbleShooterScreen.EffectType type, float delay) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.type = type;
        this.delay = delay;
    }

    public void update(float dt) {
        if (delay > 0) {
            delay = Math.max(0, delay - dt);
            return;
        }

        float duration = type == BubbleShooterScreen.EffectType.FALL ? BubbleShooterScreen.FALL_TIME : BubbleShooterScreen.REMOVAL_TIME;

        progress = Math.min(
                1f,
                progress + dt / duration
        );
    }

    public boolean dead() {
        return delay <= 0 && progress >= 1f;
    }
}