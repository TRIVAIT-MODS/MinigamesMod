package org.trivait.minigamesmod.minigame.snake;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonColors;
import org.joml.Matrix3x2fStack;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.ConfigButton;

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
            Identifier.withDefaultNamespace("textures/item/apple.png"),
            Identifier.withDefaultNamespace("textures/item/bread.png"),
            Identifier.withDefaultNamespace("textures/item/carrot.png"),
            Identifier.withDefaultNamespace("textures/item/baked_potato.png"),
            Identifier.withDefaultNamespace("textures/item/cooked_beef.png"),
            Identifier.withDefaultNamespace("textures/item/cooked_chicken.png"),
            Identifier.withDefaultNamespace("textures/item/cooked_porkchop.png"),
            Identifier.withDefaultNamespace("textures/item/cooked_mutton.png"),
            Identifier.withDefaultNamespace("textures/item/cooked_rabbit.png"),
            Identifier.withDefaultNamespace("textures/item/cooked_cod.png"),
            Identifier.withDefaultNamespace("textures/item/cooked_salmon.png"),
            Identifier.withDefaultNamespace("textures/item/beetroot.png"),
            Identifier.withDefaultNamespace("textures/item/melon_slice.png"),
            Identifier.withDefaultNamespace("textures/item/pumpkin_pie.png"),
            Identifier.withDefaultNamespace("textures/item/cookie.png"),
            Identifier.withDefaultNamespace("textures/item/suspicious_stew.png"),
            Identifier.withDefaultNamespace("textures/item/rabbit_stew.png"),
            Identifier.withDefaultNamespace("textures/item/mushroom_stew.png"),
            Identifier.withDefaultNamespace("textures/item/beetroot_soup.png")
    };

    private Identifier currentFoodTexture = Identifier.withDefaultNamespace("textures/item/apple.png");

    public SnakeScreen(Snake minigame, Screen parent) {
        super(Component.literal("Snake"));
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
            currentFoodTexture = Identifier.withDefaultNamespace("textures/item/apple.png");
        }
    }

    @Override
    protected void init() {
        Button returnButton = SpriteIconButton.builder(Component.empty(), button -> this.onClose(), true)
                .sprite(Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "icon/return"), 15, 15).build();
        returnButton.setTooltip(Tooltip.create(Component.translatable("minigame.tetris.return")));
        returnButton.setRectangle(20, 20, 10, 10);

        Button restartButton = SpriteIconButton.builder(Component.empty(), button -> initGame(), true)
                .sprite(Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "icon/restart"), 15, 15).build();
        restartButton.setTooltip(Tooltip.create(Component.translatable("minigame.tetris.restart")));
        restartButton.setRectangle(20, 20, 35, 10);

        Button pauseButton = SpriteIconButton.builder(Component.empty(), button -> gamePaused = !gamePaused, true)
                .sprite(Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "icon/pause"), 15, 15).build();
        pauseButton.setTooltip(Tooltip.create(Component.translatable("minigame.tetris.pause")));
        pauseButton.setRectangle(20, 20, 60, 10);

        this.addRenderableWidget(returnButton);
        this.addRenderableWidget(restartButton);
        this.addRenderableWidget(pauseButton);
        this.addRenderableWidget(new ConfigButton(85, 10, minigame));
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
            PlayingSoundManager.playSound(SoundEvents.PLAYER_BURP, 1, vol());
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void endGame() {
        gameOver = true;
        minigame.getLeaderboard().doPost(Minecraft.getInstance().getUser().getName(), score, true);
        SnakeConfig config = MinigameRegistry.getConfig(SnakeConfig.class);
        if (score > config.snakeHighScore) {
            config.snakeHighScore = score;
            AutoConfig.getConfigHolder(SnakeConfig.class).save();
        }
        PlayingSoundManager.playSound(SoundEvents.VILLAGER_NO, 1, vol());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        Matrix3x2fStack matrices = context.pose();

        gameX = (this.width - GAME_WIDTH) / 2;
        gameY = (this.height - GAME_HEIGHT) / 2;

        context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/snake/gui.png"), gameX-8, gameY-8, 0, 0, GAME_WIDTH+16, GAME_HEIGHT+16, GAME_WIDTH+16, GAME_HEIGHT+16);

        context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/snake/snake_cell.png"), gameX, gameY, 0, 0, GAME_WIDTH, GAME_HEIGHT, 16, 16);

        for (int i = 0; i < snake.size(); i++) {
            renderSnakeSegment(context, matrices, gameX, gameY, i);
        }

        int foodX = gameX + food[0] * CELL_SIZE;
        int foodY = gameY + food[1] * CELL_SIZE;

        context.blit(RenderPipelines.GUI_TEXTURED, currentFoodTexture, foodX, foodY, 0, 0, CELL_SIZE, CELL_SIZE, CELL_SIZE, CELL_SIZE);

        int textY = gameY - 20;
        int textX = gameX;

        context.text(this.font, Component.translatable("minigame.snake.score").append("" + score), textX, textY, CommonColors.WHITE);

        textX = textX+ font.width(Component.translatable("minigame.snake.score").append("" + score).getString()+3);

        SnakeConfig config = MinigameRegistry.getConfig(SnakeConfig.class);
        context.text(this.font, Component.translatable("minigame.snake.best_score").append("" + config.snakeHighScore), textX, textY, CommonColors.WHITE);

        if (gameOver) {
            int centerX = gameX + GAME_WIDTH / 2;
            int centerY = gameY + GAME_HEIGHT / 2;

            matrices.pushMatrix();
            matrices.translate(centerX, centerY - 10);
            matrices.scale(1.5f, 1.5f);
            context.centeredText(this.font, Component.literal("§lGAME OVER"), 0, 0, CommonColors.RED);
            matrices.popMatrix();

            context.centeredText(this.font, Component.literal("Score: " + score), centerX, centerY + 12, CommonColors.WHITE);
        }

        if (gamePaused) {
            int centerX = gameX + GAME_WIDTH / 2;
            int centerY = gameY + GAME_HEIGHT / 2;
            context.centeredText(this.font, Component.literal("PAUSED"), centerX, centerY - 5, CommonColors.YELLOW);
        }
    }

    private void renderSnakeSegment(GuiGraphicsExtractor context, Matrix3x2fStack matrices, int gameX, int gameY, int segmentIndex) {
        int[] segment = snake.get(segmentIndex);
        int x = gameX + segment[0] * CELL_SIZE;
        int y = gameY + segment[1] * CELL_SIZE;

        if (segmentIndex == 0) {
            int headRotation = getHeadRotation();
            drawRotatedTexture(context, matrices, x, y,
                Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/snake/snake3.png"), headRotation);
        } else if (segmentIndex == snake.size() - 1) {
            int tailRotation = getTailRotation();
            drawRotatedTexture(context, matrices, x, y,
                Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/snake/snake2.png"), tailRotation);
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
                    Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/snake/snake1.png"), bodyRotation);
            } else {
                int turnRotation = getTurnRotation(prevDx, prevDy, nextDx, nextDy);
                drawRotatedTexture(context, matrices, x, y,
                    Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/snake/snake4.png"), turnRotation);
            }
        }
    }

    private void drawRotatedTexture(GuiGraphicsExtractor context, Matrix3x2fStack matrices, int x, int y, Identifier texture, int rotation) {
        matrices.pushMatrix();
        matrices.translate((float) (x + CELL_SIZE / 2.0), (float) (y + CELL_SIZE / 2.0));
        matrices.rotate((float) Math.toRadians(rotation));
        matrices.translate((float) (-CELL_SIZE / 2.0), (float) (-CELL_SIZE / 2.0));
        context.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0, 0, CELL_SIZE, CELL_SIZE, CELL_SIZE, CELL_SIZE);
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
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == 256 && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }

        if (gameOver) {
            initGame();
            return true;
        }

        switch (input.key()) {
            case 265, 87 -> {
                if (direction[1] != 1 && nextDirection[1] != 1) {
                    nextDirection[0] = 0;
                    nextDirection[1] = -1;
                }
                PlayingSoundManager.playSound(SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("block.wooden_button.click_on")), 2.0F, vol());
                return true;
            }
            case 264, 83 -> {
                if (direction[1] != -1 && nextDirection[1] != -1) {
                    nextDirection[0] = 0;
                    nextDirection[1] = 1;
                }
                PlayingSoundManager.playSound(SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("block.wooden_button.click_on")), 2.0F, vol());
                return true;
            }
            case 263, 65 -> {
                if (direction[0] != 1 && nextDirection[0] != 1) {
                    nextDirection[0] = -1;
                    nextDirection[1] = 0;
                }
                PlayingSoundManager.playSound(SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("block.wooden_button.click_on")), 2.0F, vol());
                return true;
            }
            case 262, 68 -> {
                if (direction[0] != -1 && nextDirection[0] != -1) {
                    nextDirection[0] = 1;
                    nextDirection[1] = 0;
                }
                PlayingSoundManager.playSound(SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("block.wooden_button.click_on")), 2.0F, vol());
                return true;
            }
            case 32 -> {
                gamePaused = !gamePaused;
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (gameOver) {
            initGame();
            return false;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private float vol() {
        return 5*MinigameRegistry.getConfig(SnakeVisibleConfig.class).volume / 100f;
    }
}
