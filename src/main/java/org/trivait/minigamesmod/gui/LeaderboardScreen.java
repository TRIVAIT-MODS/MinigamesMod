package org.trivait.minigamesmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.trivait.minigamesmod.gui.widget.LeaderboardWidget;
import org.trivait.minigamesmod.gui.widget.LeaderboardInfoWidget;
import org.trivait.minigamesmod.leaderboard.Leaderboard;

public class LeaderboardScreen extends Screen {
    private Leaderboard leaderboard;
    private Screen parent;

    public LeaderboardScreen(Leaderboard leaderboard, Screen parent) {
        super(Component.empty());
        this.leaderboard = leaderboard;
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> this.onClose()).bounds(this.width / 2 - 50, this.height - 25, 100, 20).build());

        int widthWidget = Math.max(width - 40 * 2, 420);
        int heightWidget = Math.max(height - 40 * 2, 210);

        this.addRenderableWidget(new LeaderboardWidget(width/2-widthWidget/2, height/2-heightWidget/2, widthWidget, heightWidget, leaderboard));
        this.addRenderableWidget(new LeaderboardInfoWidget(width-8-20, 5, leaderboard));
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().gui.setScreen(parent);
    }
}
