package org.trivait.minigamesmod.minigame.bubbleshooter;

import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.AbstractMinigame;
import org.trivait.minigamesmod.leaderboard.Leaderboard;

public class BubbleShooter extends AbstractMinigame {
    public BubbleShooter() {
        super("bubbleshooter", Text.translatable("minigame.bubbleshooter.title"), Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/bubble_shooter.png"));
    }

    @Override
    public Screen createScreen(Screen parent) {
        return new BubbleShooterScreen(this, parent);
    }

    @Override
    public @Nullable Class<? extends ConfigData> getConfigClass() {
        return BubbleShooterConfig.class;
    }

    @Override
    public @Nullable Class<? extends ConfigData> getVisibleConfigClass() {
        return BubbleShooterVisibleConfig.class;
    }

    @Override
    public @Nullable Leaderboard getLeaderboard() {
        return new Leaderboard("Bubbleshooter", null, true);
    }
}
