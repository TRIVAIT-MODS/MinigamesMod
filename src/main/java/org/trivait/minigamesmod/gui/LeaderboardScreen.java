package org.trivait.minigamesmod.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.trivait.minigamesmod.gui.widget.LeaderboardWidget;
import org.trivait.minigamesmod.gui.widget.LeaderboardInfoWidget;
import org.trivait.minigamesmod.leaderboard.Leaderboard;

public class LeaderboardScreen extends Screen {
    private Leaderboard leaderboard;
    private Screen parent;

    public LeaderboardScreen(Leaderboard leaderboard, Screen parent) {
        super(Text.empty());
        this.leaderboard = leaderboard;
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), b -> this.close()).dimensions(this.width / 2 - 50, this.height - 28, 100, 20).build());

        int widthWidget = Math.max(width - 40 * 2, 420);
        int heightWidget = Math.max(height - 40 * 2, 210);

        this.addDrawableChild(new LeaderboardWidget(width/2-widthWidget/2, height/2-heightWidget/2, widthWidget, heightWidget, leaderboard));
        this.addDrawableChild(new LeaderboardInfoWidget(width-8-20, 5, leaderboard));
    }

    @Override
    public void close() {
        super.close();
        MinecraftClient.getInstance().setScreen(parent);
    }
}
