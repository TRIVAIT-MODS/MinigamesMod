package org.trivait.minigamesmod.minigame.minesweeper.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.gui.MinigameListScreen;
import org.trivait.minigamesmod.minigame.minesweeper.MinesweeperGame;
import org.trivait.minigamesmod.minigame.minesweeper.MinesweeperScreen;
import org.trivait.minigamesmod.minigame.minesweeper.MinesweeperVisibleConfig;
import org.trivait.minigamesmod.minigame.minesweeper.game.GameBoard;
import org.trivait.minigamesmod.minigame.minesweeper.game.GameMode;
import org.trivait.minigamesmod.minigame.minesweeper.game.GameSettings;
import org.trivait.minigamesmod.minigame.minesweeper.game.SavedGame;
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
        this.playerName = MinecraftClient.getInstance().getGameProfile().getName();
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
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        drawTopCounter(ctx);
    }

    private void drawTopCounter(DrawContext ctx) {
        String text;
        if (lbMode == GameMode.LEADERBOARD_TIME) {
            int secs = elapsedMs / 1000;
            int centis = (elapsedMs % 1000) / 10;
            text = secs + "." + String.format("%02d", centis);
        } else {
            text = String.valueOf(winCount);
        }

        Text label = Text.literal(text);
        int tw = textRenderer.getWidth(label);

        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(width / 2f, 4f);
        ctx.getMatrices().scale(2f, 2f);
        ctx.getMatrices().translate(-tw / 2f, 0f);
        ctx.drawText(textRenderer, label, 0, 0, 0xFFFFFFFF, true);
        ctx.getMatrices().popMatrix();
    }

    @Override
    public void close() {
        super.close();
        ((MinesweeperGame) MinigameRegistry.get("minesweeper")).createScreen(new MinigameListScreen(null), GameMode.DEFAULT);
    }
}
