package org.trivait.minigamesmod.minigame.snake;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.ConfigButton;
import org.trivait.minigamesmod.minigame.dino.GoogleDino;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeScreen extends Screen {
    static final int GRID_WIDTH = 20;
    static final int GRID_HEIGHT = 15;
    static final int CELL_SIZE = 16;
    static final int GAME_WIDTH = GRID_WIDTH * CELL_SIZE;
    static final int GAME_HEIGHT = GRID_HEIGHT * CELL_SIZE;

    private final Screen parent;
    private final Snake minigame;
    private int gameX;
    private int gameY;

    private List<int[]> snake;
    private int[] food;
    private int[] direction;
    private int[] nextDirection;
    private int score;
    private boolean gameOver;
    private boolean gamePaused;
    private int updateCounter;
    private static final int UPDATE_INTERVAL = 6;
    private Random random;

    private static final Identifier[] FOODS = {
            Identifier.ofVanilla("textures/item/apple.png"),
            Identifier.ofVanilla("textures/item/bread.png"),
            Identifier.ofVanilla("textures/item/carrot.png"),
            Identifier.ofVanilla("textures/item/baked_potato.png"),
            Identifier.ofVanilla("textures/item/cooked_beef.png"),
            Identifier.ofVanilla("textures/item/cooked_chicken.png"),
            Identifier.ofVanilla("textures/item/cooked_porkchop.png"),
            Identifier.ofVanilla("textures/item/cooked_mutton.png"),
            Identifier.ofVanilla("textures/item/cooked_rabbit.png"),
            Identifier.ofVanilla("textures/item/cooked_cod.png"),
            Identifier.ofVanilla("textures/item/cooked_salmon.png"),
            Identifier.ofVanilla("textures/item/beetroot.png"),
            Identifier.ofVanilla("textures/item/melon_slice.png"),
            Identifier.ofVanilla("textures/item/pumpkin_pie.png"),
            Identifier.ofVanilla("textures/item/cookie.png"),
            Identifier.ofVanilla("textures/item/suspicious_stew.png"),
            Identifier.ofVanilla("textures/item/rabbit_stew.png"),
            Identifier.ofVanilla("textures/item/mushroom_stew.png"),
            Identifier.ofVanilla("textures/item/beetroot_soup.png")
    };

    private Identifier currentFoodTexture = Identifier.ofVanilla("textures/item/apple.png");

    public SnakeScreen(Snake minigame, Screen parent) {
        super(Text.literal("Snake"));
        this.minigame = minigame;
        this.parent = parent;
        this.random = new Random();
        initGame();
    }

    private void initGame() {
        snake = new ArrayList<>();
        int startX = GRID_WIDTH / 2;
        int startY = GRID_HEIGHT / 2;

        snake.add(new int[]{startX, startY});
        snake.add(new int[]{startX - 1, startY});
        snake.add(new int[]{startX - 2, startY});
        snake.add(new int[]{startX - 3, startY});

        direction = new int[]{1, 0};
        nextDirection = new int[]{1, 0};

        spawnFood();

        score = 0;
        gameOver = false;
        gamePaused = false;
        updateCounter = 0;
    }

    private void spawnFood() {
        boolean validPosition = false;

        while (!validPosition) {
            food = new int[]{random.nextInt(GRID_WIDTH), random.nextInt(GRID_HEIGHT)};
            validPosition = true;

            for (int[] segment : snake) {
                if (segment[0] == food[0] && segment[1] == food[1]) {
                    validPosition = false;
                    break;
                }
            }
        }

        SnakeVisibleConfig config = MinigameRegistry.getConfig(SnakeVisibleConfig.class);

        if (config.randomFood) {
            currentFoodTexture = FOODS[random.nextInt(FOODS.length)];
        } else {
            currentFoodTexture = Identifier.ofVanilla("textures/item/apple.png");
        }
    }

    @Override
    protected void init() {
        ButtonWidget returnButton = TextIconButtonWidget.builder(Text.empty(), button -> this.close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15).build();
        returnButton.setTooltip(Tooltip.of(Text.translatable("minigame.tetris.return")));
        returnButton.setDimensionsAndPosition(20, 20, 10, 10);

        ButtonWidget restartButton = TextIconButtonWidget.builder(Text.empty(), button -> initGame(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15).build();
        restartButton.setTooltip(Tooltip.of(Text.translatable("minigame.tetris.restart")));
        restartButton.setDimensionsAndPosition(20, 20, 35, 10);

        ButtonWidget pauseButton = TextIconButtonWidget.builder(Text.empty(), button -> gamePaused = !gamePaused, true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/pause"), 15, 15).build();
        pauseButton.setTooltip(Tooltip.of(Text.translatable("minigame.tetris.pause")));
        pauseButton.setDimensionsAndPosition(20, 20, 60, 10);

        this.addDrawableChild(returnButton);
        this.addDrawableChild(restartButton);
        this.addDrawableChild(pauseButton);
        this.addDrawableChild(new ConfigButton(85, 10, minigame));
    }

    @Override
    public void tick() {
        if (gameOver || gamePaused) return;

        updateCounter++;
        if (updateCounter < UPDATE_INTERVAL) return;
        updateCounter = 0;

        direction[0] = nextDirection[0];
        direction[1] = nextDirection[1];

        int[] head = snake.get(0);
        int newX = head[0] + direction[0];
        int newY = head[1] + direction[1];

        if (newX < 0 || newX >= GRID_WIDTH || newY < 0 || newY >= GRID_HEIGHT) {
            endGame();
            return;
        }

        boolean grow = newX == food[0] && newY == food[1];

        int checkSize = grow ? snake.size() : snake.size() - 1;
        for (int i = 0; i < checkSize; i++) {
            int[] segment = snake.get(i);
            if (segment[0] == newX && segment[1] == newY) {
                endGame();
                return;
            }
        }

        snake.add(0, new int[]{newX, newY});

        if (grow) {
            score++;
            PlayingSoundManager.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1, vol());
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void endGame() {
        gameOver = true;
        minigame.getLeaderboard().doPost(MinecraftClient.getInstance().getSession().getUsername(), score, true);
        SnakeConfig config = MinigameRegistry.getConfig(SnakeConfig.class);
        if (score > config.snakeHighScore) {
            config.snakeHighScore = score;
            AutoConfig.getConfigHolder(SnakeConfig.class).save();
        }
        PlayingSoundManager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1, vol());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        Matrix3x2fStack matrices = context.getMatrices();

        gameX = (this.width - GAME_WIDTH) / 2;
        gameY = (this.height - GAME_HEIGHT) / 2;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/snake/gui.png"), gameX-8, gameY-8, 0, 0, GAME_WIDTH+16, GAME_HEIGHT+16, GAME_WIDTH+16, GAME_HEIGHT+16);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/snake/snake_cell.png"), gameX, gameY, 0, 0, GAME_WIDTH, GAME_HEIGHT, 16, 16);

        for (int i = 0; i < snake.size(); i++) {
            renderSnakeSegment(context, matrices, gameX, gameY, i);
        }

        int foodX = gameX + food[0] * CELL_SIZE;
        int foodY = gameY + food[1] * CELL_SIZE;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, currentFoodTexture, foodX, foodY, 0, 0, CELL_SIZE, CELL_SIZE, CELL_SIZE, CELL_SIZE);

        int textY = gameY - 20;
        int textX = gameX;

        context.drawTextWithShadow(this.textRenderer, Text.translatable("minigame.snake.score").append("" + score), textX, textY, Colors.WHITE);

        textX = textX+textRenderer.getWidth(Text.translatable("minigame.snake.score").append("" + score).getString()+3);

        SnakeConfig config = MinigameRegistry.getConfig(SnakeConfig.class);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("minigame.snake.best_score").append("" + config.snakeHighScore), textX, textY, Colors.WHITE);

        if (gameOver) {
            int centerX = gameX + GAME_WIDTH / 2;
            int centerY = gameY + GAME_HEIGHT / 2;

            matrices.pushMatrix();
            matrices.translate(centerX, centerY - 10);
            matrices.scale(1.5f, 1.5f);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§lGAME OVER"), 0, 0, Colors.RED);
            matrices.popMatrix();

            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Score: " + score), centerX, centerY + 12, Colors.WHITE);
        }

        if (gamePaused) {
            int centerX = gameX + GAME_WIDTH / 2;
            int centerY = gameY + GAME_HEIGHT / 2;
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("PAUSED"), centerX, centerY - 5, Colors.YELLOW);
        }
    }

    private void renderSnakeSegment(DrawContext context, Matrix3x2fStack matrices, int gameX, int gameY, int segmentIndex) {
        int[] segment = snake.get(segmentIndex);
        int x = gameX + segment[0] * CELL_SIZE;
        int y = gameY + segment[1] * CELL_SIZE;

        if (segmentIndex == 0) {
            int headRotation = getHeadRotation();
            drawRotatedTexture(context, matrices, x, y,
                Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/snake/snake3.png"), headRotation);
        } else if (segmentIndex == snake.size() - 1) {
            int tailRotation = getTailRotation();
            drawRotatedTexture(context, matrices, x, y,
                Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/snake/snake2.png"), tailRotation);
        } else {
            int[] prevSegment = snake.get(segmentIndex - 1);
            int[] nextSegment = snake.get(segmentIndex + 1);

            int prevDx = segment[0] - prevSegment[0];
            int prevDy = segment[1] - prevSegment[1];
            int nextDx = nextSegment[0] - segment[0];
            int nextDy = nextSegment[1] - segment[1];

            if (isstraight(prevDx, prevDy, nextDx, nextDy)) {
                int bodyRotation = getBodyRotation(prevDx, prevDy, nextDx, nextDy);
                drawRotatedTexture(context, matrices, x, y,
                    Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/snake/snake1.png"), bodyRotation);
            } else {
                int turnRotation = getTurnRotation(prevDx, prevDy, nextDx, nextDy);
                drawRotatedTexture(context, matrices, x, y,
                    Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/snake/snake4.png"), turnRotation);
            }
        }
    }

    private void drawRotatedTexture(DrawContext context, Matrix3x2fStack matrices, int x, int y, Identifier texture, int rotation) {
        matrices.pushMatrix();
        matrices.translate((float) (x + CELL_SIZE / 2.0), (float) (y + CELL_SIZE / 2.0));
        matrices.rotate(rotation);
        matrices.translate((float) (-CELL_SIZE / 2.0), (float) (-CELL_SIZE / 2.0));
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0, 0, CELL_SIZE, CELL_SIZE, CELL_SIZE, CELL_SIZE);
        matrices.popMatrix();
    }

    private int getHeadRotation() {
        if (direction[0] == 1) return 270;
        if (direction[0] == -1) return 90;
        if (direction[1] == 1) return 0;
        if (direction[1] == -1) return 180;
        return 0;
    }

    private int getTailRotation() {
        int[] segment = snake.get(snake.size() - 1);
        int[] prevSegment = snake.get(snake.size() - 2);

        int dx = prevSegment[0] - segment[0];
        int dy = prevSegment[1] - segment[1];

        if (dx == 1) return 90;
        if (dx == -1) return 270;
        if (dy == 1) return 180;
        if (dy == -1) return 0;
        return 0;
    }

    private boolean isstraight(int prevDx, int prevDy, int nextDx, int nextDy) {
        return (prevDx == nextDx && prevDy == nextDy) || (prevDx == -nextDx && prevDy == -nextDy);
    }

    private int getBodyRotation(int prevDx, int prevDy, int nextDx, int nextDy) {
        if (prevDx == 1 || nextDx == 1) return 90;
        if (prevDx == -1 || nextDx == -1) return 270;
        if (prevDy == 1 || nextDy == 1) return 180;
        if (prevDy == -1 || nextDy == -1) return 0;
        return 0;
    }

    private int getTurnRotation(int prevDx, int prevDy, int nextDx, int nextDy) {
        if (prevDx == 1 && nextDy == 1) return 0;
        if (prevDx == -1 && nextDy == -1) return 180;
        if (prevDx == 1 && nextDy == -1) return 90;
        if (prevDx == -1 && nextDy == 1) return 270;
        if (prevDy == -1 && nextDx == 1) return 270;
        if (prevDy == 1 && nextDx == -1) return 90;
        if (prevDy == -1 && nextDx == -1) return 0;
        if (prevDy == 1 && nextDx == 1) return 180;
        return 0;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && this.shouldCloseOnEsc()) {
            this.close();
            return true;
        }

        if (gameOver) {
            initGame();
            return true;
        }

        switch (keyCode) {
            case 265, 87 -> {
                if (direction[1] != 1 && nextDirection[1] != 1) {
                    nextDirection[0] = 0;
                    nextDirection[1] = -1;
                }
                PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol());
                return true;
            }
            case 264, 83 -> {
                if (direction[1] != -1 && nextDirection[1] != -1) {
                    nextDirection[0] = 0;
                    nextDirection[1] = 1;
                }
                PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol());
                return true;
            }
            case 263, 65 -> {
                if (direction[0] != 1 && nextDirection[0] != 1) {
                    nextDirection[0] = -1;
                    nextDirection[1] = 0;
                }
                PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol());
                return true;
            }
            case 262, 68 -> {
                if (direction[0] != -1 && nextDirection[0] != -1) {
                    nextDirection[0] = 1;
                    nextDirection[1] = 0;
                }
                PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol());
                return true;
            }
            case 32 -> {
                gamePaused = !gamePaused;
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (gameOver) {
            initGame();
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    private float vol() {
        return 5*MinigameRegistry.getConfig(SnakeVisibleConfig.class).volume / 100f;
    }
}
