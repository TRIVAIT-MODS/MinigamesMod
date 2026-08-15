package org.trivait.minigamesmod.minigame.bubbleshooter.background;

public enum Backgrounds {
    SPACE(new SpaceBackground()),
    FILLED(new FilledBackground()),
    FILLED_GRADIENT(new FilledGradientBackground()),
    GIF(new GifBackground()),
    CUSTOM_GRADIENT(new CustomGradientBackground()),
    ;
    public Background background;
    Backgrounds(Background background) {
        this.background = background;
    }

    @Override
    public String toString() {
        return background.name.getString();
    }
}
