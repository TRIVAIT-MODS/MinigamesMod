package org.trivait.minigamesmod.minigame.tetris;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.gui.widget.ConfigButton;
import org.trivait.minigamesmod.minigame.tetris.mino.Block;
import org.trivait.minigamesmod.minigame.tetris.mino.Mino;
import org.trivait.minigamesmod.minigame.tetris.mino.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class TetrisScreen extends Screen {
    public static int dropInterval = 60;
    static final int gridX = 10;
    static final int gridY = 16;
    public static final int WIDTH = Block.SIZE * gridX;
    public static final int HEIGHT = Block.SIZE * gridY;

    int levelLength = 5;

    public static final int nextWIDTH = Block.SIZE * 4;
    public static final int nextHEIGHT = Block.SIZE * 5;

    public static int leftX;
    public static int rightX;
    public static int topY;
    public static int bottomY;

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

    public final Screen parent;

    public static Mino currentMino;
    public static Mino nextMino;

    private Tetris tetris;

    public TetrisScreen(Screen parent, Tetris tetris) {
        super(Text.of("Tetris Screen"));
        this.parent = parent;
        this.tetris = tetris;
        this.init();
    }

    ButtonWidget playButton = ButtonWidget.builder(Text.translatable("minigame.tetris.start").withColor(Colors.YELLOW), button -> reset()).build();

    @Override
    protected void init() {
        //main play area frame
        leftX = this.width / 2 - WIDTH / 2;
        rightX = leftX + WIDTH;
        topY = this.height / 2 - HEIGHT / 2;
        bottomY = topY + HEIGHT;

        paused = true;

        hardDrop = MinigameRegistry.getConfig(TetrisVisibleConfig.class).hardDrop.mode;

        ButtonWidget returnButton = TextIconButtonWidget.builder(Text.empty(), button -> this.client.setScreen(this.parent), true)
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
        playButton.setDimensions(150, 20);

        this.addDrawableChild(returnButton);
        this.addDrawableChild(restartButton);
        this.addDrawableChild(pauseButton);
        this.addDrawableChild(new ConfigButton(85, 10, tetris));
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
        currentMino.setXY(WIDTH / 2, Block.SIZE);

        leftPressed = rightPressed = upPressed = downPressed = spacePressed = false;

        nextMino = pickMino();
        nextMino.setXY(WIDTH + Block.SIZE * 2 +
                        (nextMino instanceof MinoL2 || nextMino instanceof MinoZ1 ? Block.SIZE : (nextMino instanceof MinoT ? Block.SIZE / 2 : 0)),
                HEIGHT - (int) (Block.SIZE * 2.5f));
    }

    public void manager() {
        if (currentMino == null) {
            reset();
        }
        if (!currentMino.active) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvent.of(Identifier.ofVanilla("block.stone.place")), 1.5F, vol()));
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
                    onScreenText = Text.translatable("minigame.tetris.combo").append(" x"+combo);
                    onScreenTextOpacity = 30;
                    onScreenTextColour = (combo > 3 ? Colors.RED : (combo > 2 ? Colors.YELLOW : Colors.WHITE));
                    score += 50 * (combo - 1);
                }
            } else combo = 0;
            switch (lines) {
                case 1: score += 100; break;
                case 2: score += 300; break;
                case 3: score += 500; break;
                case 4: score += 800;
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvent.of(Identifier.ofVanilla("entity.generic.explode")), 0.8F, vol()));
                    animations.add(new Animation(this.width/2 - width/2, currentMino.b[2].y, width, height, "explosion", 20));
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
                case 9: dropInterval = 8; break;
                case 10: dropInterval = 6; break;
            }

            for (Block b : currentMino.b) {
                if (b.y <= Block.SIZE * 2) {
                    gameOver();
                    return;
                }
            }

            currentMino = nextMino;
            currentMino.setXY(WIDTH / 2, Block.SIZE);

            nextMino = pickMino();
            nextMino.setXY(WIDTH + Block.SIZE * 2 +
                            (nextMino instanceof MinoL2 || nextMino instanceof MinoZ1 ? Block.SIZE : (nextMino instanceof MinoT ? Block.SIZE / 2 : 0)),
                    HEIGHT - (int) (Block.SIZE * 2.5f));
        }
        float frameDuration = MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks();
        currentMino.update(frameDuration*3);
        animation+=frameDuration*3;
    }

    private void gameOver() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvent.of(Identifier.ofVanilla("entity.pig.ambient")), 1.0F, vol()));
        isNewHighScore = score > MinigameRegistry.getConfig(TetrisConfig.class).tetrisHighScore;
        active = false;
        tetris.getLeaderboard().doPost(score);
    }

    private boolean checkClear(int y) {
        int count = 0;
        for (Block block : staticBlocks) {
            if (block.y == y) count++;
        }
        if (count < gridX) {
            return false;
        }
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvent.of(Identifier.ofVanilla("block.deepslate.break")), 1.0F, vol()));
        linesCleared++;
        for (Block block : staticBlocks) {
            if (block.y == y) {
                destroying.add(block);
            }
        }

        staticBlocks.removeIf(b -> b.y == y);
        for (Block block : staticBlocks) {
            if (block.y < y) block.y += Block.SIZE;
        }

        if (linesCleared % levelLength == 0) {
            level++;
        }
        return true;
    }

    private Mino pickMino() {
        Mino mino = null;
        int i = new Random().nextInt(7);
        mino = switch (i) {
            case 0 -> new MinoL1();
            case 1 -> new MinoL2();
            case 2 -> new MinoSquare();
            case 3 -> new MinoBar();
            case 4 -> new MinoT();
            case 5 -> new MinoZ1();
            case 6 -> new MinoZ2();
            default -> mino;
        };
        return mino;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (active) super.render(context, mouseX, mouseY, delta);
        //called here as this is run quite frequently
        if (!paused && active) manager();
        if (paused) {
            upPressed = downPressed = leftPressed = rightPressed = spacePressed = false;
        }
        float scale = switch (MinecraftClient.getInstance().options.getGuiScale().getValue()) {
            case 4 -> 0.8f;
            case 1 -> 3f;
            case 2 -> 1.6f;
            default -> 1;
        };
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);
        float offsetX = context.getScaledWindowWidth() * (1 - scale) / (2f * scale);
        float offsetY = context.getScaledWindowHeight() * (1 - scale) / (2f * scale);
        context.getMatrices().translate(offsetX, offsetY);

        //draw border

        context.drawHorizontalLine(leftX - 1, rightX, topY - 1 + Block.SIZE * 3, new Color(1, 0, 0, 0.3f).getRGB());
        context.drawStrokedRectangle(leftX - 1, topY - 1, WIDTH + 2, HEIGHT + 2, Colors.WHITE);

        //draw moving mino
        if (currentMino!= null) {
            currentMino.draw(context);
            //draw hard drop
            if (hardDrop > 0) currentMino.drawHardDrop(context);
        }


        //draw next mino
        context.drawStrokedRectangle(rightX + Block.SIZE - 1, bottomY - nextHEIGHT + 1, nextWIDTH + 2, nextHEIGHT, Colors.WHITE);
        Text nextText = Text.translatable("minigame.tetris.next");
        context.drawText(this.textRenderer, nextText, rightX + Block.SIZE * 2,
                bottomY - nextHEIGHT + Block.SIZE/2, Colors.WHITE, true);
        if (nextMino!= null) nextMino.draw(context);

        //draw score
        Text scoreText = Text.translatable("minigame.tetris.score").append(": " + score);
        context.drawText(this.textRenderer, scoreText, rightX + Block.SIZE * 2,
                topY + Block.SIZE, Colors.WHITE, true);
        Text linesText = Text.translatable("minigame.tetris.lines").append(": " + linesCleared);
        context.drawText(this.textRenderer, linesText, rightX + Block.SIZE * 2,
                topY + Block.SIZE + 10, Colors.WHITE, true);

        //draw static minos
        for (Block block : staticBlocks) {
            block.draw(context);
        }

        //draw destroying minos
        for (Block d : destroying) {
            d.destroying += MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks()*3;
            if (d.destroying>9) d.destroying = 9;
            d.draw(context);
        }
        destroying.removeIf(d -> d.destroying >= 9);

        //draw explosions
        for (Animation a : animations) {
            if (a.animation.equals("explosion")) a.draw(context, this.width/2 - a.width/2, topY + a.y - a.height/2);
            else a.draw(context);
            a.frame += 1f;
        }
        animations.removeIf(an -> an.frame > an.frames);

        //draw paused text
        Text pausedText = Text.translatable("minigame.tetris.paused");
        if (paused&&active) context.drawText(this.textRenderer, pausedText, this.width / 2 - (3 * pausedText.getString().length()),
                this.height / 2 - 7, Colors.WHITE, true);

        //draw combo and tetris texts
        if (onScreenTextOpacity > 0) {
            Color base = new Color(onScreenTextColour, false);
            float alpha = Math.clamp(onScreenTextOpacity/10f,0,1);
            Color color = new Color(base.getRed()/255f, base.getGreen()/255f, base.getBlue()/255f, alpha);
            context.drawText(this.textRenderer, onScreenText, this.width / 2 - onScreenText.getString().length(), this.height / 2, color.getRGB(), true);
            onScreenTextOpacity--;
        }

        context.drawText(this.textRenderer, Text.literal("Tetris by Nukebob"), leftX, bottomY + 4, 0xFFAAAAAA, true);

        //draw play button if not active
        playButton.visible = !active;
        if (!active) {
            Color black = new Color(Colors.BLACK);
            context.fill(leftX - 1, topY - 1, leftX - 1 + WIDTH + 2, topY -1 + HEIGHT + 2, new Color(black.getRed(), black.getGreen(), black.getBlue(), 0.75f).getRGB());
            super.render(context, mouseX, mouseY, delta);
            if (currentMino != null) {
                Text finalScoreText = Text.translatable("minigame.tetris.score").append(": " + score).withColor(Colors.LIGHT_YELLOW);
                context.drawText(this.textRenderer, finalScoreText, this.width / 2 - (finalScoreText.getString().length() * 3),
                        this.height / 2 - 35, Colors.WHITE, true);
                Text linesClearedText = Text.translatable("minigame.tetris.lines").append(": " + linesCleared).withColor(Colors.LIGHT_YELLOW);
                context.drawText(this.textRenderer, linesClearedText, this.width / 2 - (linesClearedText.getString().length() * 3),
                        this.height / 2 - 25, Colors.WHITE, true);
                Text highScoreClearedText;
                if (score > MinigameRegistry.getConfig(TetrisConfig.class).tetrisHighScore) {
                    MinigameRegistry.getConfig(TetrisConfig.class).tetrisHighScore = score;
                }
                highScoreClearedText = Text.translatable(isNewHighScore ? "minigame.tetris.new_high_score" : "minigame.tetris.high_score").append(": " + MinigameRegistry.getConfig(TetrisConfig.class).tetrisHighScore).withColor(Colors.YELLOW);
                if (MinigameRegistry.getConfig(TetrisConfig.class).tetrisHighScore > 0) context.drawText(this.textRenderer, highScoreClearedText, this.width / 2 - (highScoreClearedText.getString().length() * 3),
                        this.height / 2 + 25, Colors.WHITE, true);
            }
        }
        context.getMatrices().popMatrix();
    }

    @Override
    public boolean keyPressed(@NotNull KeyInput input) {
        int keyCode = input.key();
        if (keyCode == 256 && this.shouldCloseOnEsc()) {
            this.close();
            return true;
        } else {
            switch (keyCode) {
                case 262, 68:
                    rightPressed = true;
                    break;
                case 263, 65:
                    leftPressed = true;
                    break;
                case 264, 83:
                    downPressed = true;
                    break;
                case 265, 87:
                    upPressed = true;
                    break;
                case 32:
                    spacePressed = true;
                    break;
                default:
                    return false;
            }
        }

        return false;
    }

    private float vol() {
        return 5 * MinigameRegistry.getConfig(TetrisVisibleConfig.class).volume;
    }
}