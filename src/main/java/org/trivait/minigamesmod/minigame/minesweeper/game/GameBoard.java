package org.trivait.minigamesmod.minigame.minesweeper.game;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.Random;

public class GameBoard {
    public final int w, h, mines;
    public final Cell[][] grid;

    public boolean minesPlaced = false;
    public int remainingSafe = -1;
    public boolean alive = true;
    public boolean won = false;
    public boolean firstClick = true;
    public int flaggedCount = 0;

    public boolean timerRunning = false;
    public long timerStartMs = 0L;
    public int elapsedSeconds = 0;

    private int[] activeCells = new int[256];
    private int activeCount = 0;

    private SoundCallback soundCallback;

    private final Random rng = new Random();

    public interface SoundCallback {
        void onReveal();
        void onExplode(int cellX, int cellY);
        void onWin();
    }

    public GameBoard(GameSettings settings) {
        this.w = settings.width();
        this.h = settings.height();
        this.mines = settings.mines();
        this.grid = new Cell[h][w];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                grid[y][x] = new Cell();
    }

    public GameBoard(SavedGame sg) {
        this.w = sg.w;
        this.h = sg.h;
        this.mines = sg.mines;
        this.grid = new Cell[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                Cell src = (sg.cells != null && sg.cells.length == w * h) ? sg.cells[idx] : null;
                Cell c = new Cell();
                if (src != null) {
                    c.mine = src.mine;
                    c.revealed = src.revealed;
                    c.flagged = src.flagged;
                    c.adjacent = src.adjacent;
                    c.revealProgress = src.revealProgress;
                    c.delayTicks = src.delayTicks;
                    c.scheduled = src.scheduled;
                }
                grid[y][x] = c;
            }
        }
        this.minesPlaced = sg.minesPlaced;
        this.remainingSafe = sg.remainingSafe;
        this.alive = sg.alive;
        this.won = sg.won;
        this.firstClick = sg.firstClick;
        this.elapsedSeconds = Math.max(0, Math.min(999, sg.elapsedSeconds));
        this.timerRunning = sg.timerRunning && alive && !won;
        this.timerStartMs = System.currentTimeMillis() - (elapsedSeconds * 1000L);
        int flags = 0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (grid[y][x].flagged) flags++;
        this.flaggedCount = flags;
    }

    public void setSoundCallback(SoundCallback cb) {
        this.soundCallback = cb;
    }

    public SavedGame toSavedGame() {
        SavedGame sg = new SavedGame();
        sg.w = w; sg.h = h; sg.mines = mines;
        sg.minesPlaced = minesPlaced;
        sg.remainingSafe = remainingSafe;
        sg.alive = alive; sg.won = won; sg.firstClick = firstClick;
        sg.timerRunning = timerRunning;
        sg.elapsedSeconds = elapsedSeconds;
        sg.cells = new Cell[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cell src = grid[y][x];
                Cell dst = new Cell();
                dst.mine = src.mine;
                dst.revealed = src.revealed;
                dst.flagged = src.flagged;
                dst.adjacent = src.adjacent;
                dst.revealProgress = src.revealProgress;
                dst.delayTicks = src.delayTicks;
                dst.scheduled = src.scheduled;
                sg.cells[y * w + x] = dst;
            }
        }
        return sg;
    }

    public void chord(int x, int y, boolean animations) {
        Cell c = grid[y][x];
        if (!c.revealed || c.mine || c.adjacent <= 0) return;
        int flagsAround = 0;
        for (int dy = -1; dy <= 1; dy++)
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && nx < w && ny >= 0 && ny < h && grid[ny][nx].flagged) flagsAround++;
            }
        if (flagsAround != c.adjacent) return;
        for (int dy = -1; dy <= 1; dy++)
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                    Cell n = grid[ny][nx];
                    if (!n.revealed && !n.flagged) startRevealWave(nx, ny, animations);
                }
            }
    }

    public void placeMinesAvoiding(int avoidX, int avoidY) {
        int placed = 0;
        while (placed < mines) {
            int x = rng.nextInt(w), y = rng.nextInt(h);
            if (Math.abs(x - avoidX) <= 1 && Math.abs(y - avoidY) <= 1 || grid[y][x].mine) continue;
            grid[y][x].mine = true;
            placed++;
        }
        minesPlaced = true;
        remainingSafe = (w * h) - mines;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (grid[y][x].mine) { grid[y][x].adjacent = -1; continue; }
                int count = 0;
                for (int dy = -1; dy <= 1; dy++)
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h && grid[ny][nx].mine) count++;
                    }
                grid[y][x].adjacent = count;
            }
        }
    }

    public void toggleFlag(int x, int y) {
        Cell c = grid[y][x];
        if (c.revealed) return;
        c.flagged = !c.flagged;
        flaggedCount += c.flagged ? 1 : -1;
        if (flaggedCount < 0) flaggedCount = 0;
    }

    public void startRevealWave(int sx, int sy, boolean animations) {
        Cell start = grid[sy][sx];
        if (start.mine) {
            start.revealed = true;
            alive = false;
            timerRunning = false;
            if (soundCallback != null) soundCallback.onExplode(sx, sy);
            for (int yy = 0; yy < h; yy++)
                for (int xx = 0; xx < w; xx++)
                    if (grid[yy][xx].mine) grid[yy][xx].revealed = true;
            return;
        }

        Queue<int[]> queue = new ArrayDeque<>();
        if (animations) {
            start.scheduled = true;
            start.delayTicks = 0;
            addActiveCell(sy * w + sx);
            queue.add(new int[]{sx, sy});
            while (!queue.isEmpty()) {
                int[] pos = queue.poll();
                int cx = pos[0], cy = pos[1];
                Cell c = grid[cy][cx];
                if (c.adjacent == 0) {
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = cx + dx, ny = cy + dy;
                            if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                                Cell n = grid[ny][nx];
                                if (!n.scheduled && !n.flagged && !n.mine) {
                                    n.scheduled = true;
                                    n.delayTicks = c.delayTicks + 3;
                                    addActiveCell(ny * w + nx);
                                    queue.add(new int[]{nx, ny});
                                }
                            }
                        }
                }
            }
        } else {
            queue.add(new int[]{sx, sy});
            while (!queue.isEmpty()) {
                int[] pos = queue.poll();
                int cx = pos[0], cy = pos[1];
                Cell c = grid[cy][cx];
                if (c.revealed || c.flagged || c.mine) continue;
                revealCell(cx, cy, false);
                if (c.adjacent == 0) {
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = cx + dx, ny = cy + dy;
                            if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                                Cell n = grid[ny][nx];
                                if (!n.revealed && !n.flagged && !n.mine)
                                    queue.add(new int[]{nx, ny});
                            }
                        }
                }
            }
        }
    }

    public void revealCell(int x, int y, boolean animations) {
        Cell c = grid[y][x];
        if (c.revealed) return;
        c.revealed = true;
        c.revealProgress = animations ? 0f : 1f;
        c.delayTicks = -1;
        addActiveCell(y * w + x);
        if (animations && soundCallback != null) soundCallback.onReveal();
        if (!c.mine) {
            remainingSafe--;
            if (remainingSafe <= 0) handleWin();
        }
    }

    private void handleWin() {
        if (!alive || won) return;
        won = true;
        timerRunning = false;
        for (int yy = 0; yy < h; yy++)
            for (int xx = 0; xx < w; xx++) {
                Cell c = grid[yy][xx];
                if (!c.mine) { c.revealed = true; c.revealProgress = 1f; c.delayTicks = -1; }
            }
        if (soundCallback != null) soundCallback.onWin();
    }

    public void tick(boolean animations) {
        if (timerRunning) {
            int sec = (int) ((System.currentTimeMillis() - timerStartMs) / 1000L);
            elapsedSeconds = Math.min(999, Math.max(0, sec));
        }
        if (!animations) return;
        for (int i = 0; i < activeCount; ) {
            int idx = activeCells[i];
            Cell c = cellAtIndex(idx);
            boolean keep = false;
            if (c.delayTicks > 0) {
                c.delayTicks--;
                keep = true;
            } else if (c.delayTicks == 0 && !c.revealed && c.scheduled) {
                int cy = idx / w, cx = idx % w;
                revealCell(cx, cy, true);
                c.scheduled = false;
                keep = true;
            }
            if (c.revealProgress >= 0f && c.revealProgress < 1f) {
                c.revealProgress = Math.min(1f, c.revealProgress + 0.2f);
                keep = true;
            }
            if (!keep) { activeCells[i] = activeCells[activeCount - 1]; activeCount--; continue; }
            i++;
        }
    }

    private void addActiveCell(int idx) {
        if (activeCount >= activeCells.length)
            activeCells = Arrays.copyOf(activeCells, activeCells.length * 2);
        activeCells[activeCount++] = idx;
    }

    private Cell cellAtIndex(int idx) {
        return grid[idx / w][idx % w];
    }
}
