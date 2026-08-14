package org.trivait.minigamesmod.minigame.tetris;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.ConfigButton;
import org.trivait.minigamesmod.minigame.game2048.Game2048VisibleConfig;
import org.trivait.minigamesmod.minigame.tetris.mino.*;

import java.util.ArrayList;
import java.util.Random;

public class TetrisScreen extends Screen {
    public static int dropInterval = 60;
    static final int GRID_X = 10;
    static final int GRID_Y = 16;
    public static final int TETRIS_WIDTH = Block.SIZE * GRID_X;
    public static final int TETRIS_HEIGHT = Block.SIZE * GRID_Y;
    int levelLength = 5;
    public static final int NEXT_WIDTH = Block.SIZE * 4;
    public static final int NEXT_HEIGHT = Block.SIZE * 5;

    public static int leftX;
    public static int rightX;
    public static int topY;
    public static int bottomY;

    // aliases for Block.draw compatibility
    public static int left_x;
    public static int top_y;

    public static boolean upPressed, downPressed, leftPressed, rightPressed, spacePressed, paused, active = false;
    public static int hardDrop;

    public static ArrayList<Block> staticBlocks = new ArrayList<>();
    public static ArrayList<Block> destroying = new ArrayList<>();
    public static ArrayList<Animation> animations = new ArrayList<>();

    public static int score = 0;
    public static int linesCleared = 0;
    public static int level = 0;
    public static int combo = 0;

    public static boolean isNewHighScore = false;
    public static Text onScreenText;
    public static int onScreenTextColour;
    public static int onScreenTextOpacity = 0;
    public static float animation = 0;

    // kept for Block.draw compat (WIDTH/HEIGHT)
    public static final int WIDTH = TETRIS_WIDTH;
    public static final int HEIGHT = TETRIS_HEIGHT;

    public final Screen parent;
    private final Tetris minigame;

    public static Mino currentMino;
    public static Mino nextMino;

    ButtonWidget playButton = ButtonWidget.builder(
            Text.translatable("minigame.tetris.start").withColor(Colors.YELLOW),
            button -> reset()).build();

    public TetrisScreen(Tetris minigame, Screen parent) {
        super(Text.translatable("minigame.tetris.title"));
        this.minigame = minigame;
        this.parent = parent;
    }

    private float vol() {
        return PlayingSoundManager.vol(MinigameRegistry.getConfig(TetrisVisibleConfig.class).volume);
    }

    @Override
    protected void init() {
        leftX = this.width / 2 - TETRIS_WIDTH / 2;
        rightX = leftX + TETRIS_WIDTH;
        topY = this.height / 2 - TETRIS_HEIGHT / 2;
        bottomY = topY + TETRIS_HEIGHT;
        left_x = leftX;
        top_y = topY;

        paused = true;
        hardDrop = MinigameRegistry.getConfig(TetrisVisibleConfig.class).hardDrop.mode;

        ButtonWidget returnButton = TextIconButtonWidget.builder(Text.empty(), button -> this.close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15).build();
        returnButton.setTooltip(Tooltip.of(Text.translatable("minigame.tetris.return")));
        returnButton.setDimensionsAndPosition(20, 20, 10, 10);

        ButtonWidget restartButton = TextIconButtonWidget.builder(Text.empty(), button -> gameOver(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15).build();
        restartButton.setTooltip(Tooltip.of(Text.translatable("minigame.tetris.restart")));
        restartButton.setDimensionsAndPosition(20, 20, 35, 10);

        ButtonWidget pauseButton = TextIconButtonWidget.builder(Text.empty(), button -> paused = !paused, true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/pause"), 15, 15).build();
        pauseButton.setTooltip(Tooltip.of(Text.translatable("minigame.tetris.pause")));
        pauseButton.setDimensionsAndPosition(20, 20, 60, 10);

        playButton.setPosition(this.width / 2 - 150 / 2, this.height / 2 - 20 / 2);
        playButton.setDimensions(140, 20);

        this.addDrawableChild(returnButton);
        this.addDrawableChild(restartButton);
        this.addDrawableChild(pauseButton);
        this.addDrawableChild(new ConfigButton(85, 10, minigame));
        this.addDrawableChild(playButton);
    }

    public void reset() {
        score = 0;
        linesCleared = 0;
        level = 0;
        combo = 0;
        dropInterval = 60;
        onScreenTextOpacity = 60;
        onScreenTextColour = 0;
        onScreenText = Text.empty();
        animation = (animation % 30) * 10;
        isNewHighScore = false;
        paused = false;
        active = true;
        staticBlocks = new ArrayList<>();
        destroying = new ArrayList<>();
        animations = new ArrayList<>();
        currentMino = pickMino();
        currentMino.setXY(TETRIS_WIDTH / 2, Block.SIZE);
        leftPressed = rightPressed = upPressed = downPressed = spacePressed = false;
        nextMino = pickMino();
        nextMino.setXY(TETRIS_WIDTH + Block.SIZE * 2 +
                (nextMino instanceof Mino_L2 || nextMino instanceof Mino_Z1 ? Block.SIZE :
                (nextMino instanceof Mino_T ? Block.SIZE / 2 : 0)),
                TETRIS_HEIGHT - (int)(Block.SIZE * 2.5f));
    }

    public void manager() {
        if (currentMino == null) reset();
        if (!currentMino.active) {
            PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("block.stone.place")), 1.5F, vol());
            score += 10;
            staticBlocks.add(currentMino.b[0]);
            staticBlocks.add(currentMino.b[1]);
            staticBlocks.add(currentMino.b[2]);
            staticBlocks.add(currentMino.b[3]);

            int lines = 0;
            if (checkClear(currentMino.b[0].y)) lines++;
            if (checkClear(currentMino.b[1].y)) lines++;
            if (checkClear(currentMino.b[2].y)) lines++;
            if (checkClear(currentMino.b[3].y)) lines++;

            int height = 108;
            int width = 192;
            if (lines > 0) {
                combo++;
                if (combo > 1) {
                    onScreenText = Text.translatable("minigame.tetris.combo").append(" x" + combo);
                    onScreenTextOpacity = 30;
                    onScreenTextColour = combo > 3 ? Colors.RED : combo > 2 ? Colors.YELLOW : Colors.WHITE;
                    score += 50 * (combo - 1);
                }
            } else combo = 0;

            switch (lines) {
                case 1: score += 100; break;
                case 2: score += 300; break;
                case 3: score += 500; break;
                case 4:
                    score += 800;
                    PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("entity.generic.explode")), 0.8F, vol());
                    animations.add(new Animation(this.width / 2 - width / 2, currentMino.b[2].y, width, height, "explosion", 20));
                    onScreenText = Text.translatable("minigame.tetris.tetris");
                    onScreenTextColour = 11141290;
                    onScreenTextOpacity = 30;
                    break;
            }
            switch (level) {
                case 1: dropInterval = 54; break;
                case 2: dropInterval = 48; break;
                case 3: dropInterval = 41; break;
                case 4: dropInterval = 35; break;
                case 5: dropInterval = 29; break;
                case 6: dropInterval = 22; break;
                case 7: dropInterval = 16; break;
                case 8: dropInterval = 10; break;
                case 9: dropInterval = 8;  break;
                case 10: dropInterval = 6; break;
            }

            for (Block b : currentMino.b) {
                if (b.y <= Block.SIZE * 2) { gameOver(); return; }
            }

            currentMino = nextMino;
            currentMino.setXY(TETRIS_WIDTH / 2, Block.SIZE);
            nextMino = pickMino();
            nextMino.setXY(TETRIS_WIDTH + Block.SIZE * 2 +
                    (nextMino instanceof Mino_L2 || nextMino instanceof Mino_Z1 ? Block.SIZE :
                    (nextMino instanceof Mino_T ? Block.SIZE / 2 : 0)),
                    TETRIS_HEIGHT - (int)(Block.SIZE * 2.5f));
        }
        float frameDuration = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
        currentMino.update(frameDuration * 3);
        animation += frameDuration * 3;
    }

    private void gameOver() {
        PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("entity.pig.ambient")), 1.0F, vol());
        TetrisConfig cfg = MinigameRegistry.getConfig(TetrisConfig.class);
        isNewHighScore = score > cfg.tetrisHighScore;
        active = false;
        minigame.onLose();
        if (score!=0) {
            minigame.getLeaderboard().doPost(MinecraftClient.getInstance().getSession().getUsername(), score);
        }
    }

    private boolean checkClear(int y) {
        int count = 0;
        for (Block block : staticBlocks) if (block.y == y) count++;
        if (count < GRID_X) return false;
        PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("block.deepslate.break")), 1.0F, vol());
        linesCleared++;
        for (Block block : staticBlocks) if (block.y == y) destroying.add(block);
        staticBlocks.removeIf(b -> b.y == y);
        for (Block block : staticBlocks) if (block.y < y) block.y += Block.SIZE;
        if (linesCleared % levelLength == 0) level++;
        return true;
    }

    private Mino pickMino() {
        Mino mino = null;
        int i = new Random().nextInt(7);
        mino = switch (i) {
            case 0 -> new Mino_L1();
            case 1 -> new Mino_L2();
            case 2 -> new Mino_Square();
            case 3 -> new Mino_Bar();
            case 4 -> new Mino_T();
            case 5 -> new Mino_Z1();
            case 6 -> new Mino_Z2();
            default -> mino;
        };
        return mino;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (active) super.render(context, mouseX, mouseY, delta);

        if (!paused && active) manager();
        if (paused) upPressed = downPressed = leftPressed = rightPressed = spacePressed = false;

        // draw border
        context.drawHorizontalLine(leftX - 1, rightX, topY - 1 + Block.SIZE * 3, 0x4CFF0000);
        context.drawBorder(leftX - 1, topY - 1, TETRIS_WIDTH + 2, TETRIS_HEIGHT + 2, Colors.WHITE);

        // draw moving mino
        if (currentMino != null) {
            currentMino.draw(context);
            if (hardDrop > 0) currentMino.drawHardDrop(context);
        }

        // draw next mino
        context.drawBorder(rightX + Block.SIZE - 1, bottomY - NEXT_HEIGHT + 1, NEXT_WIDTH + 2, NEXT_HEIGHT, Colors.WHITE);
        context.drawText(this.textRenderer, Text.translatable("minigame.tetris.next"),
                rightX + Block.SIZE * 2, bottomY - NEXT_HEIGHT + Block.SIZE / 2, Colors.WHITE, true);
        if (nextMino != null) nextMino.draw(context);

        // draw score
        context.drawText(this.textRenderer, Text.translatable("minigame.tetris.score").append(": " + score),
                rightX + Block.SIZE * 2, topY + Block.SIZE, Colors.WHITE, true);
        context.drawText(this.textRenderer, Text.translatable("minigame.tetris.lines").append(": " + linesCleared),
                rightX + Block.SIZE * 2, topY + Block.SIZE + 10, Colors.WHITE, true);

        // draw static minos
        for (Block block : staticBlocks) block.draw(context);

        // draw destroying minos
        for (Block d : destroying) {
            d.destroying += MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true) * 3;
            if (d.destroying > 9) d.destroying = 9;
            d.draw(context);
        }
        destroying.removeIf(d -> d.destroying >= 9);

        // draw explosions
        for (Animation a : animations) {
            if (a.animation.equals("explosion"))
                a.draw(context, this.width / 2 - a.width / 2, topY + a.y - a.height / 2);
            else a.draw(context);
            a.frame += 1f;
        }
        animations.removeIf(an -> an.frame > an.frames);

        // draw paused text
        Text pausedText = Text.translatable("minigame.tetris.paused");
        if (paused && active)
            context.drawText(this.textRenderer, pausedText,
                    this.width / 2 - (3 * pausedText.getString().length()), this.height / 2 - 7, Colors.WHITE, true);

        // draw combo/tetris texts
        if (onScreenTextOpacity > 0) {
            int base = onScreenTextColour;
            float alpha = Math.clamp(onScreenTextOpacity / 10f, 0f, 1f);
            int r = (base >> 16) & 0xFF, g = (base >> 8) & 0xFF, b = base & 0xFF;
            int color = ((int)(alpha * 255) << 24) | (r << 16) | (g << 8) | b;
            context.drawText(this.textRenderer, onScreenText,
                    this.width / 2 - onScreenText.getString().length(), this.height / 2, color, true);
            onScreenTextOpacity--;
        }

        // draw credits
        context.drawText(this.textRenderer, Text.literal("Tetris by Nukebomb"), leftX, bottomY + 4, 0xFFAAAAAA, true);

        // draw play button if not active
        playButton.visible = !active;
        if (!active) {
            super.render(context, mouseX, mouseY, delta);
            if (currentMino != null) {
                Text finalScoreText = Text.translatable("minigame.tetris.score").append(": " + score).withColor(Colors.LIGHT_YELLOW);
                context.drawText(this.textRenderer, finalScoreText,
                        this.width / 2 - (finalScoreText.getString().length() * 3), this.height / 2 - 35, Colors.WHITE, true);
                Text linesClearedText = Text.translatable("minigame.tetris.lines").append(": " + linesCleared).withColor(Colors.LIGHT_YELLOW);
                context.drawText(this.textRenderer, linesClearedText,
                        this.width / 2 - (linesClearedText.getString().length() * 3), this.height / 2 - 25, Colors.WHITE, true);

                TetrisConfig cfg = MinigameRegistry.getConfig(TetrisConfig.class);
                if (score > cfg.tetrisHighScore) {
                    cfg.tetrisHighScore = score;
                    AutoConfig.getConfigHolder(TetrisConfig.class).save();
                }
                Text highScoreClearedText = Text.translatable(
                        isNewHighScore ? "minigame.tetris.new_high_score" : "minigame.tetris.high_score")
                        .append(": " + cfg.tetrisHighScore).withColor(Colors.YELLOW);
                if (cfg.tetrisHighScore > 0)
                    context.drawText(this.textRenderer, highScoreClearedText,
                            this.width / 2 - (highScoreClearedText.getString().length() * 3), this.height / 2 + 25, Colors.WHITE, true);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && this.shouldCloseOnEsc()) { this.close(); return true; }
        switch (keyCode) {
            case 262, 68: rightPressed = true; break;
            case 263, 65: leftPressed = true; break;
            case 264, 83: downPressed = true; break;
            case 265, 87: upPressed = true; break;
            case 32: spacePressed = true; break;
            default: return false;
        }
        return false;
    }

    @Override
    public void close() {
        minigame.onStop();
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
