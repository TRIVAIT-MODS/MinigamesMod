package org.trivait.minigamesmod.minigame.minesweeper.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.gui.MinigameListScreen;
import org.trivait.minigamesmod.minigame.minesweeper.MinesweeperGame;
import org.trivait.minigamesmod.minigame.minesweeper.game.GameMode;
import org.trivait.minigamesmod.minigame.minesweeper.leaderboard.BoardCategory;
import org.trivait.minigamesmod.minigame.minesweeper.leaderboard.LeaderboardCache;
import org.trivait.minigamesmod.minigame.minesweeper.leaderboard.SheetsApi;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.LeaderboardWidget;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.ScoreboardVersionWidget;

public class ScoreLeaderboardScreen extends Screen {
    private final Screen parent;

    private LeaderboardWidget leaderboard;
    private BoardCategory boardCategory = BoardCategory.S8x8;
    private LeaderboardCache CACHE = new LeaderboardCache();

    private ButtonWidget c8x8Button;
    private ButtonWidget c16x16Button;
    private ButtonWidget c26x18Button;

    private ButtonWidget playButton;

    private ScoreboardVersionWidget versionWidget;

    public ScoreLeaderboardScreen(Screen parent) {
        super(Text.empty());
        this.parent = parent;
        SheetsApi.fetchScriptVersionAsync();
    }

    @Override
    protected void init() {
        this.leaderboard = new LeaderboardWidget(10, 10, width/2+125, height-20, CACHE, GameMode.LEADERBOARD_WIN_COUNT, boardCategory);
        this.versionWidget = new ScoreboardVersionWidget(width-5-26, 5);

        int panelX = width / 2 + 125;
        int panelW = width - panelX;
        int btnW = 100;
        int btnX = panelX + (panelW - btnW) / 2;
        int btnSpacing = 28;
        int groupH = 3 * 20 + 2 * (btnSpacing - 20) + 10 + 24;
        int groupY = (height - groupH) / 2;

        this.c8x8Button = ButtonWidget.builder(Text.literal("8x8"), b -> {
            boardCategory = BoardCategory.S8x8;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c16x16Button.active = true;
            c26x18Button.active = true;
        }).tooltip(Tooltip.of(Text.translatable("minigame.minesweeper.leaderboard.mines").append(Text.literal("" + BoardCategory.S8x8.mines)))).dimensions(btnX, groupY, btnW, 20).build();
        this.addDrawableChild(c8x8Button);

        c8x8Button.active=false;

        this.c16x16Button = ButtonWidget.builder(Text.literal("16x16"), b -> {
            boardCategory = BoardCategory.S16x16;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c8x8Button.active = true;
            c26x18Button.active = true;
        }).tooltip(Tooltip.of(Text.translatable("minigame.minesweeper.leaderboard.mines").append(Text.literal("" + BoardCategory.S16x16.mines)))).dimensions(btnX, groupY + btnSpacing, btnW, 20).build();
        this.addDrawableChild(c16x16Button);

        this.c26x18Button = ButtonWidget.builder(Text.literal("26x18"), b -> {
            boardCategory = BoardCategory.S26x18;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c16x16Button.active = true;
            c8x8Button.active = true;
        }).tooltip(Tooltip.of(Text.translatable("minigame.minesweeper.leaderboard.mines").append(Text.literal("" + BoardCategory.S26x18.mines)))).dimensions(btnX, groupY + btnSpacing * 2, btnW, 20).build();
        this.addDrawableChild(c26x18Button);

        this.playButton = ButtonWidget.builder(Text.translatable("minigame.minesweeper.leaderboard.play").setStyle(Style.EMPTY.withBold(true)), button -> {
            client.setScreen(new LeaderboardMinesweeperScreen(
                    boardCategory.toGameSettings(),
                    GameMode.LEADERBOARD_WIN_COUNT,
                    ((MinesweeperGame) MinigameRegistry.get("minesweeper")),
                    this,
                    boardCategory
            ));
        }).dimensions(btnX-2, groupY + btnSpacing * 2 + 30, btnW+4, 24).build();
        this.addDrawableChild(playButton);

        this.addDrawableChild(leaderboard);
        this.addDrawableChild(versionWidget);
        refresh();
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == 294) {
            refresh();
        }

        return super.keyPressed(input);
    }

    private void refresh() {
        CACHE.invalidate(GameMode.LEADERBOARD_WIN_COUNT);
        CACHE.refreshIfNeeded(GameMode.LEADERBOARD_WIN_COUNT, () -> {});
    }

    @Override
    public void close() {
        super.close();
        this.client.setScreen(parent);
    }
}
