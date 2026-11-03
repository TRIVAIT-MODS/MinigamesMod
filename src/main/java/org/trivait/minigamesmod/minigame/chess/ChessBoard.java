package org.trivait.minigamesmod.minigame.chess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ChessBoard {
    private final ChessPiece[][] board;
    private ChessColor turn;
    private int[] enPassantSquare;
    private int halfMoveClock;
    private int fullMoveNumber;
    private final List<ChessMove> moveHistory;
    private final Deque<MoveRecord> recordStack;
    private final List<String> positionHistory;
    private final List<ChessPiece> capturedWhitePieces;
    private final List<ChessPiece> capturedBlackPieces;

    public ChessBoard() {
        this.board = new ChessPiece[8][8];
        this.turn = ChessColor.WHITE;
        this.enPassantSquare = null;
        this.halfMoveClock = 0;
        this.fullMoveNumber = 1;
        this.moveHistory = new ArrayList<>();
        this.recordStack = new ArrayDeque<>();
        this.positionHistory = new ArrayList<>();
        this.capturedWhitePieces = new ArrayList<>();
        this.capturedBlackPieces = new ArrayList<>();
        initStartingPosition();
    }

    private ChessBoard(ChessPiece[][] board, ChessColor turn, int[] enPassantSquare, int halfMoveClock, int fullMoveNumber,
                       List<ChessMove> moveHistory, Deque<MoveRecord> recordStack, List<String> positionHistory,
                       List<ChessPiece> capturedWhitePieces, List<ChessPiece> capturedBlackPieces) {
        this.board = new ChessPiece[8][8];
        for (int y = 0; y < 8; y++) {
            System.arraycopy(board[y], 0, this.board[y], 0, 8);
        }
        this.turn = turn;
        this.enPassantSquare = enPassantSquare != null ? enPassantSquare.clone() : null;
        this.halfMoveClock = halfMoveClock;
        this.fullMoveNumber = fullMoveNumber;
        this.moveHistory = new ArrayList<>(moveHistory);
        this.recordStack = new ArrayDeque<>(recordStack);
        this.positionHistory = new ArrayList<>(positionHistory);
        this.capturedWhitePieces = new ArrayList<>(capturedWhitePieces);
        this.capturedBlackPieces = new ArrayList<>(capturedBlackPieces);
    }

    public void initStartingPosition() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                board[y][x] = null;
            }
        }
        board[0][0] = new ChessPiece(ChessPieceType.ROOK, ChessColor.BLACK);
        board[0][1] = new ChessPiece(ChessPieceType.KNIGHT, ChessColor.BLACK);
        board[0][2] = new ChessPiece(ChessPieceType.BISHOP, ChessColor.BLACK);
        board[0][3] = new ChessPiece(ChessPieceType.QUEEN, ChessColor.BLACK);
        board[0][4] = new ChessPiece(ChessPieceType.KING, ChessColor.BLACK);
        board[0][5] = new ChessPiece(ChessPieceType.BISHOP, ChessColor.BLACK);
        board[0][6] = new ChessPiece(ChessPieceType.KNIGHT, ChessColor.BLACK);
        board[0][7] = new ChessPiece(ChessPieceType.ROOK, ChessColor.BLACK);

        for (int x = 0; x < 8; x++) {
            board[1][x] = new ChessPiece(ChessPieceType.PAWN, ChessColor.BLACK);
            board[6][x] = new ChessPiece(ChessPieceType.PAWN, ChessColor.WHITE);
        }

        board[7][0] = new ChessPiece(ChessPieceType.ROOK, ChessColor.WHITE);
        board[7][1] = new ChessPiece(ChessPieceType.KNIGHT, ChessColor.WHITE);
        board[7][2] = new ChessPiece(ChessPieceType.BISHOP, ChessColor.WHITE);
        board[7][3] = new ChessPiece(ChessPieceType.QUEEN, ChessColor.WHITE);
        board[7][4] = new ChessPiece(ChessPieceType.KING, ChessColor.WHITE);
        board[7][5] = new ChessPiece(ChessPieceType.BISHOP, ChessColor.WHITE);
        board[7][6] = new ChessPiece(ChessPieceType.KNIGHT, ChessColor.WHITE);
        board[7][7] = new ChessPiece(ChessPieceType.ROOK, ChessColor.WHITE);

        turn = ChessColor.WHITE;
        enPassantSquare = null;
        halfMoveClock = 0;
        fullMoveNumber = 1;
        moveHistory.clear();
        recordStack.clear();
        positionHistory.clear();
        capturedWhitePieces.clear();
        capturedBlackPieces.clear();
        positionHistory.add(toPositionKey());
    }

    public ChessBoard copy() {
        return new ChessBoard(board, turn, enPassantSquare, halfMoveClock, fullMoveNumber,
                moveHistory, recordStack, positionHistory, capturedWhitePieces, capturedBlackPieces);
    }

    public ChessPiece getPiece(int x, int y) {
        if (!isValidCoord(x, y)) return null;
        return board[y][x];
    }

    public void setPiece(int x, int y, ChessPiece piece) {
        if (isValidCoord(x, y)) {
            board[y][x] = piece;
        }
    }

    public boolean isValidCoord(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    public ChessColor getTurn() {
        return turn;
    }

    public List<ChessMove> getMoveHistory() {
        return moveHistory;
    }

    public List<ChessPiece> getCapturedWhitePieces() {
        return capturedWhitePieces;
    }

    public List<ChessPiece> getCapturedBlackPieces() {
        return capturedBlackPieces;
    }

    public int[] getEnPassantSquare() {
        return enPassantSquare;
    }

    public int getHalfMoveClock() {
        return halfMoveClock;
    }

    public int getFullMoveNumber() {
        return fullMoveNumber;
    }

    public int[] findKing(ChessColor color) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                ChessPiece piece = board[y][x];
                if (piece != null && piece.getColor() == color && piece.getType() == ChessPieceType.KING) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    public boolean isInCheck(ChessColor color) {
        int[] kingPos = findKing(color);
        if (kingPos == null) return false;
        return isSquareAttacked(kingPos[0], kingPos[1], color.opposite());
    }

    public boolean isSquareAttacked(int targetX, int targetY, ChessColor byColor) {
        if (byColor == ChessColor.WHITE) {
            int py = targetY + 1;
            if (isValidCoord(targetX - 1, py)) {
                ChessPiece p = board[py][targetX - 1];
                if (p != null && p.getColor() == byColor && p.getType() == ChessPieceType.PAWN) return true;
            }
            if (isValidCoord(targetX + 1, py)) {
                ChessPiece p = board[py][targetX + 1];
                if (p != null && p.getColor() == byColor && p.getType() == ChessPieceType.PAWN) return true;
            }
        } else {
            int py = targetY - 1;
            if (isValidCoord(targetX - 1, py)) {
                ChessPiece p = board[py][targetX - 1];
                if (p != null && p.getColor() == byColor && p.getType() == ChessPieceType.PAWN) return true;
            }
            if (isValidCoord(targetX + 1, py)) {
                ChessPiece p = board[py][targetX + 1];
                if (p != null && p.getColor() == byColor && p.getType() == ChessPieceType.PAWN) return true;
            }
        }

        int[][] knightOffsets = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
        for (int[] offset : knightOffsets) {
            int nx = targetX + offset[0];
            int ny = targetY + offset[1];
            if (isValidCoord(nx, ny)) {
                ChessPiece p = board[ny][nx];
                if (p != null && p.getColor() == byColor && p.getType() == ChessPieceType.KNIGHT) return true;
            }
        }

        int[][] kingOffsets = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        for (int[] offset : kingOffsets) {
            int kx = targetX + offset[0];
            int ky = targetY + offset[1];
            if (isValidCoord(kx, ky)) {
                ChessPiece p = board[ky][kx];
                if (p != null && p.getColor() == byColor && p.getType() == ChessPieceType.KING) return true;
            }
        }

        int[][] orthogonalDirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : orthogonalDirs) {
            int step = 1;
            while (true) {
                int sx = targetX + dir[0] * step;
                int sy = targetY + dir[1] * step;
                if (!isValidCoord(sx, sy)) break;
                ChessPiece p = board[sy][sx];
                if (p != null) {
                    if (p.getColor() == byColor && (p.getType() == ChessPieceType.ROOK || p.getType() == ChessPieceType.QUEEN)) {
                        return true;
                    }
                    break;
                }
                step++;
            }
        }

        int[][] diagonalDirs = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] dir : diagonalDirs) {
            int step = 1;
            while (true) {
                int sx = targetX + dir[0] * step;
                int sy = targetY + dir[1] * step;
                if (!isValidCoord(sx, sy)) break;
                ChessPiece p = board[sy][sx];
                if (p != null) {
                    if (p.getColor() == byColor && (p.getType() == ChessPieceType.BISHOP || p.getType() == ChessPieceType.QUEEN)) {
                        return true;
                    }
                    break;
                }
                step++;
            }
        }

        return false;
    }

    public List<ChessMove> generateLegalMoves(ChessColor color) {
        List<ChessMove> legalMoves = new ArrayList<>();
        List<ChessMove> pseudoMoves = generatePseudoLegalMoves(color);
        for (ChessMove move : pseudoMoves) {
            makeMove(move);
            if (!isInCheck(color)) {
                legalMoves.add(move);
            }
            undoMove();
        }
        return legalMoves;
    }

    public List<ChessMove> generateLegalMovesForPiece(int x, int y) {
        List<ChessMove> legalMoves = new ArrayList<>();
        ChessPiece piece = getPiece(x, y);
        if (piece == null || piece.getColor() != turn) return legalMoves;
        List<ChessMove> pseudoMoves = new ArrayList<>();
        generatePiecePseudoMoves(x, y, piece, pseudoMoves);
        for (ChessMove move : pseudoMoves) {
            makeMove(move);
            if (!isInCheck(piece.getColor())) {
                legalMoves.add(move);
            }
            undoMove();
        }
        return legalMoves;
    }

    public List<ChessMove> generatePseudoLegalMoves(ChessColor color) {
        List<ChessMove> moves = new ArrayList<>();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                ChessPiece piece = board[y][x];
                if (piece != null && piece.getColor() == color) {
                    generatePiecePseudoMoves(x, y, piece, moves);
                }
            }
        }
        return moves;
    }

    private void generatePiecePseudoMoves(int x, int y, ChessPiece piece, List<ChessMove> moves) {
        switch (piece.getType()) {
            case PAWN:
                generatePawnMoves(x, y, piece, moves);
                break;
            case KNIGHT:
                generateKnightMoves(x, y, piece, moves);
                break;
            case BISHOP:
                generateSlidingMoves(x, y, piece, new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}}, moves);
                break;
            case ROOK:
                generateSlidingMoves(x, y, piece, new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}, moves);
                break;
            case QUEEN:
                generateSlidingMoves(x, y, piece, new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}}, moves);
                break;
            case KING:
                generateKingMoves(x, y, piece, moves);
                break;
        }
    }

    private void generatePawnMoves(int x, int y, ChessPiece piece, List<ChessMove> moves) {
        int dir = piece.getColor() == ChessColor.WHITE ? -1 : 1;
        int startRank = piece.getColor() == ChessColor.WHITE ? 6 : 1;
        int promoRank = piece.getColor() == ChessColor.WHITE ? 0 : 7;

        int forwardY = y + dir;
        if (isValidCoord(x, forwardY) && board[forwardY][x] == null) {
            if (forwardY == promoRank) {
                moves.add(new ChessMove(x, y, x, forwardY, ChessPieceType.QUEEN));
                moves.add(new ChessMove(x, y, x, forwardY, ChessPieceType.ROOK));
                moves.add(new ChessMove(x, y, x, forwardY, ChessPieceType.BISHOP));
                moves.add(new ChessMove(x, y, x, forwardY, ChessPieceType.KNIGHT));
            } else {
                moves.add(new ChessMove(x, y, x, forwardY));
                int doubleForwardY = y + 2 * dir;
                if (y == startRank && board[doubleForwardY][x] == null) {
                    moves.add(new ChessMove(x, y, x, doubleForwardY));
                }
            }
        }

        int[] captureCols = {x - 1, x + 1};
        for (int cx : captureCols) {
            if (isValidCoord(cx, forwardY)) {
                ChessPiece target = board[forwardY][cx];
                if (target != null && target.getColor() != piece.getColor()) {
                    if (forwardY == promoRank) {
                        moves.add(new ChessMove(x, y, cx, forwardY, ChessPieceType.QUEEN));
                        moves.add(new ChessMove(x, y, cx, forwardY, ChessPieceType.ROOK));
                        moves.add(new ChessMove(x, y, cx, forwardY, ChessPieceType.BISHOP));
                        moves.add(new ChessMove(x, y, cx, forwardY, ChessPieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(x, y, cx, forwardY));
                    }
                } else if (enPassantSquare != null && cx == enPassantSquare[0] && forwardY == enPassantSquare[1]) {
                    moves.add(new ChessMove(x, y, cx, forwardY, null, true, false));
                }
            }
        }
    }

    private void generateKnightMoves(int x, int y, ChessPiece piece, List<ChessMove> moves) {
        int[][] offsets = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
        for (int[] offset : offsets) {
            int tx = x + offset[0];
            int ty = y + offset[1];
            if (isValidCoord(tx, ty)) {
                ChessPiece target = board[ty][tx];
                if (target == null || target.getColor() != piece.getColor()) {
                    moves.add(new ChessMove(x, y, tx, ty));
                }
            }
        }
    }

    private void generateSlidingMoves(int x, int y, ChessPiece piece, int[][] directions, List<ChessMove> moves) {
        for (int[] dir : directions) {
            int step = 1;
            while (true) {
                int tx = x + dir[0] * step;
                int ty = y + dir[1] * step;
                if (!isValidCoord(tx, ty)) break;
                ChessPiece target = board[ty][tx];
                if (target == null) {
                    moves.add(new ChessMove(x, y, tx, ty));
                } else {
                    if (target.getColor() != piece.getColor()) {
                        moves.add(new ChessMove(x, y, tx, ty));
                    }
                    break;
                }
                step++;
            }
        }
    }

    private void generateKingMoves(int x, int y, ChessPiece piece, List<ChessMove> moves) {
        int[][] offsets = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        for (int[] offset : offsets) {
            int tx = x + offset[0];
            int ty = y + offset[1];
            if (isValidCoord(tx, ty)) {
                ChessPiece target = board[ty][tx];
                if (target == null || target.getColor() != piece.getColor()) {
                    moves.add(new ChessMove(x, y, tx, ty));
                }
            }
        }

        if (!piece.hasMoved() && !isInCheck(piece.getColor())) {
            int rank = piece.getColor() == ChessColor.WHITE ? 7 : 0;
            ChessColor enemyColor = piece.getColor().opposite();

            if (y == rank && x == 4) {
                ChessPiece rookK = board[rank][7];
                if (rookK != null && rookK.getType() == ChessPieceType.ROOK && !rookK.hasMoved()) {
                    if (board[rank][5] == null && board[rank][6] == null) {
                        if (!isSquareAttacked(5, rank, enemyColor) && !isSquareAttacked(6, rank, enemyColor)) {
                            moves.add(new ChessMove(4, rank, 6, rank, null, false, true));
                        }
                    }
                }

                ChessPiece rookQ = board[rank][0];
                if (rookQ != null && rookQ.getType() == ChessPieceType.ROOK && !rookQ.hasMoved()) {
                    if (board[rank][1] == null && board[rank][2] == null && board[rank][3] == null) {
                        if (!isSquareAttacked(3, rank, enemyColor) && !isSquareAttacked(2, rank, enemyColor)) {
                            moves.add(new ChessMove(4, rank, 2, rank, null, false, true));
                        }
                    }
                }
            }
        }
    }

    public void makeMove(ChessMove move) {
        ChessPiece moved = board[move.getFromY()][move.getFromX()];
        ChessPiece captured;

        if (move.isEnPassant()) {
            captured = board[move.getFromY()][move.getToX()];
            board[move.getFromY()][move.getToX()] = null;
        } else {
            captured = board[move.getToY()][move.getToX()];
        }

        board[move.getFromY()][move.getFromX()] = null;

        if (move.getPromotion() != null) {
            board[move.getToY()][move.getToX()] = new ChessPiece(move.getPromotion(), moved.getColor(), true);
        } else {
            board[move.getToY()][move.getToX()] = moved.withMoved(true);
        }

        if (move.isCastling()) {
            if (move.getToX() == 6) {
                ChessPiece rook = board[move.getToY()][7];
                board[move.getToY()][7] = null;
                board[move.getToY()][5] = rook.withMoved(true);
            } else if (move.getToX() == 2) {
                ChessPiece rook = board[move.getToY()][0];
                board[move.getToY()][0] = null;
                board[move.getToY()][3] = rook.withMoved(true);
            }
        }

        recordStack.push(new MoveRecord(move, moved, captured, enPassantSquare, halfMoveClock));
        moveHistory.add(move);

        if (captured != null) {
            if (captured.getColor() == ChessColor.WHITE) {
                capturedWhitePieces.add(captured);
            } else {
                capturedBlackPieces.add(captured);
            }
        }

        if (moved.getType() == ChessPieceType.PAWN && Math.abs(move.getToY() - move.getFromY()) == 2) {
            enPassantSquare = new int[]{move.getFromX(), (move.getFromY() + move.getToY()) / 2};
        } else {
            enPassantSquare = null;
        }

        if (moved.getType() == ChessPieceType.PAWN || captured != null) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }

        if (turn == ChessColor.BLACK) {
            fullMoveNumber++;
        }

        turn = turn.opposite();
        positionHistory.add(toPositionKey());
    }

    public void undoMove() {
        if (recordStack.isEmpty()) return;

        MoveRecord record = recordStack.pop();
        ChessMove move = record.getMove();

        turn = turn.opposite();
        if (turn == ChessColor.BLACK) {
            fullMoveNumber--;
        }

        halfMoveClock = record.getPrevHalfMoveClock();
        enPassantSquare = record.getPrevEnPassant();

        if (!positionHistory.isEmpty()) {
            positionHistory.remove(positionHistory.size() - 1);
        }
        if (!moveHistory.isEmpty()) {
            moveHistory.remove(moveHistory.size() - 1);
        }

        board[move.getFromY()][move.getFromX()] = record.getMovedPiece();
        board[move.getToY()][move.getToX()] = null;

        if (record.getCapturedPiece() != null) {
            if (move.isEnPassant()) {
                board[move.getFromY()][move.getToX()] = record.getCapturedPiece();
            } else {
                board[move.getToY()][move.getToX()] = record.getCapturedPiece();
            }
            if (record.getCapturedPiece().getColor() == ChessColor.WHITE) {
                capturedWhitePieces.remove(capturedWhitePieces.size() - 1);
            } else {
                capturedBlackPieces.remove(capturedBlackPieces.size() - 1);
            }
        }

        if (move.isCastling()) {
            if (move.getToX() == 6) {
                ChessPiece rook = board[move.getToY()][5];
                board[move.getToY()][5] = null;
                board[move.getToY()][7] = rook.withMoved(false);
            } else if (move.getToX() == 2) {
                ChessPiece rook = board[move.getToY()][3];
                board[move.getToY()][3] = null;
                board[move.getToY()][0] = rook.withMoved(false);
            }
        }
    }

    public String toPositionKey() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                ChessPiece p = board[y][x];
                if (p == null) {
                    sb.append('.');
                } else {
                    char c = p.getType().name().charAt(0);
                    if (p.getType() == ChessPieceType.KNIGHT) c = 'N';
                    sb.append(p.getColor() == ChessColor.WHITE ? Character.toUpperCase(c) : Character.toLowerCase(c));
                }
            }
        }
        sb.append(' ').append(turn);
        sb.append(' ');
        ChessPiece wk = board[7][4];
        if (wk != null && wk.getType() == ChessPieceType.KING && !wk.hasMoved()) {
            ChessPiece r1 = board[7][7];
            if (r1 != null && r1.getType() == ChessPieceType.ROOK && !r1.hasMoved()) sb.append('K');
            ChessPiece r2 = board[7][0];
            if (r2 != null && r2.getType() == ChessPieceType.ROOK && !r2.hasMoved()) sb.append('Q');
        }
        ChessPiece bk = board[0][4];
        if (bk != null && bk.getType() == ChessPieceType.KING && !bk.hasMoved()) {
            ChessPiece r1 = board[0][7];
            if (r1 != null && r1.getType() == ChessPieceType.ROOK && !r1.hasMoved()) sb.append('k');
            ChessPiece r2 = board[0][0];
            if (r2 != null && r2.getType() == ChessPieceType.ROOK && !r2.hasMoved()) sb.append('q');
        }
        sb.append(' ');
        if (enPassantSquare != null) {
            sb.append(enPassantSquare[0]).append(',').append(enPassantSquare[1]);
        } else {
            sb.append('-');
        }
        return sb.toString();
    }

    public boolean isCheckmate() {
        return isInCheck(turn) && generateLegalMoves(turn).isEmpty();
    }

    public boolean isStalemate() {
        return !isInCheck(turn) && generateLegalMoves(turn).isEmpty();
    }

    public boolean isFiftyMoveRule() {
        return halfMoveClock >= 100;
    }

    public boolean isThreefoldRepetition() {
        if (positionHistory.isEmpty()) return false;
        String current = positionHistory.get(positionHistory.size() - 1);
        int count = 0;
        for (String pos : positionHistory) {
            if (pos.equals(current)) {
                count++;
                if (count >= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInsufficientMaterial() {
        List<ChessPiece> pieces = new ArrayList<>();
        int whiteBishops = 0;
        int blackBishops = 0;
        int whiteKnights = 0;
        int blackKnights = 0;
        int whiteBishopSquareColor = -1;
        int blackBishopSquareColor = -1;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                ChessPiece piece = board[y][x];
                if (piece != null) {
                    if (piece.getType() == ChessPieceType.PAWN || piece.getType() == ChessPieceType.ROOK || piece.getType() == ChessPieceType.QUEEN) {
                        return false;
                    }
                    if (piece.getType() == ChessPieceType.BISHOP) {
                        if (piece.getColor() == ChessColor.WHITE) {
                            whiteBishops++;
                            whiteBishopSquareColor = (x + y) % 2;
                        } else {
                            blackBishops++;
                            blackBishopSquareColor = (x + y) % 2;
                        }
                    } else if (piece.getType() == ChessPieceType.KNIGHT) {
                        if (piece.getColor() == ChessColor.WHITE) {
                            whiteKnights++;
                        } else {
                            blackKnights++;
                        }
                    }
                    pieces.add(piece);
                }
            }
        }

        if (pieces.size() == 2) {
            return true;
        }
        if (pieces.size() == 3) {
            return whiteBishops == 1 || blackBishops == 1 || whiteKnights == 1 || blackKnights == 1;
        }
        if (pieces.size() == 4) {
            return whiteBishops == 1 && blackBishops == 1 && whiteBishopSquareColor == blackBishopSquareColor;
        }
        return false;
    }

    public boolean isGameOver() {
        return isCheckmate() || isStalemate() || isFiftyMoveRule() || isThreefoldRepetition() || isInsufficientMaterial();
    }
}
