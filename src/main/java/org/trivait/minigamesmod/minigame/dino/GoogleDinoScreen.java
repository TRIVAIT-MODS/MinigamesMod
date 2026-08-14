package org.trivait.minigamesmod.minigame.dino;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Vector4d;
import org.lwjgl.glfw.GLFW;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.ConfigButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GoogleDinoScreen extends Screen {

    private static final int GUI_WIDTH = 350;
    private static final int GUI_HEIGHT = 197;
    private static final Identifier GUI_TEXTURE = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/dino/gui.png");
    private static final int GAME_WIDTH = GUI_WIDTH-16;
    private static final int GAME_HEIGHT = GUI_HEIGHT-16;

    private static final int ROAD_WIDTH = 2400;
    private static final int ROAD_HEIGHT = 24;
    private static final Identifier ROAD_TEXTURE = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/dino/road.png");

    private float speed = 20;
    private int score = 0;

    private float road1X = 0;
    private float road2X = ROAD_WIDTH;

    private int tickCounter = 0;

    private boolean gameRunning = true;

    private Dino dino;
    private GoogleDino minigame;
    private Screen parent;
    private List<GameObject> objects = new ArrayList<>();
    private final Random random = new Random();
    private int obstacleSpawnTicks = 0;
    private int obstacleSpawnDelay = 22;
    private int cloudSpawnTicks = 0;
    private int cloudSpawnDelay = 18;

    public GoogleDinoScreen(GoogleDino minigame, Screen parent) {
        super(Text.empty());
        this.parent = parent;
        this.minigame = minigame;
    }

    @Override
    protected void init() {
        ButtonWidget returnButton = TextIconButtonWidget.builder(Text.empty(), button -> this.close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15).build();
        returnButton.setTooltip(Tooltip.of(Text.translatable("minigame.2048.undo")));
        returnButton.setDimensionsAndPosition(20, 20, 10, 10);

        ButtonWidget restartButton = TextIconButtonWidget.builder(Text.empty(), button -> restart(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15).build();
        restartButton.setTooltip(Tooltip.of(Text.translatable("minigame.restart")));
        restartButton.setDimensionsAndPosition(20, 20, 35, 10);

        this.addDrawableChild(restartButton);
        this.addDrawableChild(returnButton);
        this.addDrawableChild(new ConfigButton(60, 10, minigame));

        dino = new Dino((height-GUI_HEIGHT)/2+8+GAME_HEIGHT-4-ROAD_HEIGHT+17, (height-GUI_HEIGHT)/2+8+GAME_HEIGHT-4-ROAD_HEIGHT+17);
    }

    private void restart() {
        road1X=0;
        road2X=ROAD_WIDTH;
        gameRunning = true;
        score=0;
        objects.clear();
        dino = new Dino((height-GUI_HEIGHT)/2+8+GAME_HEIGHT-4-ROAD_HEIGHT+17, (height-GUI_HEIGHT)/2+8+GAME_HEIGHT-4-ROAD_HEIGHT+17);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();

        if (keyCode==GLFW.GLFW_KEY_SPACE||keyCode==GLFW.GLFW_KEY_UP) {
            dino.jump();
        }
        if (keyCode==GLFW.GLFW_KEY_LEFT_SHIFT||keyCode==GLFW.GLFW_KEY_LEFT_CONTROL||keyCode==GLFW.GLFW_KEY_DOWN||keyCode==GLFW.GLFW_KEY_S) {
            dino.crouching = true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        int keyCode = input.key();
        if (keyCode==GLFW.GLFW_KEY_LEFT_SHIFT||keyCode==GLFW.GLFW_KEY_LEFT_CONTROL||keyCode==GLFW.GLFW_KEY_DOWN||keyCode==GLFW.GLFW_KEY_S) {
            dino.crouching = false;
        }
        return super.keyReleased(input);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.selected = null;
        setFocused(null);
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        Matrix3x2fStack matrices = context.getMatrices();

        int guiX = (width-GUI_WIDTH)/2;
        int guiY = (height-GUI_HEIGHT)/2;

        if (gameRunning) {
            if (objects != null) {
                for (GameObject gameObject : objects) {
                    gameObject.x -= speed * delta;
                }
            }

            dino.update(delta);
            checkCollisions(guiX + 10);

            road1X -= speed * delta;
            road2X -= speed * delta;
        }

        if (road1X+ROAD_WIDTH<0) {
            road1X=road2X+ROAD_WIDTH;
        }
        if (road2X+ROAD_WIDTH<0) {
            road2X=road1X+ROAD_WIDTH;
        }

        context.drawTexture(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, guiX, guiY, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        context.fill(guiX+8, guiY+8, guiX+8+GAME_WIDTH, guiY+8+GAME_HEIGHT, -1);

        renderObjects(context, delta, guiX+8, guiY+8);

        String formattedScore = String.format("%05d", score);
        int scoreHI = MinigameRegistry.getConfig(GoogleDinoConfig.class).score;
        String formattedHIscore = String.format("%05d", scoreHI);

        int textWidth = (int) (tr.getWidth("HI " + formattedHIscore + " " + formattedScore) * 1.5F);
        int textX = guiX + 8 + GAME_WIDTH - textWidth - 24;
        int textY = guiY + 8 + 4;

        matrices.pushMatrix();
        matrices.translate(textX, textY);
        matrices.scale(1.5F, 1.5F);
        context.drawText(tr, Text.literal("HI " + formattedHIscore).styled(style -> style.withBold(true).withColor(0x909191)).append(Text.literal(" " + formattedScore).styled(style -> style.withBold(true).withColor(0xACACAC))), 0, 0, 0xFFFFFFFF, false);
        matrices.popMatrix();

        if (!gameRunning) {
            String overText = "Game over!";
            int overWidth = (int) (tr.getWidth(overText) * 2.0F);
            int overX = guiX + 8 + (GAME_WIDTH - overWidth) / 2;
            int overY = guiY + 8 + GAME_HEIGHT / 2 - 10;

            matrices.pushMatrix();
            matrices.translate(overX-10, overY);
            matrices.scale(2.0F, 2.0F);
            context.drawText(tr, Text.literal(overText).styled(style -> style.withBold(true).withColor(0x909191)), 0, 0, 0xFFFFFFFF, false);
            matrices.popMatrix();
        }
    }

    @Override
    public void tick() {
        if (!gameRunning) return;
        tickCounter++;
        if (tickCounter % 2 == 0) {
            score++;
            dino.tick();
        }

        obstacleSpawnTicks++;
        cloudSpawnTicks++;

        int gameX = (width-GUI_WIDTH)/2 + 8;
        int gameY = (height-GUI_HEIGHT)/2 + 8;
        int lastObstacleX = getLastObstacleX();

        if (obstacleSpawnTicks >= obstacleSpawnDelay && lastObstacleX < gameX + GAME_WIDTH - 90) {
            obstacleSpawnTicks = 0;
            int maxReduction = Math.min(score / 50, 14);
            int randomReduction = maxReduction > 0 ? random.nextInt(maxReduction) : 0;
            obstacleSpawnDelay = Math.max(14, 28 - randomReduction);
            spawnObstacle(gameX, gameY);
        }

        if (cloudSpawnTicks >= cloudSpawnDelay) {
            cloudSpawnTicks = 0;
            cloudSpawnDelay = 18 + random.nextInt(14);
            spawnCloud(gameX, gameY);
        }

        if (objects != null) {
            objects.removeIf(object -> object.x + object.width < gameX);
            for (GameObject gameObject : objects) {
                gameObject.tick();
            }
        }

        speed = 20 + Math.min(score * 0.0025f, 18f);
    }

    private void renderObjects(DrawContext ctx, float delta, int gameX, int gameY) {
        ctx.enableScissor(gameX, gameY, gameX+GAME_WIDTH, gameY+GAME_HEIGHT);
        renderRoad(ctx, gameX, gameY);

        if (objects!=null) {
            for (GameObject gameObject : objects) {
                gameObject.render(ctx, delta);
            }
        }

        dino.render(ctx, delta, gameX+10);

        ctx.disableScissor();
    }

    private void renderRoad(DrawContext ctx, int gameX, int gameY) {
        int y = gameY+GAME_HEIGHT-4-ROAD_HEIGHT;
        int x = (int) (gameX+road1X);

        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, ROAD_TEXTURE, x, y, 0, 0, ROAD_WIDTH, ROAD_HEIGHT, ROAD_WIDTH, ROAD_HEIGHT);
        x = (int) (gameX+road2X);
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, ROAD_TEXTURE, x, y, 0, 0, ROAD_WIDTH, ROAD_HEIGHT, ROAD_WIDTH, ROAD_HEIGHT);
    }

    private void spawnObstacle(int gameX, int gameY) {
        int spawnX = gameX + GAME_WIDTH + random.nextInt(20, 80);
        int baseY = (height-GUI_HEIGHT)/2 + 8 + GAME_HEIGHT - 4 - ROAD_HEIGHT + 17;

        GameObject newObject;
        int choice = random.nextInt(100);
        if (choice < 36 && score > 30) {
            int flyY = baseY - random.nextInt(26, 62);
            newObject = new Pterodactyl(spawnX, flyY);
        } else if (choice < 62) {
            newObject = new Cactus(spawnX, baseY);
        } else {
            newObject = new CactusLarge(spawnX, baseY);
        }
        objects.add(newObject);
    }

    private void spawnCloud(int gameX, int gameY) {
        int spawnX = gameX + GAME_WIDTH + random.nextInt(20, 90);
        int spawnY = gameY + random.nextInt(16, GAME_HEIGHT / 2);
        objects.add(new Cloud(spawnX, spawnY));
    }

    private int getLastObstacleX() {
        int lastX = -9999;
        for (GameObject object : objects) {
            if (!object.isCollidable()) continue;
            lastX = Math.max(lastX, (int) object.x);
        }
        return lastX;
    }

    private void checkCollisions(int dinoX) {
        Vector4d dinoBox = dino.getBox(dinoX);
        for (GameObject object : objects) {
            if (!object.isCollidable()) continue;
            Vector4d other = object.getBox();
            if (dinoBox.x < other.z && dinoBox.z > other.x && dinoBox.y < other.w && dinoBox.w > other.y) {
                GoogleDinoConfig config = MinigameRegistry.getConfig(GoogleDinoConfig.class);
                if (score > config.score) {
                    config.score = score;
                }
                PlayingSoundManager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1, GoogleDino.vol());
                gameRunning = false;
                minigame.getLeaderboard().doPost(MinecraftClient.getInstance().getSession().getUsername(), score, true);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        boolean handled = super.mouseClicked(click, doubled);
        dino.jump();
        if (!gameRunning && click.button() == 0 && !handled) {
            restart();
            return true;
        }
        return handled;
    }

    @Override
    public void close() {
        super.close();
        MinecraftClient.getInstance().setScreen(parent);
    }
}
