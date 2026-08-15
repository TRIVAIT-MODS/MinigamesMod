package org.trivait.minigamesmod.minigame.dino;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;
import org.joml.Vector4d;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.PlayingSoundManager;

public class Dino {
    private double y;
    private double velocityY = 0;
    private int road;
    private boolean runFrame = false;
    public boolean running = true;
    public boolean crouching = false;
    public int tickCounter = 0;

    private static final Identifier DEFAULT = Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/dino/dino.png");
    private static final Identifier RUN = Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/dino/dino_running.png");
    private static final Identifier CROUCH = Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/dino/dino_crouching.png");

    private static final int WIDTH = 88/2;
    private static final int WIDTH_CROUCH = 118/2;
    private static final int HEIGHT = 94/2;
    private static final int HEIGHT_CROUCH = 60/2;
    private static final double BASE_GRAVITY = 4.0;
    private static final double CROUCH_GRAVITY_MULTIPLIER = 1.8;
    private static final double JUMP_VELOCITY = -26.0;

    public Dino(int y, int road) {
        this.y = y;
        this.road = road;
    }

    public void render(GuiGraphicsExtractor ctx, float delta, int x) {
        Vector4d box = getBox(x);

        switch (getDinoState()) {
            case RUN -> {
                ctx.blit(RenderPipelines.GUI_TEXTURED, RUN, (int) box.x, (int) box.y, runFrame ? 0 : WIDTH, 0, WIDTH, HEIGHT, WIDTH * 2, HEIGHT);
            }
            case CROUCH -> {
                ctx.blit(RenderPipelines.GUI_TEXTURED, CROUCH, (int) box.x, (int) box.y, runFrame ? 0 : WIDTH_CROUCH, 0, WIDTH_CROUCH, HEIGHT_CROUCH, WIDTH_CROUCH * 2, HEIGHT_CROUCH);
            }
            case NONE -> {
                ctx.blit(RenderPipelines.GUI_TEXTURED, DEFAULT, (int) box.x, (int) box.y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
            }
        }
    }

    public void update(float delta) {
        if (y < road || velocityY != 0) {
            double currentGravity = crouching ? BASE_GRAVITY * CROUCH_GRAVITY_MULTIPLIER : BASE_GRAVITY;
            velocityY += currentGravity * delta;
            y += velocityY * delta;

            if (y >= road) {
                y = road;
                velocityY = 0;
            }
        }
    }

    public void tick() {
        tickCounter++;

        if (tickCounter>=3) {
            runFrame = !runFrame;
        }
    }

    public void jump() {
        if (y >= road && !crouching) {
            velocityY = JUMP_VELOCITY;

            PlayingSoundManager.playSound(SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("block.wooden_button.click_on")), 2.0F, GoogleDino.vol());
        }
    }

    public Vector4d getBox(int x) {
        DinoState state = getDinoState();
        if (state == DinoState.CROUCH) {
            return new Vector4d(x + 4, (int) y - HEIGHT_CROUCH + 2, x + WIDTH_CROUCH - 4, (int) y - 2);
        } else {
            return new Vector4d(x + 10, (int) y - HEIGHT + 3, x + WIDTH-2, (int) y - 2);
        }
    }

    public DinoState getDinoState() {
        if (crouching) return DinoState.CROUCH;
        if (running && y >= road) return DinoState.RUN;
        return DinoState.NONE;
    }

    public enum DinoState {
        RUN,
        CROUCH,
        NONE
    }
}