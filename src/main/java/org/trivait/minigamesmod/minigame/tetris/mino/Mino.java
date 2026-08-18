package org.trivait.minigamesmod.minigame.tetris.mino;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.minigame.tetris.HardDropAnimation;
import org.trivait.minigamesmod.minigame.tetris.TetrisScreen;
import org.trivait.minigamesmod.minigame.tetris.TetrisVisibleConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Mino {
    public Block[] b = new Block[4];
    public Block[] tempB = new Block[4];
    float dropCounter = 0;
    public int direction = 1;
    boolean leftCollision, rightCollision, bottomCollision;
    public boolean active = true;
    protected String type;

    private float vol() {
        return MinigameRegistry.getConfig(TetrisVisibleConfig.class).volume / 10f;
    }

    public Mino() {
        switch (this) {
            case MinoSquare ignored -> type = "square";
            case MinoBar ignored -> type = "bar";
            case MinoT ignored -> type = "t";
            case MinoL1 ignored -> type = "l1";
            case MinoL2 ignored -> type = "l2";
            case MinoZ1 ignored -> type = "z1";
            case MinoZ2 ignored -> type = "z2";
            default -> {
            }
        }

        TextureResource randomBlockTexture = MinigameRegistry.getConfig(TetrisVisibleConfig.class).randomBlocks ? getRandomBlockTexture() : getFixedBlockTexture();
        create(randomBlockTexture.texture, randomBlockTexture.width, randomBlockTexture.height);
    }

    public void create(Identifier t, int textureWidth, int textureHeight) {
        for (int i = 0; i < 4; i++) {
            b[i] = new Block(t, textureWidth, textureHeight);
            tempB[i] = new Block(t, textureWidth, textureHeight);
        }
    }

    public void setXY(int x, int y) {}

    public void updateXY(int direction) {
        for (Block block : tempB) {
            if (block.x < 0) return;
            if (block.x > TetrisScreen.WIDTH - Block.SIZE) return;
            if (block.y > TetrisScreen.HEIGHT - Block.SIZE) return;
            for (Block sB : TetrisScreen.staticBlocks) {
                if (sB.x == block.x && sB.y == block.y) return;
            }
        }
        this.direction = direction;
        for (int i = 0; i < 4; i++) {
            b[i].x = tempB[i].x;
            b[i].y = tempB[i].y;
        }
        MinecraftClient.getInstance().getSoundManager().play(
            PositionedSoundInstance.master(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol()));
    }

    public void getDirection1() {}
    public void getDirection2() {}
    public void getDirection3() {}
    public void getDirection4() {}

    public void checkMovementCollision() {
        leftCollision = false;
        rightCollision = false;
        bottomCollision = false;
        checkStaticBlockCollision();
        for (Block block : b) {
            if (block.x - Block.SIZE < 0) { leftCollision = true; break; }
        }
        for (Block block : b) {
            if (block.x + Block.SIZE >= TetrisScreen.WIDTH) { rightCollision = true; break; }
        }
        for (Block block : b) {
            if (block.y + Block.SIZE >= TetrisScreen.HEIGHT) { bottomCollision = true; break; }
        }
    }

    private void checkStaticBlockCollision() {
        for (Block staticBlock : TetrisScreen.staticBlocks) {
            for (Block block : b) {
                if (block.y + Block.SIZE == staticBlock.y && block.x == staticBlock.x) { bottomCollision = true; break; }
            }
            for (Block block : b) {
                if (block.x - Block.SIZE == staticBlock.x && block.y == staticBlock.y) { leftCollision = true; break; }
            }
            for (Block block : b) {
                if (block.x + Block.SIZE == staticBlock.x && block.y == staticBlock.y) { rightCollision = true; break; }
            }
        }
    }

    protected TextureResource getRandomBlockTexture() {
        MinecraftClient client = MinecraftClient.getInstance();
        while (true) {
            net.minecraft.block.Block block = Registries.BLOCK.get(new Random().nextInt(Registries.BLOCK.size()));
            BlockState blockState = block.getStateManager().getStates().get(new Random().nextInt(block.getStateManager().getStates().size()));
            List<BakedQuad> quads = client.getBlockRenderManager().getModel(blockState).getQuads(blockState, Direction.random(client.textRenderer.random), client.textRenderer.random);
            if (!quads.isEmpty()) {
                SpriteContents texture = quads.get(new Random().nextInt(quads.size())).getSprite().getContents();
                if (texture.getWidth() == 16 && texture.getHeight() == 16) {
                    Optional<Resource> opR = client.getResourceManager().getResource(Identifier.of("textures/" + texture.getId().getPath() + ".png"));
                    if (opR.isPresent()) {
                        try {
                            BufferedImage image = ImageIO.read(opR.get().getInputStream());
                            if (image.getRGB(0, 0) < 0 && image.getRGB(15, 0) < 0)
                                return new TextureResource(Identifier.of(texture.getId().getNamespace().split(":")[0], "textures/" + texture.getId().getPath() + ".png"), image.getWidth(), image.getHeight());
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    protected TextureResource getFixedBlockTexture() {
        Identifier texture = switch (type) {
            case "square" -> Identifier.ofVanilla("textures/block/gold_block.png");
            case "bar" -> Identifier.ofVanilla("textures/block/diamond_block.png");
            case "t" -> Identifier.ofVanilla("textures/block/amethyst_block.png");
            case "l1" -> Identifier.ofVanilla("textures/block/copper_block.png");
            case "l2" -> Identifier.ofVanilla("textures/block/lapis_block.png");
            case "z1" -> Identifier.ofVanilla("textures/block/redstone_block.png");
            case "z2" -> Identifier.ofVanilla("textures/block/emerald_block.png");
            default -> Identifier.ofVanilla("textures/block/iron_block.png");
        };

        return new TextureResource(texture, 16, 16);
    }

    public void update(float timePassed) {
        checkMovementCollision();
        if (TetrisScreen.leftPressed) {
            checkMovementCollision();
            if (!leftCollision) {
                boolean proceed = true;
                for (Block block : b) {
                    for (Block sBlock : TetrisScreen.staticBlocks) {
                        if (block.x - Block.SIZE == sBlock.x && block.y == sBlock.y) { proceed = false; break; }
                    }
                    if (block.x - Block.SIZE < 0) { proceed = false; break; }
                }
                if (proceed) {
                    MinecraftClient.getInstance().getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol()));
                    for (Block block : b) block.x -= Block.SIZE;
                }
            }
            TetrisScreen.leftPressed = false;
        }
        if (TetrisScreen.rightPressed) {
            checkMovementCollision();
            if (!rightCollision) {
                boolean proceed = true;
                for (Block block : b) {
                    for (Block sBlock : TetrisScreen.staticBlocks) {
                        if (block.x + Block.SIZE == sBlock.x && block.y == sBlock.y) { proceed = false; break; }
                    }
                    if (block.x + Block.SIZE > TetrisScreen.WIDTH) { proceed = false; break; }
                }
                if (proceed) {
                    MinecraftClient.getInstance().getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol()));
                    for (Block block : b) block.x += Block.SIZE;
                }
            }
            TetrisScreen.rightPressed = false;
        }
        if (TetrisScreen.upPressed) {
            switch (direction) {
                case 1 -> getDirection2();
                case 2 -> getDirection3();
                case 3 -> getDirection4();
                case 4 -> getDirection1();
            }
            TetrisScreen.upPressed = false;
        }
        if (TetrisScreen.downPressed) {
            if (bottomCollision) {
                dropCounter += 30;
            } else {
                boolean proceed = true;
                for (Block block : b) {
                    for (Block sb : TetrisScreen.staticBlocks) {
                        if (block.y + Block.SIZE == sb.y && block.x == sb.x) { proceed = false; break; }
                    }
                }
                if (proceed) {
                    MinecraftClient.getInstance().getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol()));
                    for (Block block : b) block.y += Block.SIZE;
                    TetrisScreen.score++;
                    dropCounter = 0;
                }
            }
            TetrisScreen.downPressed = false;
        }
        if (TetrisScreen.spacePressed && TetrisScreen.hardDrop > 0) {
            MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvent.of(Identifier.ofVanilla("entity.wind_charge.wind_burst")), 1.0F, vol() / 2));
            int drop = getDropOffset();
            TetrisScreen.animations.add(new HardDropAnimation(b[0].x, b[0].y, 27, drop, 10));
            for (Block block : b) block.y += drop;
            TetrisScreen.spacePressed = false;
            this.active = false;
            TetrisScreen.score += 2 * drop / Block.SIZE;
            return;
        }
        dropCounter += timePassed;
        if (Math.floor(dropCounter) >= TetrisScreen.dropInterval) {
            if (bottomCollision) {
                checkStaticBlockCollision();
                checkMovementCollision();
                if (bottomCollision) active = false;
            } else {
                boolean proceed = true;
                for (Block block : b) {
                    for (Block sb : TetrisScreen.staticBlocks) {
                        if (block.y + Block.SIZE == sb.y && block.x == sb.x) { proceed = false; break; }
                    }
                }
                if (proceed) {
                    for (Block block : b) block.y += Block.SIZE;
                    dropCounter = 0;
                }
            }
        }
    }

    public void draw(DrawContext context) {
        for (Block block : b) block.draw(context);
    }

    public void drawHardDrop(DrawContext context) {
        int yOffset = getDropOffset();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 0.5f);
        for (Block block : b) {
            switch (TetrisScreen.hardDrop) {
                case 2 -> {
                    boolean[] outline = getOutline(block);
                    if (outline[0]) context.drawHorizontalLine(TetrisScreen.left_x + block.x, TetrisScreen.left_x + block.x + Block.SIZE - 1, TetrisScreen.top_y + block.y + yOffset, Colors.WHITE);
                    if (outline[1]) context.drawHorizontalLine(TetrisScreen.left_x + block.x, TetrisScreen.left_x + block.x + Block.SIZE - 1, TetrisScreen.top_y + block.y + Block.SIZE - 1 + yOffset, Colors.WHITE);
                    if (outline[2]) {
                        context.drawVerticalLine(TetrisScreen.left_x + block.x, TetrisScreen.top_y + block.y + yOffset, TetrisScreen.top_y + block.y + Block.SIZE - 1 + yOffset, Colors.WHITE);
                        if (!outline[1]) context.drawVerticalLine(TetrisScreen.left_x + block.x, TetrisScreen.top_y + block.y + Block.SIZE + yOffset - 1, TetrisScreen.top_y + block.y + Block.SIZE + yOffset - 1, Colors.WHITE);
                        if (!outline[0]) context.drawVerticalLine(TetrisScreen.left_x + block.x, TetrisScreen.top_y + block.y + yOffset, TetrisScreen.top_y + block.y + yOffset, Colors.WHITE);
                    }
                    if (outline[3]) {
                        context.drawVerticalLine(TetrisScreen.left_x + block.x + Block.SIZE - 1, TetrisScreen.top_y + block.y + yOffset, TetrisScreen.top_y + block.y + Block.SIZE - 1 + yOffset, Colors.WHITE);
                        if (!outline[1]) context.drawVerticalLine(TetrisScreen.left_x + block.x + Block.SIZE - 1, TetrisScreen.top_y + block.y + Block.SIZE + yOffset - 1, TetrisScreen.top_y + block.y + Block.SIZE + yOffset - 1, Colors.WHITE);
                        if (!outline[0]) context.drawVerticalLine(TetrisScreen.left_x + block.x + Block.SIZE - 1, TetrisScreen.top_y + block.y + yOffset, TetrisScreen.top_y + block.y + yOffset, Colors.WHITE);
                    }
                    if (!(this instanceof MinoSquare)) {
                        if (!outline[0] && !outline[2]) context.drawVerticalLine(TetrisScreen.left_x + block.x, TetrisScreen.top_y + block.y + yOffset, TetrisScreen.top_y + block.y + yOffset, Colors.WHITE);
                        if (!outline[0] && !outline[3]) context.drawVerticalLine(TetrisScreen.left_x + block.x + Block.SIZE - 1, TetrisScreen.top_y + block.y + yOffset, TetrisScreen.top_y + block.y + yOffset, Colors.WHITE);
                        if (!outline[1] && !outline[2]) context.drawVerticalLine(TetrisScreen.left_x + block.x, TetrisScreen.top_y + block.y + Block.SIZE + yOffset - 1, TetrisScreen.top_y + block.y + Block.SIZE + yOffset - 1, Colors.WHITE);
                        if (!outline[1] && !outline[3]) context.drawVerticalLine(TetrisScreen.left_x + block.x + Block.SIZE - 1, TetrisScreen.top_y + block.y + Block.SIZE + yOffset - 1, TetrisScreen.top_y + block.y + Block.SIZE + yOffset - 1, Colors.WHITE);
                    }
                }
                case 3 -> block.draw(context, yOffset);
            }
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private int getDropOffset() {
        int i;
        for (i = 0; i < TetrisScreen.HEIGHT / Block.SIZE; i++) {
            for (Block block : b) {
                for (Block sB : TetrisScreen.staticBlocks) {
                    if (block.x == sB.x && block.y + i * Block.SIZE == sB.y) return i * Block.SIZE - Block.SIZE;
                }
                if (block.y + i * Block.SIZE > TetrisScreen.HEIGHT - Block.SIZE) return i * Block.SIZE - Block.SIZE;
            }
        }
        return i * Block.SIZE - Block.SIZE;
    }

    private boolean[] getOutline(Block b) {
        boolean top = true, bottom = true, left = true, right = true;
        for (Block block : this.b) {
            if (b.y - Block.SIZE == block.y && b.x == block.x) top = false;
            if (b.y + Block.SIZE == block.y && b.x == block.x) bottom = false;
            if (b.x - Block.SIZE == block.x && b.y == block.y) left = false;
            if (b.x + Block.SIZE == block.x && b.y == block.y) right = false;
        }
        return new boolean[]{top, bottom, left, right};
    }
}
