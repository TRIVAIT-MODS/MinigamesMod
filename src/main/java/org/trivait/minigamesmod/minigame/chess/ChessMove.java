package org.trivait.minigamesmod.minigame.chess;

import java.util.Objects;

public class ChessMove {
    private final int fromX;
    private final int fromY;
    private final int toX;
    private final int toY;
    private final ChessPieceType promotion;
    private final boolean isEnPassant;
    private final boolean isCastling;

    public ChessMove(int fromX, int fromY, int toX, int toY) {
        this(fromX, fromY, toX, toY, null, false, false);
    }

    public ChessMove(int fromX, int fromY, int toX, int toY, ChessPieceType promotion) {
        this(fromX, fromY, toX, toY, promotion, false, false);
    }

    public ChessMove(int fromX, int fromY, int toX, int toY, ChessPieceType promotion, boolean isEnPassant, boolean isCastling) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.promotion = promotion;
        this.isEnPassant = isEnPassant;
        this.isCastling = isCastling;
    }

    public int getFromX() {
        return fromX;
    }

    public int getFromY() {
        return fromY;
    }

    public int getToX() {
        return toX;
    }

    public int getToY() {
        return toY;
    }

    public ChessPieceType getPromotion() {
        return promotion;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public boolean isCastling() {
        return isCastling;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChessMove other = (ChessMove) obj;
        return fromX == other.fromX && fromY == other.fromY && toX == other.toX && toY == other.toY
                && promotion == other.promotion && isEnPassant == other.isEnPassant && isCastling == other.isCastling;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromX, fromY, toX, toY, promotion, isEnPassant, isCastling);
    }
}
