package org.trivait.minigamesmod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final SoundEvent BUY = registerSoundEvent("buy");
    public static final SoundEvent CLICK = registerSoundEvent("click");

    public static SoundEvent registerSoundEvent(String name) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, name), SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, name)));
    }

    public static void register() {

    }
}
