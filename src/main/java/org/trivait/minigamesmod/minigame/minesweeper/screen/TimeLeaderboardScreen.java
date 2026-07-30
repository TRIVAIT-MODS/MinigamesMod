package org.trivait.minigamesmod.minigame.minesweeper.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.minigame.minesweeper.MinesweeperGame;
import org.trivait.minigamesmod.minigame.minesweeper.game.GameMode;
import org.trivait.minigamesmod.minigame.minesweeper.leaderboard.BoardCategory;
import org.trivait.minigamesmod.minigame.minesweeper.leaderboard.LeaderboardCache;
import org.trivait.minigamesmod.minigame.minesweeper.leaderboard.SheetsApi;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.LeaderboardWidget;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.ScoreboardVersionWidget;

public class TimeLeaderboardScreen extends Screen {
    private final Screen parent;

    private LeaderboardWidget leaderboard;
    private BoardCategory boardCategory = BoardCategory.S8x8;
    private LeaderboardCache CACHE = new LeaderboardCache();

    private Button c8x8Button;
    private Button c16x16Button;
    private Button c26x18Button;

    private Button playButton;

    private ScoreboardVersionWidget versionWidget;

    public TimeLeaderboardScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
        SheetsApi.fetchScriptVersionAsync();
    }

    @Override
    protected void init() {
        this.leaderboard = new LeaderboardWidget(10, 10, width/2+125, height-20, CACHE, GameMode.LEADERBOARD_TIME, boardCategory);
        this.versionWidget = new ScoreboardVersionWidget(width-5-26, 5);

        int panelX = width / 2 + 125;
        int panelW = width - panelX;
        int btnW = 100;
        int btnX = panelX + (panelW - btnW) / 2;
        int btnSpacing = 28;
        int groupH = 3 * 20 + 2 * (btnSpacing - 20) + 10 + 24;
        int groupY = (height - groupH) / 2;

        this.c8x8Button = Button.builder(Component.literal("8x8"), b -> {
            boardCategory = BoardCategory.S8x8;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c16x16Button.active = true;
            c26x18Button.active = true;
        }).tooltip(Tooltip.create(Component.translatable("minigame.minesweeper.leaderboard.mines").append(Component.literal("" + BoardCategory.S8x8.mines)))).bounds(btnX, groupY, btnW, 20).build();
        this.addRenderableWidget(c8x8Button);

        c8x8Button.active=false;

        this.c16x16Button = Button.builder(Component.literal("16x16"), b -> {
            boardCategory = BoardCategory.S16x16;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c8x8Button.active = true;
            c26x18Button.active = true;
        }).tooltip(Tooltip.create(Component.translatable("minigame.minesweeper.leaderboard.mines").append(Component.literal("" + BoardCategory.S16x16.mines)))).bounds(btnX, groupY + btnSpacing, btnW, 20).build();
        this.addRenderableWidget(c16x16Button);

        this.c26x18Button = Button.builder(Component.literal("26x18"), b -> {
            boardCategory = BoardCategory.S26x18;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c16x16Button.active = true;
            c8x8Button.active = true;
        }).tooltip(Tooltip.create(Component.translatable("minigame.minesweeper.leaderboard.mines").append(Component.literal("" + BoardCategory.S26x18.mines)))).bounds(btnX, groupY + btnSpacing * 2, btnW, 20).build();
        this.addRenderableWidget(c26x18Button);

        this.playButton = Button.builder(Component.translatable("minigame.minesweeper.leaderboard.play").setStyle(Style.EMPTY.withBold(true)), button -> {
            minecraft.gui.setScreen(new LeaderboardMinesweeperScreen(
                    boardCategory.toGameSettings(),
                    GameMode.LEADERBOARD_TIME,
                    ((MinesweeperGame) MinigameRegistry.get("minesweeper")),
                    this,
                    boardCategory
            ));
        }).bounds(btnX-2, groupY + btnSpacing * 2 + 30, btnW+4, 24).build();
        this.addRenderableWidget(playButton);

        this.addRenderableWidget(leaderboard);
        this.addRenderableWidget(versionWidget);
        refresh();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == 294) {
            refresh();
        }

        return super.keyPressed(input);
    }

    private void refresh() {
        CACHE.invalidate(GameMode.LEADERBOARD_TIME);
        CACHE.refreshIfNeeded(GameMode.LEADERBOARD_TIME, () -> {});
    }

    @Override
    public void onClose() {
        super.onClose();
        this.minecraft.gui.setScreen(parent);
    }
}
