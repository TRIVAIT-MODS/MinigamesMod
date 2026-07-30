package org.trivait.minigamesmod.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

public class PlayingSoundManager {
    public static void playSound(SoundEvent soundEvent, float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, pitch, volume));
    }
    public static void playSound(SoundEvent soundEvent) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1, 1));
    }

    public static float vol(int volumeConfig) {
        return 5*((float) volumeConfig/100);
    }
}
