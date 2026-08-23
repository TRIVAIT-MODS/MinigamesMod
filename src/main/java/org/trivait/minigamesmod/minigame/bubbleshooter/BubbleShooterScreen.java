package org.trivait.minigamesmod.minigame.bubbleshooter;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3x2fStack;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.ConfigButton;
import org.trivait.minigamesmod.minigame.game2048.Game2048Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BubbleShooterScreen extends Screen {
    private final BubbleShooter minigame;
    private final Screen parent;

    private static final Identifier GUI_TEXTURE = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/bubble_shooter/gui.png");
    private static final Identifier BUBBLE_TEXTURE = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/bubble_shooter/bubble.png");

    //ohh this shitty fields...

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 250;
    private static final int GAME_WIDTH = GUI_WIDTH - 16;
    private static final int GAME_HEIGHT = GUI_HEIGHT - 16;
    private static final int GRID_COLS = 10;
    private static final int GRID_ROWS = 12;
    private static final int CELL_SIZE = GAME_WIDTH / GRID_COLS;
    private static final int BUBBLE_SIZE = CELL_SIZE - 2;
    private static final int BOARD_WIDTH = GRID_COLS * CELL_SIZE;
    private static final int BOARD_HEIGHT = GRID_ROWS * CELL_SIZE;
    private static final int BOARD_TOP_OFFSET = 4;
    private static final int LAUNCHER_OFFSET = 18;
    private static final int LOSS_LINE_OFFSET = 18;

    private static final float SHOT_SPEED = 240.0f;
    private static final float MAX_AIM_ANGLE = 1.1f;
    private static final float COLLISION_RADIUS = BUBBLE_SIZE * 1.05f;
    public static final float REMOVAL_TIME = 0.28f;
    public static final float FALL_TIME = 0.38f;
    private static final float LAYER_TIME = 0.25f;
    private static final int LAYER_INTERVAL = 5;

    private final Random random = new Random();

    private final int[] colors = {
            0xFFFF5A5A, 0xFF29D0FF, 0xFFFFD95A, 0xFF5AE47A,
            0xFFFF8B5E, 0xFFB982FF, 0xFFFF6FCE, 0xFF7DE6D6
    };

    private final List<Effect> effects = new ArrayList<>();

    private int[][] board;
    private int score;
    private int shots;

    private int currentColor;
    private int nextColor;

    private float angle;
    private float projectileX;
    private float projectileY;
    private float projectileVX;
    private float projectileVY;

    private float boardOffset;
    private float boardOffsetTarget;

    private float readyAnimation = 1.0f;
    private float nextAnimation = 1.0f;

    private boolean projectileActive;
    private boolean gameOver;
    private boolean defeatSoundPlayed;

    private long lastFrameNs = -1L;

    public BubbleShooterScreen(BubbleShooter minigame, Screen parent) {
        super(Text.empty());
        this.minigame = minigame;
        this.parent = parent;
    }

    @Override
    protected void init() {
        ButtonWidget returnButton = TextIconButtonWidget.builder(Text.empty(), b -> close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15)
                .build();
        returnButton.setTooltip(Tooltip.of(Text.translatable("minigame.2048.undo")));
        returnButton.setDimensionsAndPosition(20, 20, 10, 10);

        ButtonWidget restartButton = TextIconButtonWidget.builder(Text.empty(), b -> resetGame(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15)
                .build();
        restartButton.setTooltip(Tooltip.of(Text.translatable("minigame.restart")));
        restartButton.setDimensionsAndPosition(20, 20, 35, 10);

        addDrawableChild(restartButton);
        addDrawableChild(returnButton);
        addDrawableChild(new ConfigButton(60, 10, minigame));

        resetGame();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!gameOver) {
            boolean overButton = false;

            for (var child : children()) {
                if (child instanceof ButtonWidget buttonWidget && buttonWidget.isMouseOver(mouseX, mouseY)) {
                    overButton = true;
                    break;
                }
            }

            if (!overButton) {
                shoot();
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int guiX = (width - GUI_WIDTH) / 2;
        int guiY = (height - GUI_HEIGHT) / 2;
        int gameX = guiX + 8;
        int gameY = guiY + 8;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, guiX, guiY, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        drawScore(context);

        context.enableScissor(gameX, gameY, gameX+GAME_WIDTH, gameY+GAME_HEIGHT);

        MinigameRegistry.getConfig(BubbleShooterVisibleConfig.class).background.background.render(gameX, gameY, GAME_WIDTH, GAME_HEIGHT, context, delta, mouseX, mouseY);

        long now = System.nanoTime();

        if (lastFrameNs < 0) {
            lastFrameNs = now;
        }

        float dt = clamp((now - lastFrameNs) / 1_000_000_000f, 0f, 0.033f);

        lastFrameNs = now;

        update(dt);
        updateAim(mouseX, mouseY);

        drawBoard(context);

        if (!projectileActive && !gameOver && effects.isEmpty() && boardOffset > -0.1f) {
            drawTrajectory(context);
        }

        drawEffects(context);
        drawAimer(context);
        drawQueuedBubble(context);
        drawLossLine(context);

        if (gameOver) {
            drawGameOver(context);
        }

        context.disableScissor();
    }

    private void resetGame() {
        board = new int[GRID_COLS][GRID_ROWS];
        effects.clear();

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                board[col][row] = randomColor();
            }
        }

        score = 0;
        shots = 0;
        angle = 0;
        boardOffset = 0;
        boardOffsetTarget = 0;

        currentColor = randomColor();
        nextColor = randomColor();

        projectileActive = false;
        gameOver = false;
        defeatSoundPlayed = false;

        readyAnimation = 1f;
        nextAnimation = 1f;
        lastFrameNs = -1L;

        spawnProjectile();
    }

    private void update(float dt) {
        readyAnimation = Math.min(1f, readyAnimation + dt / 0.22f);
        nextAnimation = Math.min(1f, nextAnimation + dt / 0.22f);

        if (Math.abs(boardOffset - boardOffsetTarget) > 0.001f) {
            float distance = boardOffsetTarget - boardOffset;
            float maxStep = dt / LAYER_TIME;

            if (Math.abs(distance) <= 0.001f || maxStep >= 1f) {
                boardOffset = boardOffsetTarget;
            } else {
                boardOffset += distance * Math.min(1f, maxStep);
            }
        }

        if (projectileActive) {
            updateProjectile(dt);
        }

        for (int i = effects.size() - 1; i >= 0; i--) {
            Effect effect = effects.get(i);
            effect.update(dt);

            if (effect.dead()) {
                effects.remove(i);
            }
        }

        if (!gameOver && hasReachedLossLine()) {
            gameOver = true;
            updateHighScore();

            if (!defeatSoundPlayed) {
                defeatSoundPlayed = true;
                PlayingSoundManager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1f, vol());
                minigame.getLeaderboard().doPost(score);
            }
        }
    }

    private void updateAim(int mouseX, int mouseY) {
        angle = clamp((float) Math.atan2(mouseX - getLauncherX(), getLauncherY() - mouseY), -MAX_AIM_ANGLE, MAX_AIM_ANGLE);
    }

    private void shoot() {
        if (projectileActive || gameOver || readyAnimation < 0.95f || !effects.isEmpty()) {
            return;
        }

        projectileX = getLauncherX();
        projectileY = getLauncherY();
        projectileVX = (float) Math.sin(angle) * SHOT_SPEED;
        projectileVY = -(float) Math.cos(angle) * SHOT_SPEED;
        projectileActive = true;

        PlayingSoundManager.playSound(SoundEvents.BLOCK_CRAFTER_CRAFT, 1, vol());
    }

    private void spawnProjectile() {
        projectileX = getLauncherX();
        projectileY = getLauncherY();
        projectileVX = 0;
        projectileVY = 0;
        projectileActive = false;
    }

    private void updateProjectile(float dt) {
        float left = getBoardLeft() + BUBBLE_SIZE * 0.5f;
        float right = getBoardLeft() + BOARD_WIDTH - BUBBLE_SIZE * 0.5f;
        float top = getBoardTop() + BUBBLE_SIZE * 0.5f + boardOffset;

        float remaining = dt;

        while (remaining > 0 && projectileActive) {
            float step = Math.min(remaining, 1f / SHOT_SPEED);
            remaining -= step;

            float oldX = projectileX;
            float oldY = projectileY;

            float nx = oldX + projectileVX * step;
            float ny = oldY + projectileVY * step;

            if (nx < left) {
                projectileX = left;
                projectileY = ny;
                projectileVX = Math.abs(projectileVX);
                PlayingSoundManager.playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.5f, vol()/1.5f);
                continue;
            }

            if (nx > right) {
                projectileX = right;
                projectileY = ny;
                projectileVX = -Math.abs(projectileVX);
                PlayingSoundManager.playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.5f, vol()/1.5f);
                continue;
            }

            float topHit = rayHorizontalDistance(oldY, projectileVY, top);

            if (topHit >= 0 && topHit <= step) {
                projectileX = oldX + projectileVX * topHit;
                projectileY = top;

                PlayingSoundManager.playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, vol());
                lockProjectile(-1, -1);
                return;
            }

            float hit = findProjectileCollision(oldX, oldY, step);

            if (hit >= 0) {
                projectileX = oldX + projectileVX * hit;
                projectileY = oldY + projectileVY * hit;

                int[] bubble = findClosestBubble(projectileX, projectileY);

                if (bubble != null) {
                    PlayingSoundManager.playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, vol());
                    lockProjectile(bubble[0], bubble[1]);
                    return;
                }
            }

            projectileX = nx;
            projectileY = ny;
        }
    }

    private float findProjectileCollision(float x, float y, float maxTime) {
        float best = Float.MAX_VALUE;
        float radius = COLLISION_RADIUS;
        float radiusSq = radius * radius;
        float velocitySq = projectileVX * projectileVX + projectileVY * projectileVY;

        if (velocitySq < 0.001f) {
            return -1;
        }

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (board[col][row] == 0) {
                    continue;
                }

                float cx = getCellCenterX(col);
                float cy = getCellCenterY(row) + boardOffset;

                float fx = cx - x;
                float fy = cy - y;

                float projection = Math.max(
                        0,
                        (fx * projectileVX + fy * projectileVY) / velocitySq
                );

                float px = x + projectileVX * projection;
                float py = y + projectileVY * projection;

                float dx = cx - px;
                float dy = cy - py;

                float distanceSq = dx * dx + dy * dy;

                if (distanceSq > radiusSq) {
                    continue;
                }

                float offset = (float) Math.sqrt(
                        Math.max(0, (radiusSq - distanceSq) / velocitySq)
                );

                float time = Math.max(0, projection - offset);

                if (time <= maxTime && time < best) {
                    best = time;
                }
            }
        }

        return best == Float.MAX_VALUE ? -1 : best;
    }

    private int[] findClosestBubble(float x, float y) {
        int bx = -1;
        int by = -1;
        float best = Float.MAX_VALUE;

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (board[col][row] == 0) {
                    continue;
                }

                float d = distance(x, y, getCellCenterX(col), getCellCenterY(row) + boardOffset);

                if (d < best) {
                    best = d;
                    bx = col;
                    by = row;
                }
            }
        }

        return bx < 0 ? null : new int[]{bx, by};
    }

    private void lockProjectile(int hitCol, int hitRow) {
        int[] target = findAttachment(hitCol, hitRow);

        if (target == null) {
            gameOver = true;
            projectileActive = false;
            updateHighScore();

            if (!defeatSoundPlayed) {
                defeatSoundPlayed = true;
                PlayingSoundManager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1f, vol());
                minigame.getLeaderboard().doPost(score);
            }

            return;
        }

        board[target[0]][target[1]] = currentColor;
        projectileActive = false;

        resolveMatches(target[0], target[1]);

        shots++;

        currentColor = nextColor;
        nextColor = randomColor();

        readyAnimation = 0;
        nextAnimation = 0;

        updateHighScore();

        if (shots >= LAYER_INTERVAL && effects.isEmpty()) {
            shots = 0;
            addLayer();
        }

        spawnProjectile();
    }

    private int[] findAttachment(int hitCol, int hitRow) {
        if (hitRow < 0) {
            int bestCol = -1;
            float best = Float.MAX_VALUE;

            for (int col = 0; col < GRID_COLS; col++) {
                if (board[col][0] != 0) {
                    continue;
                }

                float d = Math.abs(projectileX - getCellCenterX(col));

                if (d < best) {
                    best = d;
                    bestCol = col;
                }
            }

            return bestCol < 0 ? null : new int[]{bestCol, 0};
        }

        if (!isInside(hitCol, hitRow)) {
            return null;
        }

        int bx = -1;
        int by = -1;
        float best = Float.MAX_VALUE;

        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : dirs) {
            int col = hitCol + dir[0];
            int row = hitRow + dir[1];

            if (!isInside(col, row) || board[col][row] != 0) {
                continue;
            }

            float d = distance(projectileX, projectileY, getCellCenterX(col), getCellCenterY(row) + boardOffset);

            if (d < best) {
                best = d;
                bx = col;
                by = row;
            }
        }

        return bx < 0 ? null : new int[]{bx, by};
    }

    private void resolveMatches(int col, int row) {
        if (!isInside(col, row)) {
            return;
        }

        int color = board[col][row];

        if (color == 0) {
            return;
        }

        boolean[][] visited = new boolean[GRID_COLS][GRID_ROWS];
        List<int[]> queue = new ArrayList<>();
        List<int[]> match = new ArrayList<>();

        queue.add(new int[]{col, row});
        visited[col][row] = true;

        for (int i = 0; i < queue.size(); i++) {
            int[] p = queue.get(i);

            int x = p[0];
            int y = p[1];

            if (!isInside(x, y) || board[x][y] != color) {
                continue;
            }

            match.add(p);

            addMatchNeighbor(x - 1, y, color, visited, queue);
            addMatchNeighbor(x + 1, y, color, visited, queue);
            addMatchNeighbor(x, y - 1, color, visited, queue);
            addMatchNeighbor(x, y + 1, color, visited, queue);
        }

        if (match.size() < 3) {
            return;
        }

        for (int i = 0; i < match.size(); i++) {
            int[] p = match.get(i);

            int x = p[0];
            int y = p[1];
            int bubbleColor = board[x][y];

            effects.add(new Effect(getCellCenterX(x), getCellCenterY(y) + boardOffset, bubbleColor, EffectType.EXPLOSION, i * 0.02f));

            board[x][y] = 0;
        }

        score += match.size() * 10;

        PlayingSoundManager.playSound(SoundEvents.BLOCK_DEEPSLATE_BREAK, 1.2f, vol()*1.5f);

        removeFloatingBubbles();
    }

    private void addMatchNeighbor(int x, int y, int color, boolean[][] visited, List<int[]> queue) {
        if (!isInside(x, y)) {
            return;
        }

        if (visited[x][y]) {
            return;
        }

        if (board[x][y] != color) {
            return;
        }

        visited[x][y] = true;
        queue.add(new int[]{x, y});
    }

    private void removeFloatingBubbles() {
        boolean[][] connected = new boolean[GRID_COLS][GRID_ROWS];
        List<int[]> queue = new ArrayList<>();

        for (int col = 0; col < GRID_COLS; col++) {
            if (board[col][0] != 0) {
                connected[col][0] = true;
                queue.add(new int[]{col, 0});
            }
        }

        for (int i = 0; i < queue.size(); i++) {
            int[] p = queue.get(i);

            int x = p[0];
            int y = p[1];

            int[][] dirs = {
                    {-1, 0},
                    {1, 0},
                    {0, -1},
                    {0, 1}
            };

            for (int[] dir : dirs) {
                int nx = x + dir[0];
                int ny = y + dir[1];

                if (!isInside(nx, ny)) {
                    continue;
                }

                if (connected[nx][ny]) {
                    continue;
                }

                if (board[nx][ny] == 0) {
                    continue;
                }

                connected[nx][ny] = true;
                queue.add(new int[]{nx, ny});
            }
        }

        int wave = 0;

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (board[col][row] == 0 || connected[col][row]) {
                    continue;
                }

                int color = board[col][row];

                effects.add(new Effect(getCellCenterX(col), getCellCenterY(row) + boardOffset, color, EffectType.FALL, wave++ * 0.025f));

                board[col][row] = 0;
                score += 20;
            }
        }

        if (wave > 0) {
            PlayingSoundManager.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.6f, vol());
        }
    }

    private void addLayer() {
        for (int row = GRID_ROWS - 1; row > 0; row--) {
            for (int col = 0; col < GRID_COLS; col++) {
                board[col][row] = board[col][row - 1];
            }
        }

        for (int col = 0; col < GRID_COLS; col++) {
            board[col][0] = random.nextFloat() < 0.85f ? randomColor() : 0;
        }

        boardOffset = -CELL_SIZE;
        boardOffsetTarget = 0;

        if (hasReachedLossLine()) {
            gameOver = true;
            updateHighScore();

            if (!defeatSoundPlayed) {
                defeatSoundPlayed = true;
                PlayingSoundManager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1f, vol());
                minigame.getLeaderboard().doPost(score);
            }
        }
    }

    private void drawBoard(DrawContext context) {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (board[col][row] == 0) {
                    continue;
                }

                drawBubble(context, getCellCenterX(col), getCellCenterY(row) + boardOffset, BUBBLE_SIZE, board[col][row], 1f);
            }
        }

        if (projectileActive) {
            drawBubble(context, projectileX, projectileY, BUBBLE_SIZE, currentColor, 1f);
        }
    }

    private void drawEffects(DrawContext context) {
        for (Effect effect : effects) {
            if (effect.delay > 0) {
                continue;
            }

            float p = clamp(effect.progress, 0f, 1f);

            if (effect.type == EffectType.FALL) {
                float y = effect.y + 75f * easeInCubic(p);
                float scale = 1f - p * 0.15f;

                drawBubble(context, effect.x, y, BUBBLE_SIZE, effect.color, scale);
            } else {
                float scale = Math.max(0.02f, 1f - easeInCubic(p));

                drawBubble(context, effect.x, effect.y, BUBBLE_SIZE, effect.color, scale);
            }
        }
    }

    private void drawAimer(DrawContext context) {
        if (projectileActive || gameOver) {
            return;
        }

        float scale = easeOutBack(clamp(readyAnimation, 0f, 1f));

        drawBubble(context, getLauncherX(), getLauncherY(), BUBBLE_SIZE, currentColor, scale);
    }

    private void drawQueuedBubble(DrawContext context) {
        float scale = 0.65f + 0.35f * easeOutBack(
                clamp(nextAnimation, 0f, 1f)
        );

        drawBubble(context, getLauncherX() - BUBBLE_SIZE * 1.8f, getLauncherY() + 2, BUBBLE_SIZE - 2, nextColor, scale);
    }

    private void drawTrajectory(DrawContext context) {
        float x = getLauncherX();
        float y = getLauncherY();

        float dx = (float) Math.sin(angle);
        float dy = -(float) Math.cos(angle);

        float remaining = GAME_HEIGHT * 2f;

        float left = getBoardLeft() + BUBBLE_SIZE * 0.5f;
        float right = getBoardLeft() + BOARD_WIDTH - BUBBLE_SIZE * 0.5f;
        float top = getBoardTop() + BUBBLE_SIZE * 0.5f + boardOffset;

        for (int bounce = 0; bounce < 8 && remaining > 0; bounce++) {
            float wall = Float.MAX_VALUE;

            if (dx < 0) {
                float d = (left - x) / dx;

                if (d > 0.01f) {
                    wall = d;
                }
            } else if (dx > 0) {
                float d = (right - x) / dx;

                if (d > 0.01f) {
                    wall = d;
                }
            }

            float ceiling = Float.MAX_VALUE;

            if (dy < 0) {
                float d = (top - y) / dy;

                if (d > 0.01f) {
                    ceiling = d;
                }
            }

            float bubble = findTrajectoryCollision(x, y, dx, dy, remaining);

            float distance = Math.min(wall, Math.min(ceiling, bubble));

            if (distance == Float.MAX_VALUE) {
                drawTrajectorySegment(context, x, y, dx, dy, remaining);
                return;
            }

            drawTrajectorySegment(context, x, y, dx, dy, distance);

            x += dx * distance;
            y += dy * distance;
            remaining -= distance;

            if (distance == bubble || distance == ceiling) {
                return;
            }

            dx = -dx;
            x += dx * 0.1f;
        }
    }

    private void drawTrajectorySegment(DrawContext context, float x, float y, float dx, float dy, float length) {
        Matrix3x2fStack matrices = context.getMatrices();

        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.rotate((float) Math.atan2(dy, dx));

        for (float i = 0; i < length; i += 7) {
            int start = Math.round(i);
            int end = Math.max(start + 1, Math.round(Math.min(i + 4, length)));

            context.fill(start, 0, end, 1, 0xFFFFFFFF);
        }

        matrices.popMatrix();
    }

    private float findTrajectoryCollision(float x, float y, float dx, float dy, float maxDistance) {
        float best = maxDistance;

        float radius = COLLISION_RADIUS;
        float radiusSq = radius * radius;

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (board[col][row] == 0) {
                    continue;
                }

                float cx = getCellCenterX(col);
                float cy = getCellCenterY(row) + boardOffset;

                float fx = cx - x;
                float fy = cy - y;

                float projection = fx * dx + fy * dy;

                if (projection <= 0) {
                    continue;
                }

                float perpendicular = fx * fx + fy * fy - projection * projection;

                if (perpendicular > radiusSq) {
                    continue;
                }

                float hit = projection - (float) Math.sqrt(Math.max(0, radiusSq - perpendicular));

                if (hit > 0.05f && hit < best) {
                    best = hit;
                }
            }
        }

        return best;
    }

    private void drawLossLine(DrawContext context) {
        int y = Math.round(getLossLineY());

        context.fill(getGameLeft(), y, getGameLeft() + GAME_WIDTH, y + 1, 0xFFFF3030);
    }

    private void drawScore(DrawContext context) {
        Text scoreText = Text.translatable("minigame.bubbleshooter.score").append(""+score);
        Text highText = Text.translatable("minigame.bubbleshooter.highScore").append(""+getHighScore());

        int center = getGameLeft() + GAME_WIDTH / 2;

        context.drawTextWithShadow(textRenderer, scoreText, center - textRenderer.getWidth(scoreText) / 2, getGameTop() - 28-1, 0xFFFFFFFF);

        context.drawTextWithShadow(textRenderer, highText, center - textRenderer.getWidth(highText) / 2, getGameTop() - 16-1, 0xFFFFD95A);
    }

    private void drawGameOver(DrawContext context) {
        Text defeat = Text.translatable("minigame.bubbleshooter.gameOver").copy().styled(s -> s.withBold(true));

        int centerX = getGameLeft() + GAME_WIDTH / 2;
        int centerY = getGameTop() + GAME_HEIGHT / 2;

        Matrix3x2fStack matrices = context.getMatrices();

        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(2f, 2f);

        context.drawTextWithShadow(textRenderer, defeat, -textRenderer.getWidth(defeat) / 2, -textRenderer.fontHeight / 2, 0xFFFF4040);

        matrices.popMatrix();
    }

    private void drawBubble(DrawContext context, float x, float y, int size, int color, float scale) {
        int renderSize = Math.max(1, Math.round(size * scale));

        Matrix3x2fStack matrices = context.getMatrices();

        matrices.pushMatrix();
        matrices.translate(x, y);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, BUBBLE_TEXTURE, -renderSize / 2, -renderSize / 2, 0, 0, renderSize, renderSize, renderSize, renderSize, color);

        matrices.popMatrix();
    }

    private boolean hasReachedLossLine() {
        float line = getLossLineY();

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (board[col][row] != 0 &&
                        getCellCenterY(row) + boardOffset >= line) {
                    return true;
                }
            }
        }

        return false;
    }

    private void updateHighScore() {
        BubbleShooterConfig config = MinigameRegistry.getConfig(BubbleShooterConfig.class);
        AutoConfig.getConfigHolder(BubbleShooterConfig.class).save();

        if (score > config.highScore) {
            config.highScore = score;
        }
    }

    private int getHighScore() {
        return MinigameRegistry.getConfig(BubbleShooterConfig.class).highScore;
    }

    //and this shitty methods...

    private float vol() {
        return PlayingSoundManager.vol(MinigameRegistry.getConfig(BubbleShooterVisibleConfig.class).volume);
    }

    private float getLauncherX() {
        return getGameLeft() + GAME_WIDTH * 0.5f;
    }

    private float getLauncherY() {
        return getGameTop() + BOARD_HEIGHT + LAUNCHER_OFFSET;
    }

    private int getGameLeft() {
        return (width - GUI_WIDTH) / 2 + 8;
    }

    private int getGameTop() {
        return (height - GUI_HEIGHT) / 2 + 8;
    }

    private int getBoardLeft() {
        return getGameLeft() +
                (GAME_WIDTH - BOARD_WIDTH) / 2;
    }

    private int getBoardTop() {
        return getGameTop() + BOARD_TOP_OFFSET;
    }

    private float getLossLineY() {
        return getBoardTop() + BOARD_HEIGHT - LOSS_LINE_OFFSET;
    }

    private float getCellCenterX(int col) {
        return getBoardLeft() + col * CELL_SIZE + CELL_SIZE * 0.5f;
    }

    private float getCellCenterY(int row) {
        return getBoardTop() + row * CELL_SIZE + CELL_SIZE * 0.5f;
    }

    private boolean isInside(int col, int row) {
        return col >= 0 && col < GRID_COLS && row >= 0 && row < GRID_ROWS;
    }

    private float rayHorizontalDistance(float y, float vy, float targetY) {
        if (Math.abs(vy) < 0.0001f) {
            return -1;
        }

        float t = (targetY - y) / vy;

        return t >= 0 ? t : -1;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x2 - x1, y2 - y1);
    }

    private int randomColor() {
        return colors[random.nextInt(colors.length)];
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float easeInCubic(float x) {
        return x * x * x;
    }

    private float easeOutBack(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;

        float n = x - 1f;

        return 1f + c3 * n * n * n + c1 * n * n;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    enum EffectType {
        FALL,
        EXPLOSION
    }
}