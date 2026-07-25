package org.trivait.minigamesmod.api;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface MinigameDefinition {

    String getId();

    Text getDisplayName();

    @Nullable
    Identifier getIcon();

    Screen createScreen(Screen parent);

    default Set<MinigameTag> getTags() {
        return Set.of();
    }

    @Nullable
    default Class<? extends ConfigData> getConfigClass() {
        return null;
    }

    @Nullable
    default Class<? extends ConfigData> getVisibleConfigClass() {
        return null;
    }

    default void onStart() {}

    default void onStop() {}

    default void onWin() {}

    default void onLose() {}
}
