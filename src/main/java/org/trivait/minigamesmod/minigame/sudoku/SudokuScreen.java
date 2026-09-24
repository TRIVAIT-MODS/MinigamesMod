package org.trivait.minigamesmod.minigame.sudoku;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.ConfigButton;

public class SudokuScreen extends Screen {

    private static final int MAX_MISTAKES = 3;

    private final Screen parent;
    private final Sudoku minigame;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private int highlightedNumber = 0;

    private static int[][] savedGrid = null;
    private static boolean[][] savedInitial = null;
    private static int[][] savedSolution = null;
    private static int savedMistakes = 0;
    private static boolean savedGameOver = false;
    private static boolean savedWon = false;

    public SudokuScreen(Screen parent, Sudoku sudoku) {
        super(Text.literal("Sudoku"));
        this.parent = parent;
        if (savedGrid == null) {
            initNewGame();
        }
        this.minigame = sudoku;
    }

    @Override
    protected void init() {
        ButtonWidget returnButton = TextIconButtonWidget.builder(Text.empty(), button -> this.close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15).build();
        returnButton.setTooltip(Tooltip.of(Text.translatable("minigame.2048.undo")));
        returnButton.setDimensionsAndPosition(20, 20, 10, 10);

        ButtonWidget restartButton = TextIconButtonWidget.builder(Text.empty(), button -> resetGame(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15).build();
        restartButton.setTooltip(Tooltip.of(Text.translatable("minigame.restart")));
        restartButton.setDimensionsAndPosition(20, 20, 35, 10);

        this.addDrawableChild(restartButton);
        this.addDrawableChild(returnButton);
        this.addDrawableChild(new ConfigButton(60, 10, minigame));
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
        savedMistakes = 0;
        savedGameOver = false;
        savedWon = false;
        selectedRow = -1;
        selectedCol = -1;
        highlightedNumber = 0;
    }

    public void resetGame() {
        initNewGame();
    }

    private void checkWinCondition() {
        if (savedWon || savedGameOver || savedGrid == null || savedSolution == null) {
            return;
        }

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (savedGrid[r][c] != savedSolution[r][c]) {
                    return;
                }
            }
        }

        savedWon = true;

        if (MinigameRegistry.getConfig(SudokuVisibleConfig.class).difficulty == Difficulty.MEDIUM) {
            minigame.getLeaderboard().doPost(1);
        }

        PlayingSoundManager.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, vol());
    }

    private boolean isDigitCompleted(int num) {
        if (savedGrid == null || savedSolution == null) {
            return false;
        }
        int count = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (savedGrid[r][c] == num && (savedInitial[r][c] || savedGrid[r][c] == savedSolution[r][c])) {
                    count++;
                }
            }
        }
        return count >= 9;
    }

    private void enterNumber(int num) {
        if (savedGameOver || savedWon) {
            return;
        }
        if (isDigitCompleted(num)) {
            return;
        }
        if (selectedRow < 0 || selectedRow >= 9 || selectedCol < 0 || selectedCol >= 9) {
            highlightedNumber = (highlightedNumber == num) ? 0 : num;
            return;
        }
        if (savedInitial[selectedRow][selectedCol]) {
            highlightedNumber = (highlightedNumber == num) ? 0 : num;
            return;
        }
        if (savedGrid[selectedRow][selectedCol] == savedSolution[selectedRow][selectedCol] && savedGrid[selectedRow][selectedCol] != 0) {
            highlightedNumber = (highlightedNumber == num) ? 0 : num;
            return;
        }
        if (savedGrid[selectedRow][selectedCol] == num) {
            return;
        }
        if (num == savedSolution[selectedRow][selectedCol]) {
            savedGrid[selectedRow][selectedCol] = num;
            highlightedNumber = num;
            PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol());
            checkWinCondition();
        } else {
            savedGrid[selectedRow][selectedCol] = num;
            highlightedNumber = num;
            savedMistakes++;
            PlayingSoundManager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, vol());
            if (savedMistakes >= MAX_MISTAKES) {
                savedGameOver = true;
                PlayingSoundManager.playSound(SoundEvents.ENTITY_VILLAGER_DEATH, 0.9F, vol());
            }
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int boardSize = 9 * 16;
        int startX = (this.width - boardSize) / 2;
        int startY = (this.height - boardSize - 24) / 2 + 4;

        if (savedWon) {
            Text winText = Text.translatable("minigame.sudoku.win").formatted(Formatting.GREEN, Formatting.BOLD);
            int winWidth = this.textRenderer.getWidth(winText);
            context.drawText(this.textRenderer, winText, (this.width - winWidth) / 2, startY - 14, 0xFF00FF00, true);
        } else {
            Text mistakesText = Text.translatable("minigame.sudoku.mistakes", savedMistakes);
            int mistakesWidth = this.textRenderer.getWidth(mistakesText);
            int mistakesColor = (savedMistakes > 0) ? 0xFFFF0000 : 0xFF555555;
            context.drawText(this.textRenderer, mistakesText, (this.width - mistakesWidth) / 2, startY - 14, mistakesColor, false);
        }

        context.fill(startX, startY, startX + boardSize, startY + boardSize, 0xFFFFFFFF);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int x = startX + c * 16;
                int y = startY + r * 16;

                int val = savedGrid[r][c];

                if (highlightedNumber > 0) {
                    if (val == highlightedNumber) {
                        context.fill(x, y, x + 16, y + 16, 0x4400A2FF);
                    }
                    if (r == selectedRow || c == selectedCol || (r / 3 == selectedRow / 3 && c / 3 == selectedCol / 3)) {
                        context.fill(x, y, x + 16, y + 16, 0x1A00A2FF);
                    }
                }

                if (r == selectedRow && c == selectedCol) {
                    context.fill(x, y, x + 16, y + 16, 0x5500A2FF);
                }

                if (val != 0) {
                    String text = String.valueOf(val);
                    int textWidth = this.textRenderer.getWidth(text);
                    int textX = x + (16 - textWidth) / 2;
                    int textY = y + (16 - 8) / 2;
                    if (savedInitial[r][c]) {
                        context.drawText(this.textRenderer, Text.literal(text).copy().formatted(Formatting.BLACK, Formatting.BOLD), textX, textY, 0xFF000000, false);
                    } else if (val != savedSolution[r][c]) {
                        context.drawText(this.textRenderer, Text.literal(text).copy().formatted(Formatting.RED, Formatting.BOLD), textX, textY, 0xFFFF0000, false);
                    } else {
                        context.drawText(this.textRenderer, Text.literal(text).copy().formatted(Formatting.BLUE, Formatting.BOLD), textX, textY, 0xFF0000FF, false);
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

        int btnY = startY + boardSize + 8;
        for (int i = 1; i <= 9; i++) {
            int bx = startX + (i - 1) * 16;
            boolean completed = isDigitCompleted(i);
            boolean hovered = !completed && mouseX >= bx && mouseX < bx + 16 && mouseY >= btnY && mouseY < btnY + 16;
            int bg = completed ? 0xFFCCCCCC : ((highlightedNumber == i) ? 0x5500A2FF : (hovered ? 0xFFE0E0E0 : 0xFFFFFFFF));
            int borderColor = completed ? 0xFF888888 : 0xFF000000;
            int textColor = completed ? 0xFF888888 : 0xFF000000;

            context.fill(bx, btnY, bx + 16, btnY + 16, bg);
            context.fill(bx, btnY, bx + 16, btnY + 1, borderColor);
            context.fill(bx, btnY + 15, bx + 16, btnY + 16, borderColor);
            context.fill(bx, btnY, bx + 1, btnY + 16, borderColor);
            context.fill(bx + 15, btnY, bx + 16, btnY + 16, borderColor);

            String text = String.valueOf(i);
            int tw = this.textRenderer.getWidth(text);
            context.drawText(this.textRenderer, Text.literal(text).copy().formatted(Formatting.BOLD), bx + (16 - tw) / 2, btnY + (16 - 8) / 2, textColor, false);
        }

        if (savedGameOver) {
            context.fill(startX, startY, startX + boardSize, startY + boardSize, 0xCC000000);
            Text gameOverText = Text.translatable("minigame.sudoku.game_over").formatted(Formatting.RED, Formatting.BOLD);
            int textW = this.textRenderer.getWidth(gameOverText);
            context.drawText(this.textRenderer, gameOverText, (this.width - textW) / 2, startY + (boardSize - 8) / 2, 0xFFFF0000, true);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int boardSize = 9 * 16;
        int startX = (this.width - boardSize) / 2;
        int startY = (this.height - boardSize - 24) / 2 + 4;

        if (savedGameOver) {
            if (click.x() >= startX && click.x() < startX + boardSize && click.y() >= startY && click.y() < startY + boardSize) {
                resetGame();
                PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol());
                return true;
            }
            return super.mouseClicked(click, doubled);
        }

        if (click.x() >= startX && click.x() < startX + boardSize && click.y() >= startY && click.y() < startY + boardSize) {
            int c = (int) ((click.x() - startX) / 16);
            int r = (int) ((click.y() - startY) / 16);

            selectedRow = r;
            selectedCol = c;
            highlightedNumber = savedGrid[r][c];

            if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (!savedInitial[r][c] && savedGrid[r][c] != savedSolution[r][c]) {
                    savedGrid[r][c] = 0;
                    highlightedNumber = 0;
                    PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol());
                    return true;
                }
            }

            PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol());
            return true;
        }

        int btnY = startY + boardSize + 8;
        for (int i = 1; i <= 9; i++) {
            int bx = startX + (i - 1) * 16;
            if (click.x() >= bx && click.x() < bx + 16 && click.y() >= btnY && click.y() < btnY + 16) {
                if (!isDigitCompleted(i)) {
                    enterNumber(i);
                }
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        if (savedGameOver || savedWon) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
                resetGame();
                return true;
            }
            return super.keyPressed(input);
        }
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_W) {
            if (selectedRow < 0) {
                selectedRow = 4;
                selectedCol = 4;
            } else {
                selectedRow = Math.max(0, selectedRow - 1);
            }
            highlightedNumber = savedGrid[selectedRow][selectedCol];
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_S) {
            if (selectedRow < 0) {
                selectedRow = 4;
                selectedCol = 4;
            } else {
                selectedRow = Math.min(8, selectedRow + 1);
            }
            highlightedNumber = savedGrid[selectedRow][selectedCol];
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) {
            if (selectedCol < 0) {
                selectedRow = 4;
                selectedCol = 4;
            } else {
                selectedCol = Math.max(0, selectedCol - 1);
            }
            highlightedNumber = savedGrid[selectedRow][selectedCol];
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) {
            if (selectedCol < 0) {
                selectedRow = 4;
                selectedCol = 4;
            } else {
                selectedCol = Math.min(8, selectedCol + 1);
            }
            highlightedNumber = savedGrid[selectedRow][selectedCol];
            return true;
        }
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            enterNumber(keyCode - GLFW.GLFW_KEY_1 + 1);
            return true;
        }
        if (keyCode >= GLFW.GLFW_KEY_KP_1 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            enterNumber(keyCode - GLFW.GLFW_KEY_KP_1 + 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_0 || keyCode == GLFW.GLFW_KEY_KP_0 || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
            if (selectedRow >= 0 && selectedRow < 9 && selectedCol >= 0 && selectedCol < 9) {
                if (!savedInitial[selectedRow][selectedCol] && savedGrid[selectedRow][selectedCol] != savedSolution[selectedRow][selectedCol]) {
                    savedGrid[selectedRow][selectedCol] = 0;
                    highlightedNumber = 0;
                    PlayingSoundManager.playSound(SoundEvent.of(Identifier.ofVanilla("block.wooden_button.click_on")), 2.0F, vol());
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