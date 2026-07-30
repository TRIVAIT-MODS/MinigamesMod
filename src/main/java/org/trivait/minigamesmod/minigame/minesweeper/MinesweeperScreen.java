package org.trivait.minigamesmod.minigame.minesweeper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.joml.Matrix3x2fStack;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.gui.widget.ConfigButton;
import org.trivait.minigamesmod.minigame.minesweeper.game.*;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.DigitDisplayWidget;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.ExplosionAnimation;
import org.trivait.minigamesmod.minigame.minesweeper.screen.widget.SmileyButtonWidget;

import java.util.ArrayList;
import java.util.List;

public class MinesweeperScreen extends Screen {

    private static final Identifier TEX_FLAG     = Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/minesweeper/flag.png");
    private static final Identifier TEX_BARRIER  = Identifier.withDefaultNamespace("textures/item/barrier.png");
    private static final Identifier TEX_TNT_SIDE = Identifier.withDefaultNamespace("textures/block/tnt_side.png");

    private static final Component[] ADJ_TEXT = {
        Component.empty(),
        Component.literal("1").withStyle(s -> s.withBold(true)),
        Component.literal("2").withStyle(s -> s.withBold(true)),
        Component.literal("3").withStyle(s -> s.withBold(true)),
        Component.literal("4").withStyle(s -> s.withBold(true)),
        Component.literal("5").withStyle(s -> s.withBold(true)),
        Component.literal("6").withStyle(s -> s.withBold(true)),
        Component.literal("7").withStyle(s -> s.withBold(true)),
        Component.literal("8").withStyle(s -> s.withBold(true)),
    };

    private static final int TOP_BAR_H = 28;

    private final Minecraft mc = Minecraft.getInstance();
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

    private Button leaderboardButton;

    private MinesweeperGame minigame;
    private Screen parent;

    public MinesweeperScreen(GameSettings settings, GameMode gameMode, MinesweeperGame minigame, Screen parent) {
        super(Component.empty());
        this.newGameSettings = settings;
        this.initialSave = null;
        this.gameMode = gameMode;
        this.minigame = minigame;
        this.parent = parent;
    }

    public MinesweeperScreen(SavedGame savedGame, GameMode gameMode, MinesweeperGame minigame, Screen parent) {
        super(Component.empty());
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

        this.clearWidgets();

        Button returnButton = SpriteIconButton.builder(Component.empty(), button -> this.onClose(), true)
                .sprite(Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "icon/return"), 15, 15).build();
        returnButton.setTooltip(Tooltip.create(Component.translatable("minigame.2048.undo")));
        returnButton.setRectangle(20, 20, 10, 10);

        Button restartButton = SpriteIconButton.builder(Component.empty(), button -> resetGame(), true)
                .sprite(Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "icon/restart"), 15, 15).build();
        restartButton.setTooltip(Tooltip.create(Component.translatable("minigame.restart")));
        restartButton.setRectangle(20, 20, 35, 10);

        this.addRenderableWidget(restartButton);
        this.addRenderableWidget(returnButton);

        int smileSize = Math.max(18, Math.min(26, TOP_BAR_H - 2));
        smileyBtn = new SmileyButtonWidget(
            this.width / 2 - smileSize / 2,
            topBarY + (TOP_BAR_H - smileSize) / 2,
            smileSize,
            this::resetGame
        );
        this.addRenderableWidget(smileyBtn);

        minesDisplay = new DigitDisplayWidget(3);
        timerDisplay = new DigitDisplayWidget(3);
        int dispY = topBarY + (TOP_BAR_H - minesDisplay.getHeight()) / 2;
        minesDisplay.setPosition(topBarX + 6, dispY);
        timerDisplay.setPosition(topBarX + topBarW - 6 - timerDisplay.getWidth(), dispY);

        if (gameMode == GameMode.DEFAULT) {

            this.addRenderableWidget(new ConfigButton(60, 10, minigame));
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
                mc.getSoundManager().play(SimpleSoundInstance.forUI(
                    SoundEvents.DEEPSLATE_BREAK, 0.25f, (float) cfg.soundsVolume /100));
            }
            public void onExplode(int cellX, int cellY) {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(
                    SoundEvents.GENERIC_EXPLODE.value(), 0.7f, (float) cfg.soundsVolume /100));
                if (cfg.enableExplosionAnimation) {
                    int cx = gridX + cellX * cellSize + cellSize / 2;
                    int cy = gridY + cellY * cellSize + cellSize / 2;
                    explosions.add(new ExplosionAnimation(cx, cy, cellSize * 3));
                }
            }
            public void onWin() {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(
                    SoundEvents.FIREWORK_ROCKET_BLAST, 0.8f, (float) cfg.soundsVolume /100));
            }
        };
    }

    @Override
    public void onClose() {
        if (board != null) MinesweeperGame.setSavedGame(board.toSavedGame());
        super.onClose();
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
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        int button = click.button();

        MinesweeperVisibleConfig cfg = MinigameRegistry.getConfig(MinesweeperVisibleConfig.class);
        if (board == null) return super.mouseClicked(click, doubled);

        if (!board.alive || board.won) {
            if (button == 0) { resetGame(); return true; }
            return super.mouseClicked(click, doubled);
        }

        int gx = (int) Math.floor((click.x() - gridX) / (double) cellSize);
        int gy = (int) Math.floor((click.y() - gridY) / (double) cellSize);
        if (gx < 0 || gx >= board.w || gy < 0 || gy >= board.h)
            return super.mouseClicked(click, doubled);

        mc.getSoundManager().play(SimpleSoundInstance.forUI(
            SoundEvents.NOTE_BLOCK_HAT.value(), 0.20f, (float) cfg.soundsVolume /100));

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
                mc.getSoundManager().play(SimpleSoundInstance.forUI(
                    SoundEvents.DEEPSLATE_BREAK, 0.25f, (float) cfg.soundsVolume /100));
            }
            board.startRevealWave(gx, gy, cfg.enableAnimations);
            MinesweeperGame.setSavedGame(board.toSavedGame());
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        if (board == null) return;
        drawTopBar(context);
        drawGrid(context, mouseX, mouseY);
        explosions.forEach(e -> e.render(context));
    }

    private void drawTopBar(GuiGraphicsExtractor context) {
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

    private void drawGrid(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        Matrix3x2fStack matrices = context.pose();
        double scaleFactor = mc.getWindow().getGuiScale();
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

                matrices.pushMatrix();
                if (cellScale != 1.0f) {
                    matrices.translate(cx, cy);
                    matrices.scale(cellScale, cellScale);
                    matrices.translate(-cx, -cy);
                }

                context.fill(x, y, x + cellSize, y + cellSize, bg);

                matrices.pushMatrix();
                matrices.scale(invScale, invScale);
                int px1 = (int) Math.round(x * scaleFactor);
                int py1 = (int) Math.round(y * scaleFactor);
                int px2 = (int) Math.round((x + cellSize) * scaleFactor);
                int py2 = (int) Math.round((y + cellSize) * scaleFactor);
                context.fill(px1, py1, px2, py1 + 1, border);
                context.fill(px1, py2 - 1, px2, py2, border);
                context.fill(px1, py1, px1 + 1, py2, border);
                context.fill(px2 - 1, py1, px2, py2, border);
                matrices.popMatrix();

                if (c.revealed || c.flagged) {
                    drawCellContent(context, c, cx, cy, texSize, textScale);
                }

                matrices.popMatrix();
            }
        }
    }

    private void drawCellContent(GuiGraphicsExtractor context, Cell c, int cx, int cy, int texSize, float textScale) {

        Matrix3x2fStack matrices = context.pose();
        if (c.revealed) {
            if (c.mine && !board.won) {
                Identifier tex = (c.flagged && !board.alive) ? TEX_FLAG : TEX_TNT_SIDE;
                context.blit(RenderPipelines.GUI_TEXTURED, tex, cx - texSize / 2, cy - texSize / 2, 0, 0, texSize, texSize, texSize, texSize);
            } else if (c.adjacent > 0) {
                Component numText = ADJ_TEXT[c.adjacent];
                int color = getAdjColor(c.adjacent);
                matrices.pushMatrix();
                matrices.translate(cx, cy);
                if (textScale != 1.0f) matrices.scale(textScale, textScale);
                matrices.translate(
                    -mc.font.width(numText) / 2f,
                    -mc.font.lineHeight / 2f);
                context.text(mc.font, numText, 0, 0, color, false);
                matrices.popMatrix();
            }
        } else if (c.flagged) {
            Identifier tex = (!board.alive && !board.won && !c.mine) ? TEX_BARRIER : TEX_FLAG;
            context.blit(RenderPipelines.GUI_TEXTURED, tex, cx - texSize / 2, cy - texSize / 2, 0, 0, texSize, texSize, texSize, texSize);
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
    public boolean isPauseScreen() { return false; }
}
