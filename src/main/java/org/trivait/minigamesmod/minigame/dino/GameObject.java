package org.trivait.minigamesmod.minigame.dino;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.joml.Vector4d;

public abstract class GameObject {
    public float x;
    public int y;
    protected int width;
    protected int height;
    protected Identifier texture;
    protected boolean collidable = true;

    public GameObject(int x, int y, int width, int height, Identifier identifier) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.texture = identifier;
    }

    public boolean isCollidable() {
        return collidable;
    }

    public abstract void render(GuiGraphicsExtractor ctx, float delta);
    public void tick() {

    }

    public Vector4d getBox() {
        return new Vector4d(x, y-height, x+width, y);
    }
}
