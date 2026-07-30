package org.trivait.minigamesmod.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;

public class PlayingSoundManager {
    public static void playSound(SoundEvent soundEvent, float pitch, float volume) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(soundEvent, pitch, volume));
    }
    public static void playSound(SoundEvent soundEvent) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(soundEvent, 1, 1));
    }

    public static float vol(int volumeConfig) {
        return 5*((float) volumeConfig/100);
    }
}
