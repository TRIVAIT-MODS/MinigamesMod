package org.trivait.minigamesmod.minigame.chess;

public class MoveRecord {
    private final ChessMove move;
    private final ChessPiece movedPiece;
    private final ChessPiece capturedPiece;
    private final int[] prevEnPassant;
    private final int prevHalfMoveClock;

    public MoveRecord(ChessMove move, ChessPiece movedPiece, ChessPiece capturedPiece, int[] prevEnPassant, int prevHalfMoveClock) {
        this.move = move;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.prevEnPassant = prevEnPassant;
        this.prevHalfMoveClock = prevHalfMoveClock;
    }

    public ChessMove getMove() {
        return move;
    }

    public ChessPiece getMovedPiece() {
        return movedPiece;
    }

    public ChessPiece getCapturedPiece() {
        return capturedPiece;
    }

    public int[] getPrevEnPassant() {
        return prevEnPassant;
    }

    public int getPrevHalfMoveClock() {
        return prevHalfMoveClock;
    }
}
