package org.trivait.minigamesmod.minigame.game2048;

import com.sun.jna.platform.win32.GL;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.MinigamesButton;
import org.trivait.minigamesmod.minigame.tetris.TetrisVisibleConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Game2048Screen extends Screen {

    private final Game2048 minigame;
    private final Screen parent;

    private int size;
    private int[][] grid;
    private int[][] animFromGrid;
    private long score = 0;
    private boolean gameOver = false;
    private boolean won = false;

    private static final int GAP = 6;
    private static final int BOARD_RADIUS = 6;
    private static final long ANIM_DURATION = 120;
    private static final long COLOR_ANIM_DURATION = 300;

    private final List<TileAnim> animations = new ArrayList<>();
    private long animStartTime = -ANIM_DURATION;
    private long colorAnimStart = 0;
    private boolean colorAnimActive = false;

    private static class TileAnim {
        float fromX, fromY, toX, toY;
        int value;
        boolean merge;

        TileAnim(float fromX, float fromY, float toX, float toY, int value, boolean merge) {
            this.fromX = fromX; this.fromY = fromY;
            this.toX = toX;     this.toY = toY;
            this.value = value; this.merge = merge;
        }
    }

    public Game2048Screen(Game2048 minigame, Screen parent) {
        super(Text.translatable("minigame.2048.title"));
        this.minigame = minigame;
        this.parent = parent;
    }

    private static final int PANEL_H = 36;
    private static final int SCORE_H = 44;

    private int cellSize() {
        int availH = this.height - PANEL_H - SCORE_H - 16;
        int maxBoard = Math.min(this.width - 20, availH);

        int cell = (maxBoard - GAP) / size - GAP;

        return Math.min(48, cell);
    }

    private int boardW() {
        return size * (cellSize() + GAP) + GAP;
    }

    private int boardX() { return (this.width - boardW()) / 2; }

    private int boardY() {
        int usableTop = SCORE_H + 8;
        int usableBot = this.height - PANEL_H;
        return usableTop + (usableBot - usableTop - boardW()) / 2;
    }

    @Override
    protected void init() {
        Game2048VisibleConfig cfg = MinigameRegistry.getConfig(Game2048VisibleConfig.class);
        size = cfg.gridSize;
        grid = new int[size][size];
        animFromGrid = new int[size][size];
        score = 0; gameOver = false; won = false;
        animations.clear();
        colorAnimActive = false;
        animStartTime = -ANIM_DURATION;
        spawnTile(); spawnTile();
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearChildren();
        int btnY = this.height - 28;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), b -> this.close())
                .dimensions(8, btnY, 56, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("minigame.restart"), b -> game2048Init())
                .dimensions(68, btnY, 70, 20).build());
        TextIconButtonWidget configBtn = TextIconButtonWidget.builder(
                Text.empty(),
                (button) -> {
                    if (client != null) {
                        client.setScreen(AutoConfig.getConfigScreen(Game2048VisibleConfig.class, this).get());
                    }
                },
                true
        ).width(20).texture(Identifier.of(MinigamesMod.MOD_ID, "icon/config"), 18, 18).build();
        configBtn.setPosition(142, btnY);
        this.addDrawableChild(configBtn);
    }

    private void game2048Init() {
        Game2048VisibleConfig cfg = MinigameRegistry.getConfig(Game2048VisibleConfig.class);
        size = cfg.gridSize;
        grid = new int[size][size];
        animFromGrid = new int[size][size];
        score = 0; gameOver = false; won = false;
        animations.clear();
        colorAnimActive = false;
        animStartTime = -ANIM_DURATION;
        spawnTile(); spawnTile();
        rebuildButtons();
    }

    private void spawnTile() {
        List<int[]> empty = new ArrayList<>();
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] == 0) empty.add(new int[]{r, c});
        if (empty.isEmpty()) return;
        int[] pos = empty.get((int)(Math.random() * empty.size()));
        grid[pos[0]][pos[1]] = Math.random() < 0.9 ? 2 : 4;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (gameOver) return super.keyPressed(keyCode, scanCode, modifiers);
        int dr = 0, dc = 0;
        Random random = new Random();

        switch (keyCode) {
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> {
                dr = -1;
                PlayingSoundManager.playSound(SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM, random.nextFloat(0.9f, 1.2f), vol());
            }
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> {
                dr = 1;
                PlayingSoundManager.playSound(SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM, random.nextFloat(0.9f, 1.2f), vol());
            }
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> {
                dc = -1;
                PlayingSoundManager.playSound(SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM, random.nextFloat(0.9f, 1.2f), vol());
            }
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> {
                dc = 1;
                PlayingSoundManager.playSound(SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM, random.nextFloat(0.9f, 1.2f), vol());
            }
            default -> { return super.keyPressed(keyCode, scanCode, modifiers); }
        }
        if (slide(dr, dc)) {
            spawnTile();
            if (!canMove()) { gameOver = true; saveBestScore(); minigame.onLose(); }
        }
        return true;
    }

    private boolean slide(int dr, int dc) {
        boolean moved = false;
        boolean[][] merged = new boolean[size][size];

        int cell = cellSize();
        int bx = boardX(), by = boardY();

        int[][] snapshot = new int[size][size];
        for (int r = 0; r < size; r++) snapshot[r] = Arrays.copyOf(grid[r], size);

        int rowStart = dr > 0 ? size - 1 : 0, rowEnd = dr > 0 ? -1 : size, rowStep = dr > 0 ? -1 : 1;
        int colStart = dc > 0 ? size - 1 : 0, colEnd = dc > 0 ? -1 : size, colStep = dc > 0 ? -1 : 1;

        animations.clear();

        for (int r = rowStart; r != rowEnd; r += rowStep) {
            for (int c = colStart; c != colEnd; c += colStep) {
                if (grid[r][c] == 0) continue;
                int tr = r, tc = c;
                while (true) {
                    int nr = tr + dr, nc = tc + dc;
                    if (nr < 0 || nr >= size || nc < 0 || nc >= size) break;
                    if (grid[nr][nc] == 0) { tr = nr; tc = nc; }
                    else if (grid[nr][nc] == grid[r][c] && !merged[nr][nc]) { tr = nr; tc = nc; break; }
                    else break;
                }
                if (tr == r && tc == c) continue;

                boolean isMerge = grid[tr][tc] == grid[r][c] && !merged[tr][tc];
                float fx = bx + GAP + c  * (cell + GAP) + cell / 2f;
                float fy = by + GAP + r  * (cell + GAP) + cell / 2f;
                float tx = bx + GAP + tc * (cell + GAP) + cell / 2f;
                float ty = by + GAP + tr * (cell + GAP) + cell / 2f;
                animations.add(new TileAnim(fx, fy, tx, ty, grid[r][c], isMerge));

                if (isMerge) {
                    grid[tr][tc] *= 2; score += grid[tr][tc]; merged[tr][tc] = true;
                    if (grid[tr][tc] == 2048 && !won) { won = true; saveBestScore(); minigame.onWin(); }
                } else {
                    grid[tr][tc] = grid[r][c];
                }
                grid[r][c] = 0;
                moved = true;
            }
        }

        if (moved) {
            animFromGrid = snapshot;
            animStartTime = System.currentTimeMillis();
            colorAnimStart = System.currentTimeMillis();
            colorAnimActive = true;
        }
        return moved;
    }

    private void saveBestScore() {
        Game2048Config cfg = MinigameRegistry.getConfig(Game2048Config.class);
        if (score > cfg.bestScore) { cfg.bestScore = score; AutoConfig.getConfigHolder(Game2048Config.class).save(); }
    }

    private boolean canMove() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++) {
                if (grid[r][c] == 0) return true;
                if (r + 1 < size && grid[r][c] == grid[r + 1][c]) return true;
                if (c + 1 < size && grid[r][c] == grid[r][c + 1]) return true;
            }
        return false;
    }

    private int tileColor(int val) {
        return switch (val) {
            case 2    -> 0xFFEEE4DA;
            case 4    -> 0xFFEDE0C8;
            case 8    -> 0xFFF2B179;
            case 16   -> 0xFFF59563;
            case 32   -> 0xFFF67C5F;
            case 64   -> 0xFFF65E3B;
            case 128  -> 0xFFEDCF72;
            case 256  -> 0xFFEDCC61;
            case 512  -> 0xFFEDC850;
            case 1024 -> 0xFFEDC53F;
            case 2048 -> 0xFFEDC22E;
            default   -> 0xFF3C3A32;
        };
    }

    private int lerpColor(int from, int to, float t) {
        int ar = (from >> 16) & 0xFF, ag = (from >> 8) & 0xFF, ab = from & 0xFF;
        int br = (to   >> 16) & 0xFF, bg = (to   >> 8) & 0xFF, bb = to   & 0xFF;
        return 0xFF000000 | ((int)(ar + (br - ar) * t) << 16) | ((int)(ag + (bg - ag) * t) << 8) | (int)(ab + (bb - ab) * t);
    }

    private void fillRoundedRect(DrawContext context, int x, int y, int w, int h, int r, int color) {
        context.fill(x + r, y, x + w - r, y + h, color);
        context.fill(x, y + r, x + r, y + h - r, color);
        context.fill(x + w - r, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                if (Math.sqrt((double)(r - 1 - i) * (r - 1 - i) + (double)(r - 1 - j) * (r - 1 - j)) < r) {
                    context.fill(x + i, y + j, x + i + 1, y + j + 1, color);
                    context.fill(x + w - 1 - i, y + j, x + w - i, y + j + 1, color);
                    context.fill(x + i, y + h - 1 - j, x + i + 1, y + h - j, color);
                    context.fill(x + w - 1 - i, y + h - 1 - j, x + w - i, y + h - j, color);
                }
            }
        }
    }

    private void drawTile(DrawContext context, int val, int x, int y, int cell, int color) {
        fillRoundedRect(context, x, y, cell, cell, 4, color);
        String label = String.valueOf(val);
        float targetH = cell * 0.42f;
        float scale = targetH / 9f;
        int textW = textRenderer.getWidth(label);
        float scaleW = (float)(cell - 8) / (textW * scale > 0 ? textW : 1);
        if (scaleW < 1f) scale *= scaleW;
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x + cell / 2f, y + cell / 2f - (9f * scale) / 2f, 0);
        matrices.scale(scale, scale, 1f);
        context.drawCenteredTextWithShadow(textRenderer, label, 0, 0, 0xFFFFFFFF);
        matrices.pop();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cell = cellSize();
        int bw = boardW();
        int bx = boardX(), by = boardY();

        long now = System.currentTimeMillis();
        float moveP = Math.min(1f, (float)(now - animStartTime) / ANIM_DURATION);
        float eased = 1f - (1f - moveP) * (1f - moveP);
        boolean animating = moveP < 1f && !animations.isEmpty();

        float colorT = colorAnimActive ? Math.min(1f, (float)(now - colorAnimStart) / COLOR_ANIM_DURATION) : 1f;
        if (colorAnimActive && colorT >= 1f) colorAnimActive = false;

        fillRoundedRect(context, bx, by, bw, bw, BOARD_RADIUS, 0xFFBBADA0);
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                fillRoundedRect(context, bx + GAP + c * (cell + GAP), by + GAP + r * (cell + GAP), cell, cell, 4, 0xFFCDC1B4);

        if (animating) {
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    int val = animFromGrid[r][c];
                    if (val == 0) continue;
                    boolean movedAway = false;
                    for (TileAnim a : animations) {
                        int sr = Math.round((a.fromY - by - GAP) / (cell + GAP));
                        int sc = Math.round((a.fromX - bx - GAP) / (cell + GAP));
                        if (sr == r && sc == c) { movedAway = true; break; }
                    }
                    if (!movedAway) {
                        int x = bx + GAP + c * (cell + GAP);
                        int y = by + GAP + r * (cell + GAP);
                        drawTile(context, val, x, y, cell, tileColor(val));
                    }
                }
            }
            for (TileAnim anim : animations) {
                float ax = anim.fromX + (anim.toX - anim.fromX) * eased;
                float ay = anim.fromY + (anim.toY - anim.fromY) * eased;
                float sf = anim.merge ? (1f + 0.15f * (float)Math.sin(moveP * Math.PI)) : 1f;
                int ts = (int)(cell * sf);
                drawTile(context, anim.value, (int)(ax - ts / 2f), (int)(ay - ts / 2f), ts, tileColor(anim.value));
            }
        } else {
            animations.clear();
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    int val = grid[r][c];
                    if (val == 0) continue;
                    int x = bx + GAP + c * (cell + GAP);
                    int y = by + GAP + r * (cell + GAP);
                    int color = tileColor(val);
                    if (colorAnimActive && animFromGrid != null) {
                        int fromVal = animFromGrid[r][c];
                        int fromColor = fromVal == 0 ? 0xFFCDC1B4 : tileColor(fromVal);
                        color = lerpColor(fromColor, color, colorT);
                    }
                    drawTile(context, val, x, y, cell, color);
                }
            }
        }

        long best = MinigameRegistry.getConfig(Game2048Config.class).bestScore;
        int sbW = bw/2-4, sbH = 32;
        int sx = bx + sbW+8, sy = by - sbH - 6;
        fillRoundedRect(context, sx, sy, sbW, sbH, 4, 0xFFBBADA0);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("minigame.2048.score"), sx + sbW / 2, sy + 4, 0xFFEEE4DA);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(String.valueOf(score)), sx + sbW / 2, sy + 16, 0xFFFFFFFF);
        int bsX = bx;
        fillRoundedRect(context, bsX, sy, sbW, sbH, 4, 0xFFBBADA0);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("minigame.2048.best"), bsX + sbW / 2, sy + 4, 0xFFEEE4DA);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(String.valueOf(best)), bsX + sbW / 2, sy + 16, 0xFFFFFFFF);

        if (gameOver || won) {
            context.fill(bx, by, bx + bw, by + bw, 0xAA776E65);
            Text msg = gameOver ? Text.translatable("minigame.2048.game_over") : Text.translatable("minigame.2048.win");
            int msgColor = gameOver ? 0xFFFF4444 : 0xFF44FF88;
            context.drawCenteredTextWithShadow(textRenderer, msg, this.width / 2, by + bw / 2 - 10, msgColor);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(String.valueOf(score)), this.width / 2, by + bw / 2 + 4, 0xFFFFFFFF);
        }
    }

    private float vol() {
        return PlayingSoundManager.vol(MinigameRegistry.getConfig(Game2048VisibleConfig.class).volume);
    }

    @Override
    public void close() { minigame.onStop(); this.client.setScreen(parent); }

    @Override
    public boolean shouldPause() { return false; }
}
