package org.trivait.minigamesmod.minigame.chess;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.AbstractMinigame;

public class Chess extends AbstractMinigame {
    public Chess() {
        super("chess",
                Text.translatable("minigame.chess.title"),
                Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess_icon.png"));
    }

    @Override
    public @Nullable Class<? extends ConfigData> getConfigClass() {
        return ChessConfig.class;
    }

    @Override
    public @Nullable Class<? extends ConfigData> getVisibleConfigClass() {
        return ChessVisibleConfig.class;
    }

    @Override
    public Screen createScreen(Screen parent) {
        return new ChessScreen(parent, this);
    }
}
