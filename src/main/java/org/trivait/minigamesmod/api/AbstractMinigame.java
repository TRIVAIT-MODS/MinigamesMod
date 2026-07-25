package org.trivait.minigamesmod.api;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class AbstractMinigame implements MinigameDefinition {

    private final String id;
    private final Text displayName;
    private final Identifier icon;

    protected AbstractMinigame(String id, Text displayName, Identifier icon) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
    }

    @Override public String getId() { return id; }
    @Override public Text getDisplayName() { return displayName; }
    @Override public Identifier getIcon() { return icon; }

    @Override public @Nullable Class<? extends ConfigData> getConfigClass() { return null; }
    @Override public @Nullable Class<? extends ConfigData> getVisibleConfigClass() { return null; }

    @Override public abstract Screen createScreen(Screen parent);
}
