package org.trivait.minigamesmod.minigame.sudoku;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.ConfigButton;

public class SudokuScreen extends Screen {

    private final Screen parent;
    private final Sudoku minigame;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean won = false;

    private static int[][] savedGrid = null;
    private static boolean[][] savedInitial = null;
    private static int[][] savedSolution = null;

    public SudokuScreen(Screen parent, Sudoku sudoku) {
        super(Component.literal("Sudoku"));
        this.parent = parent;
        if (savedGrid == null) {
            initNewGame();
        }
        this.minigame = sudoku;
    }

    @Override
    protected void init() {
        Button returnButton = SpriteIconButton.builder(Component.empty(), button -> this.onClose(), true)
                .sprite(Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "icon/return"), 15, 15).build();
        returnButton.setTooltip(Tooltip.create(Component.translatable("minigame.2048.undo")));
        returnButton.setRectangle(20, 20, 10, 10);

        Button restartButton = SpriteIconButton.builder(Component.empty(), button -> resetGame(), true)
                .sprite(Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "icon/restart"), 15, 15).build();
        restartButton.setTooltip(Tooltip.create(Component.translatable("minigame.restart")));
        restartButton.setRectangle(20, 20, 35, 10);

        this.addRenderableWidget(restartButton);
        this.addRenderableWidget(returnButton);
        this.addRenderableWidget(new ConfigButton(60, 10, minigame));
    }

    private void initNewGame() {
        savedGrid = new int[9][9];
        savedInitial = new boolean[9][9];
        SudokuBoard board = new SudokuBoard(MinigameRegistry.getConfig(SudokuVisibleConfig.class).difficulty.getCellsToRemove());
        int[][] puzzle = board.getPuzzle();
        boolean[][] initMap = board.getInitial();
        savedSolution = board.getSolution();
        for (int r = 0; r < 9; r++) {
            System.arraycopy(puzzle[r], 0, savedGrid[r], 0, 9);
            System.arraycopy(initMap[r], 0, savedInitial[r], 0, 9);
        }
        won = false;
    }

    public void resetGame() {
        initNewGame();
    }

    private void checkWinCondition() {
        if (won || savedGrid == null || savedSolution == null) {
            return;
        }

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (savedGrid[r][c] != savedSolution[r][c]) {
                    return;
                }
            }
        }

        won = true;

        if (MinigameRegistry.getConfig(SudokuVisibleConfig.class).difficulty == Difficulty.MEDIUM) {
            minigame.getLeaderboard().doPost(1);
        }

        PlayingSoundManager.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, vol());
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        int boardSize = 9 * 16;
        int startX = (this.width - boardSize) / 2;
        int startY = (this.height - boardSize) / 2;

        if (won) {
            Component winText = Component.translatable("minigame.sudoku.win").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
            int winWidth = this.font.width(winText);
            context.text(this.font, winText, (this.width - winWidth) / 2, startY - 20, 0xFF00FF00, true);
        }

        context.fill(startX, startY, startX + boardSize, startY + boardSize, 0xFFFFFFFF);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int x = startX + c * 16;
                int y = startY + r * 16;

                if (r == selectedRow && c == selectedCol) {
                    context.fill(x, y, x + 16, y + 16, 0x5500A2FF);
                }

                int val = savedGrid[r][c];
                if (val != 0) {
                    String text = String.valueOf(val);
                    int textWidth = this.font.width(text);
                    int textX = x + (16 - textWidth) / 2;
                    int textY = y + (16 - 8) / 2;
                    if (savedInitial[r][c]) {
                        context.text(this.font, Component.literal(text).copy().withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), textX, textY, 0xFF0000FF, false);
                    } else {
                        context.text(this.font, Component.literal(text).copy().withStyle(ChatFormatting.BLACK, ChatFormatting.ITALIC), textX, textY, 0xFF000000, false);
                    }
                }
            }
        }

        for (int i = 0; i <= 9; i++) {
            int thickness = (i % 3 == 0) ? 2 : 1;
            int offset = i * 16;
            context.fill(startX + offset - (thickness == 2 ? 1 : 0), startY, startX + offset + (thickness == 2 ? 1 : 1), startY + boardSize, 0xFF000000);
            context.fill(startX, startY + offset - (thickness == 2 ? 1 : 0), startX + boardSize, startY + offset + (thickness == 2 ? 1 : 1), 0xFF000000);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (won) return super.mouseClicked(click, doubled);
        int boardSize = 9 * 16;
        int startX = (this.width - boardSize) / 2;
        int startY = (this.height - boardSize) / 2;

        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (mouseX >= startX && mouseX < startX + boardSize && mouseY >= startY && mouseY < startY + boardSize) {
            int c = (int) ((mouseX - startX) / 16);
            int r = (int) ((mouseY - startY) / 16);

            selectedRow = r;
            selectedCol = c;

            if (!savedInitial[r][c]) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    savedGrid[r][c] = (savedGrid[r][c] % 9) + 1;
                    PlayingSoundManager.playSound(SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("block.wooden_button.click_on")), 2.0F, vol());
                    checkWinCondition();
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    savedGrid[r][c] = savedGrid[r][c] - 1;
                    if (savedGrid[r][c] < 0) {
                        savedGrid[r][c] = 9;
                    }
                    PlayingSoundManager.playSound(SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("block.wooden_button.click_on")), 2.0F, vol());
                    checkWinCondition();
                }
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (won) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        if (selectedRow >= 0 && selectedRow < 9 && selectedCol >= 0 && selectedCol < 9) {
            if (!savedInitial[selectedRow][selectedCol]) {
                if (verticalAmount > 0) {
                    savedGrid[selectedRow][selectedCol] = (savedGrid[selectedRow][selectedCol] % 9) + 1;
                    PlayingSoundManager.playSound(SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("block.wooden_button.click_on")), 2.0F, vol());
                } else if (verticalAmount < 0) {
                    savedGrid[selectedRow][selectedCol] = savedGrid[selectedRow][selectedCol] - 1;
                    if (savedGrid[selectedRow][selectedCol] < 0) {
                        savedGrid[selectedRow][selectedCol] = 9;
                    }
                    PlayingSoundManager.playSound(SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("block.wooden_button.click_on")), 2.0F, vol());
                }
                checkWinCondition();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if (won) return super.keyPressed(input);
        if (selectedRow >= 0 && selectedRow < 9 && selectedCol >= 0 && selectedCol < 9) {
            if (!savedInitial[selectedRow][selectedCol]) {
                if (input.key() >= GLFW.GLFW_KEY_1 && input.key() <= GLFW.GLFW_KEY_9) {
                    savedGrid[selectedRow][selectedCol] = input.key() - GLFW.GLFW_KEY_1 + 1;
                    PlayingSoundManager.playSound(SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("block.wooden_button.click_on")), 2.0F, vol());
                    checkWinCondition();
                    return true;
                } else if (input.key() == GLFW.GLFW_KEY_0 || input.key() == GLFW.GLFW_KEY_BACKSPACE || input.key() == GLFW.GLFW_KEY_DELETE) {
                    savedGrid[selectedRow][selectedCol] = 0;
                    PlayingSoundManager.playSound(SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("block.wooden_button.click_on")), 2.0F, vol());
                    checkWinCondition();
                    return true;
                }
            }
        }
        return super.keyPressed(input);
    }

    public float vol() {
        return PlayingSoundManager.vol(MinigameRegistry.getConfig(SudokuVisibleConfig.class).volume);
    }
}