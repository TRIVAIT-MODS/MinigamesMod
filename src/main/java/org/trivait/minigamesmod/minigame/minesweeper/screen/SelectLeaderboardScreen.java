package org.trivait.minigamesmod.minigame.minesweeper.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.ScoreSelectWidget;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.TimeSelectWidget;

public class SelectLeaderboardScreen extends Screen {
    private final Screen parent;

    public SelectLeaderboardScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        int maxSize = Math.min(this.width / 2 - 24, this.height - 66);
        int w = Math.min(maxSize, 420);
        int totalW = w * 2 + 14;
        int startX = (this.width - totalW) / 2;
        int y = (this.height - w) / 2;

        ScoreSelectWidget scoreSelectWidget = new ScoreSelectWidget(startX, y, w, w, parent);
        TimeSelectWidget timeSelectWidget = new TimeSelectWidget(startX + w + 14, y, w, w, parent);

        this.addRenderableWidget(scoreSelectWidget);
        this.addRenderableWidget(timeSelectWidget);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.minecraft.setScreen(parent);
    }
}
