package org.trivait.minigamesmod.minigame.minesweeper;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.gui.widget.ConfigButton;
import org.trivait.minigamesmod.minigame.minesweeper.game.*;
import org.trivait.minigamesmod.minigame.minesweeper.screen.SelectLeaderboardScreen;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.DigitDisplayWidget;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.ExplosionAnimation;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.SmileyButtonWidget;

import java.util.ArrayList;
import java.util.List;

public class MinesweeperScreen extends Screen {

    private static final Identifier TEX_FLAG     = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/minesweeper/flag.png");
    private static final Identifier TEX_BARRIER  = Identifier.ofVanilla("textures/item/barrier.png");
    private static final Identifier TEX_TNT_SIDE = Identifier.ofVanilla("textures/block/tnt_side.png");

    private static final Text[] ADJ_TEXT = {
        Text.empty(),
        Text.literal("1").styled(s -> s.withBold(true)),
        Text.literal("2").styled(s -> s.withBold(true)),
        Text.literal("3").styled(s -> s.withBold(true)),
        Text.literal("4").styled(s -> s.withBold(true)),
        Text.literal("5").styled(s -> s.withBold(true)),
        Text.literal("6").styled(s -> s.withBold(true)),
        Text.literal("7").styled(s -> s.withBold(true)),
        Text.literal("8").styled(s -> s.withBold(true)),
    };

    private static final int TOP_BAR_H = 28;

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final GameSettings newGameSettings;
    private final SavedGame initialSave;

    protected GameBoard board;

    private int cellSize;
    private int gridX, gridY;
    private int topBarX, topBarY, topBarW;

    private SmileyButtonWidget smileyBtn;
    private DigitDisplayWidget minesDisplay;
    private DigitDisplayWidget timerDisplay;

    private final List<ExplosionAnimation> explosions = new ArrayList<>();

    public final GameMode gameMode;

    private ButtonWidget leaderboardButton;

    private MinesweeperGame minigame;
    private Screen parent;

    public MinesweeperScreen(GameSettings settings, GameMode gameMode, MinesweeperGame minigame, Screen parent) {
        super(Text.empty());
        this.newGameSettings = settings;
        this.initialSave = null;
        this.gameMode = gameMode;
        this.minigame = minigame;
        this.parent = parent;
    }

    public MinesweeperScreen(SavedGame savedGame, GameMode gameMode, MinesweeperGame minigame, Screen parent) {
        super(Text.empty());
        this.newGameSettings = null;
        this.initialSave = savedGame;
        this.gameMode = gameMode;
        this.minigame = minigame;
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (initialSave != null && board == null) {
            board = new GameBoard(initialSave);
        } else if (board == null) {
            board = new GameBoard(newGameSettings != null ? newGameSettings : defaultSettings());
        }

        board.setSoundCallback(makeSoundCallback());

        int marginTop = 40 + TOP_BAR_H + 6;
        int availW = Math.max(1, this.width - 40);
        int availH = Math.max(1, this.height - (marginTop + 50));
        cellSize = Math.min(Math.min(Math.max(8, availW / board.w), Math.max(8, availH / board.h)), 48);

        int totalW = board.w * cellSize;
        int totalH = board.h * cellSize;
        gridX = (this.width - totalW) / 2;
        gridY = Math.max(marginTop, (this.height - totalH) / 2);
        topBarW = totalW;
        topBarX = gridX;
        topBarY = Math.max(10, gridY - TOP_BAR_H - 6);

        this.clearChildren();

        ButtonWidget returnButton = TextIconButtonWidget.builder(Text.empty(), button -> this.close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15).build();
        returnButton.setTooltip(Tooltip.of(Text.translatable("minigame.2048.undo")));
        returnButton.setDimensionsAndPosition(20, 20, 10, 10);

        ButtonWidget restartButton = TextIconButtonWidget.builder(Text.empty(), button -> resetGame(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15).build();
        restartButton.setTooltip(Tooltip.of(Text.translatable("minigame.restart")));
        restartButton.setDimensionsAndPosition(20, 20, 35, 10);

        this.addDrawableChild(restartButton);
        this.addDrawableChild(returnButton);

        int smileSize = Math.max(18, Math.min(26, TOP_BAR_H - 2));
        smileyBtn = new SmileyButtonWidget(
            this.width / 2 - smileSize / 2,
            topBarY + (TOP_BAR_H - smileSize) / 2,
            smileSize,
            this::resetGame
        );
        this.addDrawableChild(smileyBtn);

        minesDisplay = new DigitDisplayWidget(3);
        timerDisplay = new DigitDisplayWidget(3);
        int dispY = topBarY + (TOP_BAR_H - minesDisplay.getHeight()) / 2;
        minesDisplay.setPosition(topBarX + 6, dispY);
        timerDisplay.setPosition(topBarX + topBarW - 6 - timerDisplay.getWidth(), dispY);
        if (gameMode == GameMode.DEFAULT) {
            this.addDrawableChild(new ConfigButton(60, 10, minigame));
        }
    }

    private GameSettings defaultSettings() {
        MinesweeperVisibleConfig cfg = MinigameRegistry.getConfig(MinesweeperVisibleConfig.class);
        return new GameSettings(cfg.gridWidth, cfg.gridHeight, cfg.mines);
    }

    protected void resetGame() {
        MinesweeperGame.setSavedGame(null);
        explosions.clear();
        board = new GameBoard(new GameSettings(MinigameRegistry.getConfig(MinesweeperVisibleConfig.class).gridWidth, MinigameRegistry.getConfig(MinesweeperVisibleConfig.class).gridHeight, MinigameRegistry.getConfig(MinesweeperVisibleConfig.class).mines));
        board.setSoundCallback(makeSoundCallback());
        this.init();
    }

    protected GameBoard.SoundCallback makeSoundCallback() {
        return new GameBoard.SoundCallback() {
            MinesweeperVisibleConfig cfg = MinigameRegistry.getConfig(MinesweeperVisibleConfig.class);
            public void onReveal() {
                mc.getSoundManager().play(PositionedSoundInstance.master(
                    SoundEvents.BLOCK_DEEPSLATE_BREAK, 0.25f, (float) cfg.soundsVolume /100));
            }
            public void onExplode(int cellX, int cellY) {
                mc.getSoundManager().play(PositionedSoundInstance.master(
                    SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.7f, (float) cfg.soundsVolume /100));
                if (cfg.enableExplosionAnimation) {
                    int cx = gridX + cellX * cellSize + cellSize / 2;
                    int cy = gridY + cellY * cellSize + cellSize / 2;
                    explosions.add(new ExplosionAnimation(cx, cy, cellSize * 3));
                }
            }
            public void onWin() {
                mc.getSoundManager().play(PositionedSoundInstance.master(
                    SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, (float) cfg.soundsVolume /100));
            }
        };
    }

    @Override
    public void close() {
        if (board != null) MinesweeperGame.setSavedGame(board.toSavedGame());
        super.close();
        if (parent!=null) mc.setScreen(parent);
    }

    @Override
    public void tick() {
        if (board == null) return;
        board.tick(MinigameRegistry.getConfig(MinesweeperVisibleConfig.class).enableAnimations);
        updateSmileyState();
        explosions.forEach(ExplosionAnimation::tick);
        explosions.removeIf(e -> e.done);
    }

    private void updateSmileyState() {
        if (smileyBtn == null) return;
        if (!board.alive) smileyBtn.setState(SmileyButtonWidget.State.LOSE);
        else if (board.won) smileyBtn.setState(SmileyButtonWidget.State.WIN);
        else smileyBtn.setState(SmileyButtonWidget.State.PLAYING);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MinesweeperVisibleConfig cfg = MinigameRegistry.getConfig(MinesweeperVisibleConfig.class);
        if (board == null) return super.mouseClicked(mouseX, mouseY, button);

        if (!board.alive || board.won) {
            if (button == 0) { resetGame(); return true; }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int gx = (int) Math.floor((mouseX - gridX) / (double) cellSize);
        int gy = (int) Math.floor((mouseY - gridY) / (double) cellSize);
        if (gx < 0 || gx >= board.w || gy < 0 || gy >= board.h)
            return super.mouseClicked(mouseX, mouseY, button);

        mc.getSoundManager().play(PositionedSoundInstance.master(
            SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 0.20f, (float) cfg.soundsVolume /100));

        Cell c = board.grid[gy][gx];

        if (button == 1) {
            if (!c.revealed) board.toggleFlag(gx, gy);
            MinesweeperGame.setSavedGame(board.toSavedGame());
            return true;
        }

        if (button == 0 && c.revealed) {
            board.chord(gx, gy, cfg.enableAnimations);
            MinesweeperGame.setSavedGame(board.toSavedGame());
            return true;
        }

        if (button == 0 && !c.flagged && !c.revealed) {
            if (board.firstClick) {
                board.placeMinesAvoiding(gx, gy);
                board.firstClick = false;
                board.timerRunning = true;
                board.timerStartMs = System.currentTimeMillis();
                board.elapsedSeconds = 0;
            }
            if (!cfg.enableAnimations) {
                mc.getSoundManager().play(PositionedSoundInstance.master(
                    SoundEvents.BLOCK_DEEPSLATE_BREAK, 0.25f, (float) cfg.soundsVolume /100));
            }
            board.startRevealWave(gx, gy, cfg.enableAnimations);
            MinesweeperGame.setSavedGame(board.toSavedGame());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (board == null) return;
        drawTopBar(context);
        drawGrid(context, mouseX, mouseY);
        explosions.forEach(e -> e.render(context));
    }

    private void drawTopBar(DrawContext context) {
        int barY2 = topBarY + TOP_BAR_H;
        int barX2 = topBarX + topBarW;
        int hx1 = smileyBtn.getX(), hx2 = hx1 + smileyBtn.getWidth();
        int hy1 = smileyBtn.getY(), hy2 = hy1 + smileyBtn.getHeight();
        int bg = 0xFF2B2B2B, border = 0xFF555555;

        context.fill(topBarX, topBarY, hx1,  barY2, bg);
        context.fill(hx2, topBarY, barX2, barY2, bg);
        context.fill(hx1, topBarY, hx2, hy1, bg);
        context.fill(hx1, hy2, hx2, barY2, bg);

        context.fill(topBarX, topBarY, barX2, topBarY + 1, border);
        context.fill(topBarX, barY2 - 1,  barX2, barY2, border);
        context.fill(topBarX, topBarY, topBarX + 1, barY2, border);
        context.fill(barX2 - 1,  topBarY, barX2, barY2, border);

        minesDisplay.setValue(Math.max(0, board.mines - board.flaggedCount));
        timerDisplay.setValue(board.elapsedSeconds);
        minesDisplay.render(context);
        timerDisplay.render(context);
    }

    private void drawGrid(DrawContext context, int mouseX, int mouseY) {
        MatrixStack matrices = context.getMatrices();
        double scaleFactor = mc.getWindow().getScaleFactor();
        MinesweeperVisibleConfig cfg = MinigameRegistry.getConfig(MinesweeperVisibleConfig.class);
        float uiScale = cellSize / 24f;
        float textScale = Math.max(0.70f, Math.min(1.30f, uiScale * 1.05f));
        int texSize = Math.max(10, Math.min(cellSize - 2, (int) (cellSize * 0.78f)));
        int border = 0xFF555555;
        int half = cellSize / 2;
        float invScale = (float) (1.0 / scaleFactor);

        for (int yy = 0; yy < board.h; yy++) {
            for (int xx = 0; xx < board.w; xx++) {
                int x = gridX + xx * cellSize;
                int y = gridY + yy * cellSize;
                int cx = x + half, cy = y + half;
                Cell c = board.grid[yy][xx];

                boolean hovered = mouseX >= x && mouseX < x + cellSize
                               && mouseY >= y && mouseY < y + cellSize;
                boolean revealedBg = c.revealed && !(c.flagged && !board.alive && !board.won);
                int bg = revealedBg ? 0xFF2D2D2D : 0xFF454545;
                if (hovered && !revealedBg) bg = brighten(bg, 0.10f);

                float cellScale = 1.0f;
                if (cfg.enableAnimations && c.revealProgress >= 0f && c.revealProgress < 1f) {
                    float t = c.revealProgress < 0.5f ? c.revealProgress / 0.5f : (1f - c.revealProgress) / 0.5f;
                    cellScale = 1.0f + t * 0.15f;
                }

                matrices.push();
                if (cellScale != 1.0f) {
                    matrices.translate(cx, cy, 0f);
                    matrices.scale(cellScale, cellScale, 1f);
                    matrices.translate(-cx, -cy, 0f);
                }

                context.fill(x, y, x + cellSize, y + cellSize, bg);

                matrices.push();
                matrices.scale(invScale, invScale, 1.0f);
                int px1 = (int) Math.round(x * scaleFactor);
                int py1 = (int) Math.round(y * scaleFactor);
                int px2 = (int) Math.round((x + cellSize) * scaleFactor);
                int py2 = (int) Math.round((y + cellSize) * scaleFactor);
                context.fill(px1, py1, px2, py1 + 1, border);
                context.fill(px1, py2 - 1, px2, py2, border);
                context.fill(px1, py1, px1 + 1, py2, border);
                context.fill(px2 - 1, py1, px2, py2, border);
                matrices.pop();

                if (c.revealed || c.flagged) {
                    drawCellContent(context, c, cx, cy, texSize, textScale);
                }

                matrices.pop();
            }
        }
    }

    private void drawCellContent(DrawContext context, Cell c, int cx, int cy, int texSize, float textScale) {

        MatrixStack matrices = context.getMatrices();
        if (c.revealed) {
            if (c.mine && !board.won) {
                Identifier tex = (c.flagged && !board.alive) ? TEX_FLAG : TEX_TNT_SIDE;
                context.drawTexture(tex, cx - texSize / 2, cy - texSize / 2, 0, 0, texSize, texSize, texSize, texSize);
            } else if (c.adjacent > 0) {
                Text numText = ADJ_TEXT[c.adjacent];
                int color = getAdjColor(c.adjacent);
                matrices.push();
                matrices.translate(cx, cy, 0f);
                if (textScale != 1.0f) matrices.scale(textScale, textScale, 1f);
                matrices.translate(
                    -mc.textRenderer.getWidth(numText) / 2f,
                    -mc.textRenderer.fontHeight / 2f, 0f);
                context.drawText(mc.textRenderer, numText, 0, 0, color, false);
                matrices.pop();
            }
        } else if (c.flagged) {
            Identifier tex = (!board.alive && !board.won && !c.mine) ? TEX_BARRIER : TEX_FLAG;
            context.drawTexture(tex, cx - texSize / 2, cy - texSize / 2, 0, 0, texSize, texSize, texSize, texSize);
        }
    }

    private int getAdjColor(int adj) {
        return switch (adj) {
            case 1 -> 0xFF3EB2FF;
            case 2 -> 0xFF41D45E;
            case 3 -> 0xFFFF4E4E;
            case 4 -> 0xFF7757FF;
            case 5 -> 0xFFFFA84E;
            case 6 -> 0xFF4EE0FF;
            case 7 -> 0xFFFFFFFF;
            default -> 0xFFBBBBBB;
        };
    }

    private static int brighten(int color, float amount) {
        int a = (color >>> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >>> 16) & 0xFF) * (1f + amount)));
        int g = Math.min(255, (int) (((color >>> 8)  & 0xFF) * (1f + amount)));
        int b = Math.min(255, (int) ((color & 0xFF)           * (1f + amount)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean shouldPause() { return false; }
}
