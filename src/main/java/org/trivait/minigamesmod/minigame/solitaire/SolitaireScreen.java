package org.trivait.minigamesmod.minigame.solitaire;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.ModSounds;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.ConfigButton;

import java.util.ArrayList;
import java.util.List;

public final class SolitaireScreen extends Screen {
    private static final int BOARD_WIDTH = 310;
    private static final int BOARD_HEIGHT = 260;
    private static final int GAME_X = 8;
    private static final int GAME_Y = 8;
    private static final int GAME_WIDTH = BOARD_WIDTH - GAME_X * 2;
    private static final int GAME_HEIGHT = BOARD_HEIGHT - GAME_Y * 2;
    private static final int PADDING = 5;
    private static final int CARD_WIDTH = 37;
    private static final int CARD_HEIGHT = 49;
    private static final int MAX_CARD_STEP = 14;
    private static final int COLUMN_GAP = 4;
    private static final int COLUMN_STEP = CARD_WIDTH + COLUMN_GAP;
    private static final int FOUNDATION_START = 128;
    private static final int DEAL_DELAY = 42;
    private static final int DEAL_DURATION = 190;
    private static final int MOVE_DURATION = 150;

    private final Screen parent;
    private final Solitaire minigame;
    private SolitaireGame game;
    private int boardLeft;
    private int boardTop;
    private long lastFrame;
    private long dealTime;
    private Motion motion;
    private ReturnAnimation returnAnimation;
    private final List<DealCard> dealCards = new ArrayList<>();
    private int selectedColumn = -1;
    private int selectedIndex = -1;
    private int cardStep;
    private int selectedFoundation = -1;
    private boolean selectedWaste;
    private boolean dragging;
    private int dragX;
    private int dragY;

    public SolitaireScreen(Screen parent, Solitaire minigame) {
        super(Text.empty());
        this.parent = parent;
        this.minigame = minigame;
    }

    @Override
    protected void init() {
        if (game == null) startGame();
        layout();
        clearChildren();
        ButtonWidget back = TextIconButtonWidget.builder(Text.empty(), button -> close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15).build();
        back.setTooltip(Tooltip.of(Text.translatable("minigame.2048.undo")));
        ButtonWidget restart = TextIconButtonWidget.builder(Text.empty(), button -> restart(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15).build();
        restart.setTooltip(Tooltip.of(Text.translatable("minigame.restart")));
        restart.setDimensionsAndPosition(20, 20, 35, 10);
        addDrawableChild(restart);
        addDrawableChild(back);
        addDrawableChild(new ConfigButton(60, 10, minigame));
    }

    private void startGame() {
        game = new SolitaireGame();
        lastFrame = System.nanoTime();
        dealTime = 0;
        dealCards.clear();
        for (int column = 0; column < game.tableau.size(); column++) {
            for (int index = 0; index < game.tableau.get(column).size(); index++) {
                dealCards.add(new DealCard(game.tableau.get(column).get(index), column, index, dealCards.size() * DEAL_DELAY));
            }
        }
        sound(ModSounds.PUTCARD0);
    }

    private void restart() {
        motion = null;
        returnAnimation = null;
        clearSelection();
        startGame();
        init();
    }

    private void layout() {
        boardLeft = (width - BOARD_WIDTH) / 2;
        boardTop = (height - BOARD_HEIGHT) / 2;
        int tallest = 1;
        if (game != null) for (List<SolitaireGame.Card> pile : game.tableau) tallest = Math.max(tallest, pile.size());
        int available = boardTop + GAME_Y + GAME_HEIGHT - PADDING - tableauY();
        cardStep = tallest == 1 ? MAX_CARD_STEP : Math.min(MAX_CARD_STEP, Math.max(1, (available - CARD_HEIGHT) / (tallest - 1)));
    }

    private int stockX() { return boardLeft + GAME_X + PADDING; }
    private int wasteX() { return stockX() + CARD_WIDTH + COLUMN_GAP; }
    private int topY() { return boardTop + GAME_Y + PADDING; }
    private int tableauY() { return topY() + CARD_HEIGHT + PADDING; }
    private int columnX(int column) { return boardLeft + GAME_X + PADDING + column * COLUMN_STEP; }
    private int foundationX(int slot) { return boardLeft + GAME_X + FOUNDATION_START + slot * COLUMN_STEP; }
    private int columnAt(int x) {
        int column = (x - columnX(0) + COLUMN_GAP / 2) / COLUMN_STEP;
        return column >= 0 && column < 7 ? column : -1;
    }

    private int cardAt(int column, int y) {
        List<SolitaireGame.Card> pile = game.tableau.get(column);
        if (pile.isEmpty() || y < tableauY()) return -1;
        return Math.min(pile.size() - 1, Math.max(0, (y - tableauY()) / cardStep));
    }

    private boolean inside(int x, int y, int left, int top) {
        return x >= left && x < left + CARD_WIDTH && y >= top && y < top + CARD_HEIGHT;
    }

    private boolean selected() { return selectedColumn >= 0 || selectedWaste || selectedFoundation >= 0; }
    private void clearSelection() { selectedColumn = selectedIndex = selectedFoundation = -1; selectedWaste = dragging = false; }
    private boolean animating() { return !dealCards.isEmpty() || returnAnimation != null || motion != null; }
    private float volume() { return Math.max(1.0f, PlayingSoundManager.vol(MinigameRegistry.getConfig(SolitaireVisibleConfig.class).volume)); }
    private void sound(SoundEvent event) { PlayingSoundManager.playSound(event, 1.0f, volume()); }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1 || game.won || animating()) return super.mouseClicked(mouseX, mouseY, button);
        layout();
        int x = (int) mouseX;
        int y = (int) mouseY;
        if (inside(x, y, stockX(), topY())) {
            if (game.stock.isEmpty() && !game.waste.isEmpty()) startReturnAnimation();
            else if (!game.stock.isEmpty()) {
                SolitaireGame.Card previous = game.waste.isEmpty() ? null : game.waste.get(game.waste.size() - 1);
                game.draw();
                motion = new Motion(game.waste.get(game.waste.size() - 1), stockX(), topY(), wasteX(), topY(), previous);
            }
            clearSelection();
            sound(ModSounds.PUTCARD0);
            return true;
        }
        if (inside(x, y, wasteX(), topY())) {
            if (!selected() && !game.waste.isEmpty()) selectedWaste = true;
            else clearSelection();
            return true;
        }
        for (int slot = 0; slot < 4; slot++) {
            if (inside(x, y, foundationX(slot), topY())) {
                if (selected()) dropFoundation(slot);
                else if (game.foundations[slot] > 0) selectedFoundation = slot;
                return true;
            }
        }
        int column = columnAt(x);
        if (column < 0 || y < tableauY()) return super.mouseClicked(mouseX, mouseY, button);
        if (selected()) {
            dropTableau(column);
            return true;
        }
        int index = cardAt(column, y);
        if (index >= 0) {
            SolitaireGame.Card card = game.tableau.get(column).get(index);
            if (!card.faceUp) game.flip(column);
            else if (game.canMoveRun(game.tableau.get(column), index)) {
                selectedColumn = column;
                selectedIndex = index;
            }
        }
        return true;
    }

    private SolitaireGame.Card selectedCard() {
        if (selectedWaste) return game.waste.get(game.waste.size() - 1);
        if (selectedFoundation >= 0) return card(SolitaireGame.Suit.values()[game.foundationSuits[selectedFoundation]], game.foundations[selectedFoundation]);
        return game.tableau.get(selectedColumn).get(selectedIndex);
    }

    private void dropTableau(int target) {
        boolean wasDragging = dragging;
        SolitaireGame.Card moving = selectedCard();
        boolean moved = selectedWaste ? game.moveWasteToTableau(target) : selectedFoundation >= 0 ? game.moveFoundationToTableau(selectedFoundation, target) : game.moveTableauToTableau(selectedColumn, selectedIndex, target);
        if (moved) sound(ModSounds.PUTCARD1);
        if (wasDragging) sound(ModSounds.DRAG_END);
        clearSelection();
    }

    private void dropFoundation(int target) {
        boolean wasDragging = dragging;
        SolitaireGame.Card moving = selectedCard();
        boolean moved = selectedWaste ? game.moveWasteToFoundation(target) : selectedColumn >= 0 && game.moveTableauToFoundation(selectedColumn, target);
        if (moved) sound(ModSounds.PUTCARD2);
        if (wasDragging) sound(ModSounds.DRAG_END);
        clearSelection();
    }

    private void startReturnAnimation() {
        returnAnimation = new ReturnAnimation(new ArrayList<>(game.waste));
        game.draw();
        sound(ModSounds.PUTCARD0);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && selected()) {
            if (!dragging) sound(ModSounds.DRAG_START);
            dragging = true;
            dragX = (int) mouseX;
            dragY = (int) mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && dragging) {
            layout();
            int x = (int) mouseX;
            int y = (int) mouseY;
            int column = columnAt(x);
            if (y >= tableauY() && column >= 0) dropTableau(column);
            else if (y >= topY() && y < topY() + CARD_HEIGHT && x >= foundationX(0) && x < foundationX(3) + CARD_WIDTH) dropFoundation(Math.min(3, Math.max(0, (x - foundationX(0)) / COLUMN_STEP)));
            else {
                sound(ModSounds.DRAG_END);
                clearSelection();
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_Z && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            game.undo();
            clearSelection();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        layout();
        long now = System.nanoTime();
        long elapsed = lastFrame == 0 ? 0 : Math.min(50, (now - lastFrame) / 1_000_000);
        lastFrame = now;
        dealTime += elapsed;
        if (!dealCards.isEmpty() && dealTime >= (dealCards.size() - 1L) * DEAL_DELAY + DEAL_DURATION) dealCards.clear();
        if (motion != null && (motion.elapsed += elapsed) >= MOVE_DURATION) motion = null;
        if (returnAnimation != null && (returnAnimation.elapsed += elapsed) >= MOVE_DURATION + DEAL_DELAY * returnAnimation.cards.size()) returnAnimation = null;
        context.drawTexture(SolitaireTextures.GUI_TEXTURE, boardLeft, boardTop, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, BOARD_WIDTH, BOARD_HEIGHT);
        drawBoard(context);
    }

    private void drawBoard(DrawContext context) {
        slot(context, stockX(), topY());
        if (returnAnimation == null && !game.stock.isEmpty()) texture(context, SolitaireTextures.CLOSED_CARD, stockX(), topY());
        slot(context, wasteX(), topY());
        if (!game.waste.isEmpty() && !selectedWaste && (motion == null || motion.previousCard == null)) card(context, game.waste.get(game.waste.size() - 1), wasteX(), topY());
        for (int slot = 0; slot < 4; slot++) {
            slot(context, foundationX(slot), topY());
            if (game.foundations[slot] > 0 && selectedFoundation != slot && !motionAt(foundationX(slot), topY())) card(context, card(SolitaireGame.Suit.values()[game.foundationSuits[slot]], game.foundations[slot]), foundationX(slot), topY());
        }

        for (int column = 0; column < 7; column++) {
            List<SolitaireGame.Card> pile = game.tableau.get(column);
            slot(context, columnX(column), tableauY());
            for (int index = 0; index < pile.size(); index++) {
                SolitaireGame.Card current = pile.get(index);
                if (dragging && selectedColumn == column && index >= selectedIndex) continue;
                if (motion != null && motion.card == current) continue;
                DealCard deal = findDeal(current);
                if (deal != null && dealTime < deal.start) continue;
                if (deal != null && !deal.soundPlayed) {
                    deal.soundPlayed = true;
                    sound(ModSounds.PUTCARD2);
                }
                int x = columnX(column);
                int y = tableauY() + index * cardStep;
                if (deal != null && dealTime < deal.start + DEAL_DURATION) {
                    float progress = smooth((dealTime - deal.start) / (float) DEAL_DURATION);
                    x = lerp(stockX(), x, progress);
                    y = lerp(topY(), y, progress);
                }
                if (current.faceUp) card(context, current, x, y);
                else texture(context, SolitaireTextures.CLOSED_CARD, x, y);
            }
        }
        if (selectedWaste && !dragging) slot(context, wasteX(), topY());
        if (selectedFoundation >= 0) slot(context, foundationX(selectedFoundation), topY());
        if (dragging) drawDragged(context);
        if (motion != null) drawMotion(context);
        if (returnAnimation != null) drawReturn(context);
        if (game.won) context.drawCenteredTextWithShadow(textRenderer, Text.translatable("minigame.solitaire.win"), boardLeft + BOARD_WIDTH / 2, boardTop + BOARD_HEIGHT - 18, 0xFFFFFF00);
    }

    private void drawDragged(DrawContext context) {
        if (selectedWaste || selectedFoundation >= 0) card(context, selectedCard(), dragX - CARD_WIDTH / 2, dragY - CARD_HEIGHT / 2);
        else for (int index = selectedIndex; index < game.tableau.get(selectedColumn).size(); index++) card(context, game.tableau.get(selectedColumn).get(index), dragX - CARD_WIDTH / 2, dragY - CARD_HEIGHT / 2 + (index - selectedIndex) * cardStep);
    }

    private void drawMotion(DrawContext context) {
        float progress = smooth(motion.elapsed / (float) MOVE_DURATION);
        if (motion.previousCard != null) card(context, motion.previousCard, wasteX(), topY());
        card(context, motion.card, lerp(motion.fromX, motion.toX, progress), lerp(motion.fromY, motion.toY, progress));
    }

    private void drawReturn(DrawContext context) {
        for (int index = 0; index < returnAnimation.cards.size(); index++) {
            float progress = smooth((returnAnimation.elapsed - index * (float) DEAL_DELAY) / MOVE_DURATION);
            if (progress >= 1) continue;
            card(context, returnAnimation.cards.get(index), lerp(wasteX(), stockX(), Math.max(0, progress)), topY());
        }
    }

    private DealCard findDeal(SolitaireGame.Card card) {
        for (DealCard deal : dealCards) if (deal.card == card) return deal;
        return null;
    }

    private boolean motionAt(int x, int y) { return motion != null && motion.toX == x && motion.toY == y; }
    private static int lerp(int from, int to, float progress) { return Math.round(from + (to - from) * progress); }
    private static float smooth(float value) { value = Math.max(0, Math.min(1, value)); return value * value * (3 - 2 * value); }
    private SolitaireGame.Card card(SolitaireGame.Suit suit, int rank) { SolitaireGame.Card card = new SolitaireGame.Card(suit, rank); card.faceUp = true; return card; }
    private void slot(DrawContext context, int x, int y) { texture(context, SolitaireTextures.OVERLAY_CARD, x, y); }
    private void card(DrawContext context, SolitaireGame.Card card, int x, int y) { texture(context, SolitaireTextures.cardTexture(card.suit, card.rank), x, y); }
    private void texture(DrawContext context, Identifier texture, int x, int y) { context.drawTexture(texture, x, y, 0, 0, CARD_WIDTH, CARD_HEIGHT, CARD_WIDTH, CARD_HEIGHT); }

    @Override
    public void close() { super.close(); MinecraftClient.getInstance().setScreen(parent); }

    private static final class DealCard {
        private final SolitaireGame.Card card;
        private final int start;
        private boolean soundPlayed;
        private DealCard(SolitaireGame.Card card, int column, int index, int start) { this.card = card; this.start = start; }
    }

    private static final class Motion {
        private final SolitaireGame.Card card;
        private final SolitaireGame.Card previousCard;
        private final int fromX, fromY, toX, toY;
        private long elapsed;
        private Motion(SolitaireGame.Card card, int fromX, int fromY, int toX, int toY, SolitaireGame.Card previousCard) { this.card = card; this.fromX = fromX; this.fromY = fromY; this.toX = toX; this.toY = toY; this.previousCard = previousCard; }
    }

    private static final class ReturnAnimation {
        private final List<SolitaireGame.Card> cards;
        private long elapsed;
        private ReturnAnimation(List<SolitaireGame.Card> cards) { this.cards = cards; }
    }
}
