package org.trivait.minigamesmod.minigame.pinball;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.trivait.minigamesmod.api.AbstractMinigame;

public class Pinball extends AbstractMinigame {
    public Pinball() {
        super("pinball",
                Text.translatable("minigame.pinball.title"),
                Identifier.ofVanilla("textures/item/diamond_pickaxe.png")
        );
    }

    @Override
    public @Nullable Class<? extends ConfigData> getConfigClass() {
        return PinballConfig.class;
    }

    @Override
    public @Nullable Class<? extends ConfigData> getVisibleConfigClass() {
        return PinballVisibleConfig.class;
    }

    @Override
    public Screen createScreen(Screen parent) {
        return new PinballScreen(parent, this);
    }
}
