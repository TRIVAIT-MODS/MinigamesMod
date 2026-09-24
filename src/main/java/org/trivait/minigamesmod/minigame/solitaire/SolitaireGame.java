package org.trivait.minigamesmod.minigame.solitaire;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public final class SolitaireGame {
    public enum Suit {
        CLUBS("clubs", false),
        DIAMONDS("diamond", true),
        HEARTS("heart", true),
        SPADES("spades", false);

        public final String textureName;
        public final boolean red;

        Suit(String textureName, boolean red) {
            this.textureName = textureName;
            this.red = red;
        }
    }

    public static final class Card {
        public final Suit suit;
        public final int rank;
        public boolean faceUp;

        Card(Suit suit, int rank) {
            this.suit = suit;
            this.rank = rank;
        }

        public boolean isRed() {
            return suit.red;
        }
    }

    public final List<List<Card>> tableau = new ArrayList<>(7);
    public final List<Card> stock = new ArrayList<>();
    public final List<Card> waste = new ArrayList<>();
    public final int[] foundations = new int[4];
    public final int[] foundationSuits = {-1, -1, -1, -1};
    private final Deque<State> undo = new ArrayDeque<>();
    public boolean won;

    public SolitaireGame() {
        reset();
    }

    public void reset() {
        tableau.clear();
        stock.clear();
        waste.clear();
        undo.clear();
        Arrays.fill(foundations, 0);
        Arrays.fill(foundationSuits, -1);
        won = false;
        List<Card> deck = new ArrayList<>(52);
        for (Suit suit : Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck, new Random());
        for (int column = 0; column < 7; column++) {
            List<Card> pile = new ArrayList<>();
            for (int row = 0; row <= column; row++) {
                Card card = deck.remove(deck.size() - 1);
                card.faceUp = (row == column);
                pile.add(card);
            }
            tableau.add(pile);
        }
        stock.addAll(deck);
    }

    public void draw() {
        save();
        if (!stock.isEmpty()) {
            Card card = stock.remove(stock.size() - 1);
            card.faceUp = true;
            waste.add(card);
        } else if (!waste.isEmpty()) {
            while (!waste.isEmpty()) {
                Card card = waste.remove(waste.size() - 1);
                card.faceUp = false;
                stock.add(card);
            }
        } else {
            undo.pop();
        }
    }

    public boolean flip(int column) {
        List<Card> pile = tableau.get(column);
        if (pile.isEmpty() || pile.get(pile.size() - 1).faceUp) {
            return false;
        }
        save();
        pile.get(pile.size() - 1).faceUp = true;
        return true;
    }

    public int findFoundationFor(Card card) {
        if (card == null) {
            return -1;
        }
        for (int i = 0; i < 4; i++) {
            if (foundations[i] > 0 && foundationSuits[i] == card.suit.ordinal() && foundations[i] + 1 == card.rank) {
                return i;
            }
        }
        if (card.rank == 1) {
            for (int i = 0; i < 4; i++) {
                if (foundations[i] == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean canPlaceOnFoundation(Card card, int foundation) {
        if (card == null || foundation < 0 || foundation >= 4) {
            return false;
        }
        if (foundations[foundation] == 0) {
            return card.rank == 1;
        }
        return foundationSuits[foundation] == card.suit.ordinal() && foundations[foundation] + 1 == card.rank;
    }

    public boolean moveWasteToTableau(int column) {
        if (waste.isEmpty() || !canPlaceOnTableau(waste.get(waste.size() - 1), column)) {
            return false;
        }
        save();
        tableau.get(column).add(waste.remove(waste.size() - 1));
        checkWin();
        return true;
    }

    public boolean moveWasteToFoundation() {
        if (waste.isEmpty()) {
            return false;
        }
        int target = findFoundationFor(waste.get(waste.size() - 1));
        if (target == -1) {
            return false;
        }
        return moveWasteToFoundation(target);
    }

    public boolean moveWasteToFoundation(int foundation) {
        if (waste.isEmpty()) {
            return false;
        }
        Card card = waste.get(waste.size() - 1);
        if (!canPlaceOnFoundation(card, foundation)) {
            return false;
        }
        save();
        waste.remove(waste.size() - 1);
        foundations[foundation] = card.rank;
        foundationSuits[foundation] = card.suit.ordinal();
        checkWin();
        return true;
    }

    public boolean moveTableauToFoundation(int column) {
        List<Card> pile = tableau.get(column);
        if (pile.isEmpty()) {
            return false;
        }
        Card card = pile.get(pile.size() - 1);
        if (!card.faceUp) {
            return false;
        }
        int target = findFoundationFor(card);
        if (target == -1) {
            return false;
        }
        return moveTableauToFoundation(column, target);
    }

    public boolean moveTableauToFoundation(int column, int foundation) {
        List<Card> pile = tableau.get(column);
        if (pile.isEmpty()) {
            return false;
        }
        Card card = pile.get(pile.size() - 1);
        if (!card.faceUp || !canPlaceOnFoundation(card, foundation)) {
            return false;
        }
        save();
        pile.remove(pile.size() - 1);
        foundations[foundation] = card.rank;
        foundationSuits[foundation] = card.suit.ordinal();
        flipAfterMove(pile);
        checkWin();
        return true;
    }

    public boolean moveTableauToTableau(int from, int cardIndex, int to) {
        if (from == to) {
            return false;
        }
        List<Card> source = tableau.get(from);
        if (cardIndex < 0 || cardIndex >= source.size() || !canMoveRun(source, cardIndex) || !canPlaceOnTableau(source.get(cardIndex), to)) {
            return false;
        }
        save();
        tableau.get(to).addAll(new ArrayList<>(source.subList(cardIndex, source.size())));
        source.subList(cardIndex, source.size()).clear();
        flipAfterMove(source);
        checkWin();
        return true;
    }

    public boolean moveFoundationToTableau(int foundation, int column) {
        if (foundation < 0 || foundation >= 4) {
            return false;
        }
        int rank = foundations[foundation];
        if (rank == 0) {
            return false;
        }
        Card card = new Card(Suit.values()[foundationSuits[foundation]], rank);
        card.faceUp = true;
        if (!canPlaceOnTableau(card, column)) {
            return false;
        }
        save();
        foundations[foundation]--;
        if (foundations[foundation] == 0) {
            foundationSuits[foundation] = -1;
        }
        tableau.get(column).add(card);
        return true;
    }

    public boolean undo() {
        if (undo.isEmpty()) {
            return false;
        }
        State state = undo.pop();
        restore(state);
        return true;
    }

    public boolean canMoveRun(List<Card> pile, int index) {
        if (index < 0 || index >= pile.size() || !pile.get(index).faceUp) {
            return false;
        }
        for (int i = index + 1; i < pile.size(); i++) {
            Card upper = pile.get(i - 1);
            Card lower = pile.get(i);
            if (!lower.faceUp || upper.rank != lower.rank + 1 || upper.isRed() == lower.isRed()) {
                return false;
            }
        }
        return true;
    }

    public boolean canPlaceOnTableau(Card card, int column) {
        List<Card> pile = tableau.get(column);
        if (pile.isEmpty()) {
            return card.rank == 13;
        }
        Card target = pile.get(pile.size() - 1);
        return target.faceUp && target.rank == card.rank + 1 && target.isRed() != card.isRed();
    }

    private void flipAfterMove(List<Card> pile) {
        if (!pile.isEmpty() && !pile.get(pile.size() - 1).faceUp) {
            pile.get(pile.size() - 1).faceUp = true;
        }
    }

    private void checkWin() {
        won = foundations[0] == 13 && foundations[1] == 13 && foundations[2] == 13 && foundations[3] == 13;
    }

    private void save() {
        undo.push(new State(this));
    }

    private void restore(State state) {
        tableau.clear();
        stock.clear();
        waste.clear();
        for (List<Card> pile : state.tableau) {
            tableau.add(copyPile(pile));
        }
        stock.addAll(copyPile(state.stock));
        waste.addAll(copyPile(state.waste));
        System.arraycopy(state.foundations, 0, foundations, 0, 4);
        System.arraycopy(state.foundationSuits, 0, foundationSuits, 0, 4);
        won = state.won;
    }

    private static List<Card> copyPile(List<Card> source) {
        List<Card> copy = new ArrayList<>(source.size());
        for (Card card : source) {
            Card clone = new Card(card.suit, card.rank);
            clone.faceUp = card.faceUp;
            copy.add(clone);
        }
        return copy;
    }

    private static final class State {
        private final List<List<Card>> tableau = new ArrayList<>(7);
        private final List<Card> stock;
        private final List<Card> waste;
        private final int[] foundations;
        private final int[] foundationSuits;
        private final boolean won;

        private State(SolitaireGame game) {
            for (List<Card> pile : game.tableau) {
                tableau.add(copyPile(pile));
            }
            stock = copyPile(game.stock);
            waste = copyPile(game.waste);
            foundations = game.foundations.clone();
            foundationSuits = game.foundationSuits.clone();
            won = game.won;
        }
    }
}
