package org.trivait.minigamesmod.minigame.minesweeper.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.ScoreSelectWidget;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.TimeSelectWidget;

public class SelectLeaderboardScreen extends Screen {
    private final Screen parent;

    public SelectLeaderboardScreen(Screen parent) {
        super(Text.empty());
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

        this.addDrawableChild(scoreSelectWidget);
        this.addDrawableChild(timeSelectWidget);
    }

    @Override
    public void close() {
        super.close();
        this.client.setScreen(parent);
    }
}
