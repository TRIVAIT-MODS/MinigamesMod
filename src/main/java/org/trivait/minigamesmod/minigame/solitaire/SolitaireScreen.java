package org.trivait.minigamesmod.minigame.solitaire;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.render.RenderLayer;
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

import java.util.ArrayList;
import java.util.List;

public final class SolitaireScreen extends Screen {
    private static final int BOARD_WIDTH = 310;
    private static final int BOARD_HEIGHT = 260;
    private static final int CARD_WIDTH = 37;
    private static final int CARD_HEIGHT = 49;
    private static final int COLUMN_GAP = 5;
    private static final int COLUMN_STEP = CARD_WIDTH + COLUMN_GAP;
    private static final int DEAL_DELAY = 30;
    private static final int DEAL_DURATION = 140;
    private static final int DRAW_DURATION = 120;
    private static final int RECYCLE_DURATION = 160;
    private static final int AUTO_MOVE_DURATION = 140;

    private final Screen parent;
    private final Solitaire minigame;
    private SolitaireGame game;
    private int boardLeft;
    private int boardTop;

    private int selectedColumn = -1;
    private int selectedIndex = -1;
    private int selectedFoundation = -1;
    private boolean selectedWaste;
    private boolean dragging;
    private int dragX;
    private int dragY;
    private boolean wonTriggered;

    private long lastClickTime = 0;
    private int lastClickColumn = -2;
    private int lastClickIndex = -2;

    private final List<DealStep> dealSteps = new ArrayList<>();
    private long dealStartTime;
    private boolean dealing;

    private boolean drawAnimActive;
    private long drawStartTime;
    private SolitaireGame.Card drawAnimCard;

    private boolean recycleAnimActive;
    private long recycleStartTime;

    private boolean autoMoveActive;
    private long autoMoveStartTime;
    private SolitaireGame.Card autoMoveCard;
    private int autoMoveFromX;
    private int autoMoveFromY;
    private int autoMoveToX;
    private int autoMoveToY;
    private boolean autoMoveFromWaste;
    private int autoMoveFoundation = -1;

    public SolitaireScreen(Screen parent, Solitaire minigame) {
        super(Text.empty());
        this.parent = parent;
        this.minigame = minigame;
    }

    @Override
    protected void init() {
        if (game == null) {
            startGame();
        }
        layout();
        clearChildren();
        ButtonWidget back = TextIconButtonWidget.builder(Text.empty(), button -> close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15).build();
        back.setTooltip(Tooltip.of(Text.translatable("minigame.2048.undo")));
        back.setDimensionsAndPosition(20, 20, 10, 10);
        ButtonWidget restart = TextIconButtonWidget.builder(Text.empty(), button -> restart(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15).build();
        restart.setTooltip(Tooltip.of(Text.translatable("minigame.restart")));
        restart.setDimensionsAndPosition(20, 20, 35, 10);
        addDrawableChild(restart);
        addDrawableChild(back);
        addDrawableChild(new ConfigButton(60, 10, minigame));
    }

    private void startGame() {
        layout();
        game = new SolitaireGame();
        wonTriggered = false;
        clearSelection();
        drawAnimActive = false;
        recycleAnimActive = false;
        autoMoveActive = false;
        setupDeal();
        playSound(SoundEvents.BLOCK_WOOL_PLACE, 1.2f);
    }

    private void setupDeal() {
        dealSteps.clear();
        int step = 0;
        for (int c = 0; c < 7; c++) {
            List<SolitaireGame.Card> pile = game.tableau.get(c);
            for (int r = 0; r < pile.size(); r++) {
                dealSteps.add(new DealStep(pile.get(r), c, r, step * DEAL_DELAY));
                step++;
            }
        }
        dealStartTime = System.currentTimeMillis();
        dealing = true;
    }

    private void restart() {
        clearSelection();
        startGame();
        init();
    }

    private void layout() {
        boardLeft = (width - BOARD_WIDTH) / 2;
        boardTop = (height - BOARD_HEIGHT) / 2;
    }

    private int startX() {
        return boardLeft + 9;
    }

    private int topY() {
        return boardTop + 10;
    }

    private int tableauY() {
        return topY() + CARD_HEIGHT + 8;
    }

    private int stockX() {
        return startX();
    }

    private int wasteX() {
        return startX() + COLUMN_STEP;
    }

    private int foundationX(int slot) {
        return startX() + (3 + slot) * COLUMN_STEP;
    }

    private int columnX(int column) {
        return startX() + column * COLUMN_STEP;
    }

    private int columnAt(int x) {
        int col = (x - startX()) / COLUMN_STEP;
        return (col >= 0 && col < 7) ? col : -1;
    }

    private int cardY(int column, int index) {
        List<SolitaireGame.Card> pile = game.tableau.get(column);
        int y = tableauY();
        int faceDownStep = 10;
        int faceUpStep = 15;
        int totalEstimated = 0;
        for (int i = 0; i < pile.size() - 1; i++) {
            totalEstimated += pile.get(i).faceUp ? faceUpStep : faceDownStep;
        }
        int available = BOARD_HEIGHT - 12 - (tableauY() - boardTop) - CARD_HEIGHT;
        if (totalEstimated > available && totalEstimated > 0) {
            float factor = (float) available / totalEstimated;
            faceDownStep = Math.max(4, Math.round(faceDownStep * factor));
            faceUpStep = Math.max(6, Math.round(faceUpStep * factor));
        }
        for (int i = 0; i < index; i++) {
            y += pile.get(i).faceUp ? faceUpStep : faceDownStep;
        }
        return y;
    }

    private int cardAt(int column, int mouseY) {
        List<SolitaireGame.Card> pile = game.tableau.get(column);
        if (pile.isEmpty() || mouseY < tableauY()) {
            return -1;
        }
        for (int i = pile.size() - 1; i >= 0; i--) {
            int cy = cardY(column, i);
            if (selectedColumn == column && !dragging && i >= selectedIndex) {
                cy -= 3;
            }
            if (mouseY >= cy && mouseY < cy + CARD_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    private boolean inside(int x, int y, int left, int top) {
        return x >= left && x < left + CARD_WIDTH && y >= top && y < top + CARD_HEIGHT;
    }

    private boolean selected() {
        return selectedColumn >= 0 || selectedWaste || selectedFoundation >= 0;
    }

    private void clearSelection() {
        selectedColumn = -1;
        selectedIndex = -1;
        selectedFoundation = -1;
        selectedWaste = false;
        dragging = false;
    }

    private float volume() {
        return Math.max(0.1f, PlayingSoundManager.vol(MinigameRegistry.getConfig(SolitaireVisibleConfig.class).volume));
    }

    private void playSound(SoundEvent event, float pitch) {
        PlayingSoundManager.playSound(event, pitch, volume());
    }

    private void startAutoMove(SolitaireGame.Card card, int fromX, int fromY, int toX, int toY, boolean fromWaste, int foundationSlot) {
        autoMoveActive = true;
        autoMoveStartTime = System.currentTimeMillis();
        autoMoveCard = card;
        autoMoveFromX = fromX;
        autoMoveFromY = fromY;
        autoMoveToX = toX;
        autoMoveToY = toY;
        autoMoveFromWaste = fromWaste;
        autoMoveFoundation = foundationSlot;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (game.won) {
            return super.mouseClicked(click, doubled);
        }
        if (dealing) {
            dealing = false;
        }
        layout();
        int x = (int) click.x();
        int y = (int) click.y();

        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (inside(x, y, wasteX(), topY())) {
                if (!game.waste.isEmpty()) {
                    SolitaireGame.Card topCard = game.waste.get(game.waste.size() - 1);
                    int target = game.findFoundationFor(topCard);
                    if (target != -1 && game.moveWasteToFoundation(target)) {
                        startAutoMove(topCard, wasteX(), topY(), foundationX(target), topY(), true, target);
                        playSound(SoundEvents.BLOCK_WOOL_PLACE, 1.1f);
                        clearSelection();
                        checkWon();
                        return true;
                    }
                }
            }
            int column = columnAt(x);
            if (column >= 0 && y >= tableauY()) {
                List<SolitaireGame.Card> pile = game.tableau.get(column);
                if (!pile.isEmpty() && pile.get(pile.size() - 1).faceUp) {
                    SolitaireGame.Card topCard = pile.get(pile.size() - 1);
                    int target = game.findFoundationFor(topCard);
                    int cy = cardY(column, pile.size() - 1);
                    if (target != -1 && game.moveTableauToFoundation(column, target)) {
                        startAutoMove(topCard, columnX(column), cy, foundationX(target), topY(), false, target);
                        playSound(SoundEvents.BLOCK_WOOL_PLACE, 1.1f);
                        clearSelection();
                        checkWon();
                        return true;
                    }
                }
            }
            return true;
        }

        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1) {
            return super.mouseClicked(click, doubled);
        }

        if (inside(x, y, stockX(), topY())) {
            long now = System.currentTimeMillis();
            if (game.stock.isEmpty() && !game.waste.isEmpty()) {
                recycleAnimActive = true;
                recycleStartTime = now;
                game.draw();
                playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 1.1f);
            } else if (!game.stock.isEmpty()) {
                game.draw();
                drawAnimActive = true;
                drawStartTime = now;
                drawAnimCard = game.waste.get(game.waste.size() - 1);
                playSound(SoundEvents.BLOCK_WOOL_PLACE, 1.3f);
            }
            clearSelection();
            return true;
        }

        long now = System.currentTimeMillis();

        if (inside(x, y, wasteX(), topY())) {
            if (!game.waste.isEmpty()) {
                boolean isDouble = (now - lastClickTime < 300 && lastClickColumn == -1);
                lastClickTime = now;
                lastClickColumn = -1;
                lastClickIndex = 0;
                if (isDouble) {
                    SolitaireGame.Card topCard = game.waste.get(game.waste.size() - 1);
                    int target = game.findFoundationFor(topCard);
                    if (target != -1 && game.moveWasteToFoundation(target)) {
                        startAutoMove(topCard, wasteX(), topY(), foundationX(target), topY(), true, target);
                        playSound(SoundEvents.BLOCK_WOOL_PLACE, 1.1f);
                        clearSelection();
                        checkWon();
                        return true;
                    }
                }
                if (selectedWaste) {
                    clearSelection();
                } else {
                    clearSelection();
                    selectedWaste = true;
                    playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.8f);
                }
            }
            return true;
        }

        for (int slot = 0; slot < 4; slot++) {
            if (inside(x, y, foundationX(slot), topY())) {
                if (selected()) {
                    dropFoundation(slot);
                } else if (game.foundations[slot] > 0) {
                    clearSelection();
                    selectedFoundation = slot;
                    playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.8f);
                }
                return true;
            }
        }

        int column = columnAt(x);
        if (column >= 0 && y >= tableauY()) {
            if (selected()) {
                dropTableau(column);
                return true;
            }
            int index = cardAt(column, y);
            if (index >= 0) {
                SolitaireGame.Card card = game.tableau.get(column).get(index);
                if (!card.faceUp) {
                    if (index == game.tableau.get(column).size() - 1) {
                        game.flip(column);
                        playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.5f);
                    }
                } else if (game.canMoveRun(game.tableau.get(column), index)) {
                    boolean isDouble = (now - lastClickTime < 300 && lastClickColumn == column && lastClickIndex == index);
                    lastClickTime = now;
                    lastClickColumn = column;
                    lastClickIndex = index;
                    if (isDouble && index == game.tableau.get(column).size() - 1) {
                        int target = game.findFoundationFor(card);
                        int cy = cardY(column, index);
                        if (target != -1 && game.moveTableauToFoundation(column, target)) {
                            startAutoMove(card, columnX(column), cy, foundationX(target), topY(), false, target);
                            playSound(SoundEvents.BLOCK_WOOL_PLACE, 1.1f);
                            clearSelection();
                            checkWon();
                            return true;
                        }
                    }
                    clearSelection();
                    selectedColumn = column;
                    selectedIndex = index;
                    playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.8f);
                }
            }
            return true;
        }

        clearSelection();
        return super.mouseClicked(click, doubled);
    }

    private void dropTableau(int target) {
        boolean moved = selectedWaste ? game.moveWasteToTableau(target)
                : selectedFoundation >= 0 ? game.moveFoundationToTableau(selectedFoundation, target)
                : selectedColumn >= 0 ? game.moveTableauToTableau(selectedColumn, selectedIndex, target) : false;
        if (moved) {
            playSound(SoundEvents.BLOCK_WOOL_PLACE, 1.1f);
            checkWon();
        } else if (dragging) {
            playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0f);
        }
        clearSelection();
    }

    private void dropFoundation(int target) {
        boolean moved = selectedWaste ? game.moveWasteToFoundation(target)
                : selectedColumn >= 0 ? game.moveTableauToFoundation(selectedColumn, target) : false;
        if (moved) {
            playSound(SoundEvents.BLOCK_WOOL_PLACE, 1.1f);
            checkWon();
        } else if (dragging) {
            playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0f);
        }
        clearSelection();
    }

    private void checkWon() {
        if (game.won && !wonTriggered) {
            wonTriggered = true;
            minigame.onWin();
            minigame.getLeaderboard().doPost(1);
        }
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1 && selected()) {
            dragging = true;
            dragX = (int) click.x();
            dragY = (int) click.y();
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1 && dragging) {
            int x = (int) click.x();
            int y = (int) click.y();
            int column = columnAt(x);
            if (y >= tableauY() && column >= 0) {
                dropTableau(column);
            } else if (y >= topY() && y < topY() + CARD_HEIGHT) {
                boolean dropped = false;
                for (int slot = 0; slot < 4; slot++) {
                    if (x >= foundationX(slot) && x < foundationX(slot) + CARD_WIDTH) {
                        dropFoundation(slot);
                        dropped = true;
                        break;
                    }
                }
                if (!dropped) {
                    clearSelection();
                }
            } else {
                clearSelection();
            }
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_Z && (input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0) {
            if (dealing) {
                dealing = false;
            }
            if (game.undo()) {
                clearSelection();
                drawAnimActive = false;
                recycleAnimActive = false;
                autoMoveActive = false;
                playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.3f);
            }
            return true;
        }
        return super.keyPressed(input);
    }

    private static int lerp(int from, int to, float progress) {
        return Math.round(from + (to - from) * progress);
    }

    private DealStep findDealStep(int column, int index) {
        for (DealStep step : dealSteps) {
            if (step.column == column && step.index == index) {
                return step;
            }
        }
        return null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        layout();
        long now = System.currentTimeMillis();

        if (drawAnimActive && now - drawStartTime >= DRAW_DURATION) {
            drawAnimActive = false;
            drawAnimCard = null;
        }

        if (recycleAnimActive && now - recycleStartTime >= RECYCLE_DURATION) {
            recycleAnimActive = false;
        }

        if (autoMoveActive && now - autoMoveStartTime >= AUTO_MOVE_DURATION) {
            autoMoveActive = false;
            autoMoveCard = null;
            autoMoveFoundation = -1;
        }

        if (dealing && now - dealStartTime >= dealSteps.size() * DEAL_DELAY + DEAL_DURATION) {
            dealing = false;
        }

        context.drawTexture(RenderPipelines.GUI_TEXTURED, SolitaireTextures.GUI_TEXTURE, boardLeft, boardTop, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, BOARD_WIDTH, BOARD_HEIGHT);

        slot(context, stockX(), topY());
        if (!game.stock.isEmpty() || recycleAnimActive) {
            texture(context, SolitaireTextures.CLOSED_CARD, stockX(), topY());
        }

        slot(context, wasteX(), topY());
        if (!game.waste.isEmpty() && !recycleAnimActive) {
            if (drawAnimActive) {
                if (game.waste.size() >= 2) {
                    card(context, game.waste.get(game.waste.size() - 2), wasteX(), topY());
                }
            } else if (dragging && selectedWaste) {
                if (game.waste.size() >= 2) {
                    card(context, game.waste.get(game.waste.size() - 2), wasteX(), topY());
                }
            } else if (autoMoveActive && autoMoveFromWaste) {
                if (!game.waste.isEmpty()) {
                    card(context, game.waste.get(game.waste.size() - 1), wasteX(), topY());
                }
            } else {
                int wy = (selectedWaste && !dragging) ? topY() - 3 : topY();
                card(context, game.waste.get(game.waste.size() - 1), wasteX(), wy);
            }
        }

        for (int slot = 0; slot < 4; slot++) {
            slot(context, foundationX(slot), topY());
            int rank = game.foundations[slot];
            if (rank > 0) {
                SolitaireGame.Suit suit = SolitaireGame.Suit.values()[game.foundationSuits[slot]];
                if (dragging && selectedFoundation == slot) {
                    if (rank > 1) {
                        card(context, card(suit, rank - 1), foundationX(slot), topY());
                    }
                } else if (autoMoveActive && autoMoveFoundation == slot) {
                    if (rank > 1) {
                        card(context, card(suit, rank - 1), foundationX(slot), topY());
                    }
                } else {
                    int fy = (selectedFoundation == slot && !dragging) ? topY() - 3 : topY();
                    card(context, card(suit, rank), foundationX(slot), fy);
                }
            }
        }

        for (int col = 0; col < 7; col++) {
            slot(context, columnX(col), tableauY());
            List<SolitaireGame.Card> pile = game.tableau.get(col);
            for (int i = 0; i < pile.size(); i++) {
                if (dealing) {
                    DealStep step = findDealStep(col, i);
                    if (step != null && now < dealStartTime + step.startMs + DEAL_DURATION) {
                        continue;
                    }
                }
                if (dragging && selectedColumn == col && i >= selectedIndex) {
                    continue;
                }
                int y = cardY(col, i);
                if (selectedColumn == col && !dragging && i >= selectedIndex) {
                    y -= 3;
                }
                SolitaireGame.Card current = pile.get(i);
                if (current.faceUp) {
                    card(context, current, columnX(col), y);
                } else {
                    texture(context, SolitaireTextures.CLOSED_CARD, columnX(col), y);
                }
            }
        }

        if (dealing) {
            long elapsed = now - dealStartTime;
            for (DealStep step : dealSteps) {
                if (elapsed >= step.startMs && elapsed < step.startMs + DEAL_DURATION) {
                    float p = (float) (elapsed - step.startMs) / DEAL_DURATION;
                    float t = p * p * (3 - 2 * p);
                    int x = lerp(stockX(), columnX(step.column), t);
                    int y = lerp(topY(), cardY(step.column, step.index), t);
                    if (!step.soundPlayed) {
                        step.soundPlayed = true;
                        playSound(SoundEvents.BLOCK_WOOL_PLACE, 0.9f + step.index * 0.05f);
                    }
                    texture(context, SolitaireTextures.CLOSED_CARD, x, y);
                }
            }
        }

        if (drawAnimActive && drawAnimCard != null) {
            float p = Math.min(1.0f, (float) (now - drawStartTime) / DRAW_DURATION);
            float t = p * p * (3 - 2 * p);
            int x = lerp(stockX(), wasteX(), t);
            card(context, drawAnimCard, x, topY());
        }

        if (recycleAnimActive) {
            float p = Math.min(1.0f, (float) (now - recycleStartTime) / RECYCLE_DURATION);
            float t = p * p * (3 - 2 * p);
            int x = lerp(wasteX(), stockX(), t);
            texture(context, SolitaireTextures.CLOSED_CARD, x, topY());
        }

        if (autoMoveActive && autoMoveCard != null) {
            float p = Math.min(1.0f, (float) (now - autoMoveStartTime) / AUTO_MOVE_DURATION);
            float t = p * p * (3 - 2 * p);
            int x = lerp(autoMoveFromX, autoMoveToX, t);
            int y = lerp(autoMoveFromY, autoMoveToY, t);
            card(context, autoMoveCard, x, y);
        }

        if (dragging) {
            drawDragged(context);
        }

        if (game.won) {
            Text winText = Text.translatable("minigame.solitaire.win").formatted(Formatting.GOLD, Formatting.BOLD);
            int winWidth = textRenderer.getWidth(winText);
            context.drawText(textRenderer, winText, boardLeft + (BOARD_WIDTH - winWidth) / 2, boardTop + BOARD_HEIGHT - 18, 0xFFFFD700, true);
        }
    }

    private void drawDragged(DrawContext context) {
        if (selectedWaste) {
            int cx = dragX - CARD_WIDTH / 2;
            int cy = dragY - CARD_HEIGHT / 2;
            card(context, game.waste.get(game.waste.size() - 1), cx, cy);
        } else if (selectedFoundation >= 0) {
            int cx = dragX - CARD_WIDTH / 2;
            int cy = dragY - CARD_HEIGHT / 2;
            card(context, card(SolitaireGame.Suit.values()[game.foundationSuits[selectedFoundation]], game.foundations[selectedFoundation]), cx, cy);
        } else if (selectedColumn >= 0) {
            List<SolitaireGame.Card> pile = game.tableau.get(selectedColumn);
            int cx = dragX - CARD_WIDTH / 2;
            int cy = dragY - CARD_HEIGHT / 2;
            for (int i = selectedIndex; i < pile.size(); i++) {
                int cardY = cy + (i - selectedIndex) * 15;
                card(context, pile.get(i), cx, cardY);
            }
        }
    }

    private SolitaireGame.Card card(SolitaireGame.Suit suit, int rank) {
        SolitaireGame.Card card = new SolitaireGame.Card(suit, rank);
        card.faceUp = true;
        return card;
    }

    private void slot(DrawContext context, int x, int y) {
        texture(context, SolitaireTextures.OVERLAY_CARD, x, y);
    }

    private void card(DrawContext context, SolitaireGame.Card card, int x, int y) {
        texture(context, SolitaireTextures.cardTexture(card.suit, card.rank), x, y);
    }

    private void texture(DrawContext context, Identifier texture, int x, int y) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, CARD_WIDTH, CARD_HEIGHT, CARD_WIDTH, CARD_HEIGHT);
    }

    @Override
    public void close() {
        super.close();
        MinecraftClient.getInstance().setScreen(parent);
    }

    private static final class DealStep {
        private final SolitaireGame.Card card;
        private final int column;
        private final int index;
        private final int startMs;
        private boolean soundPlayed;

        private DealStep(SolitaireGame.Card card, int column, int index, int startMs) {
            this.card = card;
            this.column = column;
            this.index = index;
            this.startMs = startMs;
        }
    }
}
