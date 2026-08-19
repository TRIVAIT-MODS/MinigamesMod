package org.trivait.minigamesmod;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class ModSounds {

    public static final SoundEvent BUY = registerSoundEvent("buy");
    public static final SoundEvent CLICK = registerSoundEvent("click");

    public static SoundEvent registerSoundEvent(String name) {
        return Registry.register(Registries.SOUND_EVENT, Identifier.of(MinigamesMod.MOD_ID, name), SoundEvent.of(Identifier.of(MinigamesMod.MOD_ID, name)));
    }

    public static void register() {

    }
}
