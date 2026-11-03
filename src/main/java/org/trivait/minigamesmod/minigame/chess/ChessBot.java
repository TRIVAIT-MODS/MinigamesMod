package org.trivait.minigamesmod.minigame.chess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChessBot {

    private static final int[] PAWN_PST = {
            0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
             5,  5, 10, 25, 25, 10,  5,  5,
             0,  0,  0, 20, 20,  0,  0,  0,
             5, -5,-10,  0,  0,-10, -5,  5,
             5, 10, 10,-20,-20, 10, 10,  5,
             0,  0,  0,  0,  0,  0,  0,  0
    };

    private static final int[] KNIGHT_PST = {
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50
    };

    private static final int[] BISHOP_PST = {
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20
    };

    private static final int[] ROOK_PST = {
             0,  0,  0,  0,  0,  0,  0,  0,
             5, 10, 10, 10, 10, 10, 10,  5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
             0,  0,  0,  5,  5,  0,  0,  0
    };

    private static final int[] QUEEN_PST = {
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
             -5,  0,  5,  5,  5,  5,  0, -5,
              0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] KING_PST = {
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
             20, 20,  0,  0,  0,  0, 20, 20,
             20, 30, 10,  0,  0, 10, 30, 20
    };

    private final Random random = new Random();

    public ChessMove findBestMove(ChessBoard board, ChessBotMode mode) {
        List<ChessMove> legalMoves = board.generateLegalMoves(board.getTurn());
        if (legalMoves.isEmpty()) return null;
        if (legalMoves.size() == 1) return legalMoves.get(0);

        if (mode == ChessBotMode.EASY) {
            return findEasyMove(board, legalMoves);
        }

        int depth;
        int qDepth;
        if (mode == ChessBotMode.NORMAL) {
            depth = 2;
            qDepth = 0;
        } else if (mode == ChessBotMode.HARD) {
            depth = 3;
            qDepth = 2;
        } else {
            depth = 4;
            qDepth = 3;
        }

        orderMoves(legalMoves, board);

        ChessMove bestMove = legalMoves.get(0);
        int bestScore = Integer.MIN_VALUE + 1000;
        int alpha = Integer.MIN_VALUE + 1000;
        int beta = Integer.MAX_VALUE - 1000;

        for (ChessMove move : legalMoves) {
            board.makeMove(move);
            int score = -negamax(board, depth - 1, -beta, -alpha, qDepth);
            board.undoMove();

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            if (score > alpha) {
                alpha = score;
            }
        }

        return bestMove;
    }

    private ChessMove findEasyMove(ChessBoard board, List<ChessMove> legalMoves) {
        ChessMove bestMove = legalMoves.get(0);
        int bestScore = Integer.MIN_VALUE;

        for (ChessMove move : legalMoves) {
            board.makeMove(move);
            int score = -evaluate(board) + random.nextInt(80);
            board.undoMove();

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private int negamax(ChessBoard board, int depth, int alpha, int beta, int qDepth) {
        if (board.isCheckmate()) {
            return -100000 - depth;
        }
        if (board.isStalemate() || board.isInsufficientMaterial() || board.isThreefoldRepetition() || board.isFiftyMoveRule()) {
            return 0;
        }

        if (depth <= 0) {
            if (qDepth > 0) {
                return quiescence(board, alpha, beta, qDepth);
            }
            return evaluate(board);
        }

        List<ChessMove> legalMoves = board.generateLegalMoves(board.getTurn());
        orderMoves(legalMoves, board);

        int bestScore = Integer.MIN_VALUE + 1000;

        for (ChessMove move : legalMoves) {
            board.makeMove(move);
            int score = -negamax(board, depth - 1, -beta, -alpha, qDepth);
            board.undoMove();

            if (score > bestScore) {
                bestScore = score;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (alpha >= beta) {
                break;
            }
        }

        return bestScore;
    }

    private int quiescence(ChessBoard board, int alpha, int beta, int qDepth) {
        int standPat = evaluate(board);
        if (standPat >= beta) {
            return beta;
        }
        if (standPat > alpha) {
            alpha = standPat;
        }
        if (qDepth <= 0) {
            return alpha;
        }

        List<ChessMove> captures = getCaptures(board);
        orderMoves(captures, board);

        for (ChessMove move : captures) {
            board.makeMove(move);
            int score = -quiescence(board, -beta, -alpha, qDepth - 1);
            board.undoMove();

            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }

    private List<ChessMove> getCaptures(ChessBoard board) {
        List<ChessMove> captures = new ArrayList<>();
        List<ChessMove> all = board.generateLegalMoves(board.getTurn());
        for (ChessMove move : all) {
            if (move.isEnPassant() || move.getPromotion() != null || board.getPiece(move.getToX(), move.getToY()) != null) {
                captures.add(move);
            }
        }
        return captures;
    }

    private void orderMoves(List<ChessMove> moves, ChessBoard board) {
        moves.sort((m1, m2) -> Integer.compare(scoreMove(m2, board), scoreMove(m1, board)));
    }

    private int scoreMove(ChessMove move, ChessBoard board) {
        int score = 0;
        ChessPiece target = board.getPiece(move.getToX(), move.getToY());
        if (target != null) {
            ChessPiece attacker = board.getPiece(move.getFromX(), move.getFromY());
            int attackerVal = attacker != null ? attacker.getType().getValue() : 100;
            score += 10000 + target.getType().getValue() * 10 - attackerVal;
        } else if (move.isEnPassant()) {
            score += 10500;
        }

        if (move.getPromotion() != null) {
            score += 9000;
        }

        if (move.isCastling()) {
            score += 500;
        }

        return score;
    }

    public int evaluate(ChessBoard board) {
        int whiteScore = 0;
        int blackScore = 0;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                ChessPiece piece = board.getPiece(x, y);
                if (piece == null) continue;

                int material = piece.getType().getValue();
                int pst = getPst(piece.getType(), piece.getColor(), x, y);
                int total = material + pst;

                if (piece.getColor() == ChessColor.WHITE) {
                    whiteScore += total;
                } else {
                    blackScore += total;
                }
            }
        }

        return board.getTurn() == ChessColor.WHITE ? (whiteScore - blackScore) : (blackScore - whiteScore);
    }

    private int getPst(ChessPieceType type, ChessColor color, int x, int y) {
        int index = color == ChessColor.WHITE ? (y * 8 + x) : ((7 - y) * 8 + x);
        switch (type) {
            case PAWN: return PAWN_PST[index];
            case KNIGHT: return KNIGHT_PST[index];
            case BISHOP: return BISHOP_PST[index];
            case ROOK: return ROOK_PST[index];
            case QUEEN: return QUEEN_PST[index];
            case KING: return KING_PST[index];
            default: return 0;
        }
    }
}
