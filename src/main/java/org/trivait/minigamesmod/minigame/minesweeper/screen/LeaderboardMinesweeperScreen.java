package org.trivait.minigamesmod.minigame.minesweeper.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.gui.MinigameListScreen;
import org.trivait.minigamesmod.minigame.minesweeper.MinesweeperGame;
import org.trivait.minigamesmod.minigame.minesweeper.MinesweeperScreen;
import org.trivait.minigamesmod.minigame.minesweeper.game.GameBoard;
import org.trivait.minigamesmod.minigame.minesweeper.game.GameMode;
import org.trivait.minigamesmod.minigame.minesweeper.game.GameSettings;
import org.trivait.minigamesmod.minigame.minesweeper.leaderboard.BoardCategory;
import org.trivait.minigamesmod.minigame.minesweeper.leaderboard.SheetsApi;

public class LeaderboardMinesweeperScreen extends MinesweeperScreen {

    private final GameMode lbMode;
    private final BoardCategory category;
    private final String playerName;
    private long timerStartMs = 0;
    private boolean lbTimerRunning = false;
    private int elapsedMs = 0;
    private boolean resultSubmitted = false;

    private int winCount = 0;

    public LeaderboardMinesweeperScreen(GameSettings settings, GameMode lbMode, MinesweeperGame minigame, Screen parent, BoardCategory category) {
        super(settings, lbMode, minigame, parent);
        this.lbMode = lbMode;
        this.category = category;
        this.playerName = Minecraft.getInstance().getUser().getName();
        MinesweeperGame.setSavedGame(null);
    }

    @Override
    protected void resetGame() {
        if (lbMode == GameMode.LEADERBOARD_TIME) {
            lbTimerRunning = false;
            elapsedMs = 0;
            resultSubmitted = false;
        }
        super.resetGame();
    }

    @Override
    protected GameBoard.SoundCallback makeSoundCallback() {
        GameBoard.SoundCallback base = super.makeSoundCallback();
        return new GameBoard.SoundCallback() {
            @Override public void onReveal() { base.onReveal(); }

            @Override
            public void onExplode(int cellX, int cellY) {
                base.onExplode(cellX, cellY);
                if (lbMode == GameMode.LEADERBOARD_TIME && lbTimerRunning) {
                    elapsedMs = (int) (System.currentTimeMillis() - timerStartMs);
                    lbTimerRunning = false;
                }
            }

            @Override
            public void onWin() {
                base.onWin();
                handleWin();
            }
        };
    }

    private void handleWin() {
        if (lbMode == GameMode.LEADERBOARD_TIME) {
            if (lbTimerRunning) {
                elapsedMs = (int) (System.currentTimeMillis() - timerStartMs);
                lbTimerRunning = false;
            }
            if (!resultSubmitted) {
                resultSubmitted = true;
                SheetsApi.submitTimeAsync(playerName, elapsedMs / 1000.0, category);
            }
        } else if (lbMode == GameMode.LEADERBOARD_WIN_COUNT) {
            winCount++;
            SheetsApi.submitScoreAsync(playerName, 1, category);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (board == null) return;

        if (lbMode == GameMode.LEADERBOARD_TIME) {
            if (board.timerRunning && !lbTimerRunning && !resultSubmitted) {
                timerStartMs = System.currentTimeMillis();
                lbTimerRunning = true;
            }
            if (lbTimerRunning) {
                elapsedMs = (int) (System.currentTimeMillis() - timerStartMs);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(ctx, mouseX, mouseY, delta);
        drawTopCounter(ctx);
    }

    private void drawTopCounter(GuiGraphicsExtractor ctx) {
        String text;
        if (lbMode == GameMode.LEADERBOARD_TIME) {
            int secs = elapsedMs / 1000;
            int centis = (elapsedMs % 1000) / 10;
            text = secs + "." + String.format("%02d", centis);
        } else {
            text = String.valueOf(winCount);
        }

        Component label = Component.literal(text);
        int tw = font.width(label);

        ctx.pose().pushMatrix();
        ctx.pose().translate(width / 2f, 4f);
        ctx.pose().scale(2f, 2f);
        ctx.pose().translate(-tw / 2f, 0f);
        ctx.text(font, label, 0, 0, 0xFFFFFFFF, true);
        ctx.pose().popMatrix();
    }

    @Override
    public void onClose() {
        super.onClose();
        ((MinesweeperGame) MinigameRegistry.get("minesweeper")).createScreen(new MinigameListScreen(null), GameMode.DEFAULT);
    }
}
