package org.trivait.minigamesmod.minigame.solitaire;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.trivait.minigamesmod.api.AbstractMinigame;

public class Solitaire extends AbstractMinigame {
    public Solitaire() {
        super("solitaire",
                Text.translatable("minigame.solitaire.title"),
                Identifier.ofVanilla("textures/item/diamond_pickaxe.png")
        );
    }

    @Override
    public @Nullable Class<? extends ConfigData> getConfigClass() {
        return SolitaireConfig.class;
    }

    @Override
    public @Nullable Class<? extends ConfigData> getVisibleConfigClass() {
        return SolitaireVisibleConfig.class;
    }

    @Override
    public Screen createScreen(Screen parent) {
        return new SolitaireScreen(parent, this);
    }
}
