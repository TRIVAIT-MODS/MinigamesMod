package org.trivait.minigamesmod.minigame.tetris.mino;

import net.minecraft.util.Identifier;

public class TextureResource {
    public final Identifier texture;
    public final int height;
    public final int width;

    public TextureResource(Identifier texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }
}
