package org.trivait.minigamesmod.minigame.chess;

import java.util.Objects;

public class ChessPiece {
    private final ChessPieceType type;
    private final ChessColor color;
    private final boolean hasMoved;

    public ChessPiece(ChessPieceType type, ChessColor color) {
        this(type, color, false);
    }

    public ChessPiece(ChessPieceType type, ChessColor color, boolean hasMoved) {
        this.type = type;
        this.color = color;
        this.hasMoved = hasMoved;
    }

    public ChessPieceType getType() {
        return type;
    }

    public ChessColor getColor() {
        return color;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public ChessPiece withMoved(boolean moved) {
        return new ChessPiece(this.type, this.color, moved);
    }

    public ChessPiece withType(ChessPieceType newType) {
        return new ChessPiece(newType, this.color, this.hasMoved);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChessPiece other = (ChessPiece) obj;
        return hasMoved == other.hasMoved && type == other.type && color == other.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, color, hasMoved);
    }
}
