package org.trivait.minigamesmod.minigame.chess;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.ConfigButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChessScreen extends Screen {

    private static final int CELL_SIZE = 20;
    private static final int PIECE_SIZE = 16;
    private static final int GAME_W = CELL_SIZE * 8;
    private static final int GUI_W = GAME_W + 16;
    private static final int BOARD_OFFSET = 8;

    private static final Identifier CLASSIC_GUI = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/gui_classic.png");
    private static final Identifier WOODEN_GUI = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/gui_wooden.png");

    private static class MoveAnimation {
        final ChessPiece piece;
        final int fromPixelX;
        final int fromPixelY;
        final int toPixelX;
        final int toPixelY;
        final int boardToX;
        final int boardToY;
        final long startTime;
        final long duration;

        MoveAnimation(ChessPiece piece, int fromPixelX, int fromPixelY, int toPixelX, int toPixelY, int boardToX, int boardToY, long duration) {
            this.piece = piece;
            this.fromPixelX = fromPixelX;
            this.fromPixelY = fromPixelY;
            this.toPixelX = toPixelX;
            this.toPixelY = toPixelY;
            this.boardToX = boardToX;
            this.boardToY = boardToY;
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
        }
    }

    private final Screen parent;
    private final Chess minigame;
    private final MinecraftClient mc;
    private final ChessBoard board;
    private final ChessBot bot;

    private ChessColor actualPlayerColor = ChessColor.WHITE;

    private int selectedX = -1;
    private int selectedY = -1;
    private final List<ChessMove> currentLegalMoves = new ArrayList<>();
    private ChessMove lastMove = null;

    private boolean isDragging = false;
    private boolean dragExitedCell = false;
    private ChessPiece draggedPiece = null;
    private int dragStartX = -1;
    private int dragStartY = -1;

    private boolean pendingPromotion = false;
    private int promoFromX;
    private int promoFromY;
    private int promoToX;
    private int promoToY;

    private boolean botThinking = false;
    private volatile ChessMove pendingBotMove = null;
    private volatile long botMoveReadyTime = 0;

    private boolean gameOver = false;
    private ChessColor winner = null;
    private Text gameOverReason = null;

    private final List<MoveAnimation> activeAnimations = new ArrayList<>();

    public ChessScreen(Screen parent, Chess minigame) {
        super(Text.empty());
        this.parent = parent;
        this.minigame = minigame;
        this.mc = MinecraftClient.getInstance();
        this.board = new ChessBoard();
        this.bot = new ChessBot();
    }

    @Override
    protected void init() {
        ButtonWidget returnButton = TextIconButtonWidget.builder(Text.empty(), b -> close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15)
                .build();
        returnButton.setTooltip(Tooltip.of(Text.translatable("minigame.return")));
        returnButton.setDimensionsAndPosition(20, 20, 10, 10);

        ButtonWidget restartButton = TextIconButtonWidget.builder(Text.empty(), b -> resetGame(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15)
                .build();
        restartButton.setTooltip(Tooltip.of(Text.translatable("minigame.restart")));
        restartButton.setDimensionsAndPosition(20, 20, 35, 10);

        ConfigButton configButton = new ConfigButton(60, 10, minigame);

        addDrawableChild(returnButton);
        addDrawableChild(restartButton);
        addDrawableChild(configButton);

        resetGame();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (botThinking && pendingBotMove != null && System.currentTimeMillis() >= botMoveReadyTime) {
            ChessMove move = pendingBotMove;
            pendingBotMove = null;
            botThinking = false;
            applyMove(move);
        }

        long now = System.currentTimeMillis();
        activeAnimations.removeIf(anim -> now - anim.startTime >= anim.duration);

        int startX = (width - GUI_W) / 2;
        int startY = (height - GUI_W) / 2;
        int boardX = startX + BOARD_OFFSET;
        int boardY = startY + BOARD_OFFSET;

        if (isDragging && !dragExitedCell) {
            int minX = boardX + dragStartX * CELL_SIZE;
            int maxX = minX + CELL_SIZE;
            int minY = boardY + dragStartY * CELL_SIZE;
            int maxY = minY + CELL_SIZE;
            if (mouseX < minX || mouseX >= maxX || mouseY < minY || mouseY >= maxY) {
                dragExitedCell = true;
            }
        }

        ChessRenderMode boardMode = MinigameRegistry.getConfig(ChessVisibleConfig.class).boardMode;
        Identifier guiTex = boardMode == ChessRenderMode.CLASSIC ? CLASSIC_GUI : WOODEN_GUI;
        context.drawTexture(guiTex, startX, startY, 0, 0, GUI_W, GUI_W, GUI_W, GUI_W);

        boolean tracesEnabled = MinigameRegistry.getConfig(ChessVisibleConfig.class).traces;

        if (tracesEnabled && lastMove != null) {
            int fx = boardX + lastMove.getFromX() * CELL_SIZE;
            int fy = boardY + lastMove.getFromY() * CELL_SIZE;
            context.fill(fx, fy, fx + CELL_SIZE, fy + CELL_SIZE, 0x44FFE082);
            int tx = boardX + lastMove.getToX() * CELL_SIZE;
            int ty = boardY + lastMove.getToY() * CELL_SIZE;
            context.fill(tx, ty, tx + CELL_SIZE, ty + CELL_SIZE, 0x55FFE082);
        }

        if (!gameOver && board.isInCheck(board.getTurn())) {
            int[] kp = board.findKing(board.getTurn());
            if (kp != null) {
                int kx = boardX + kp[0] * CELL_SIZE;
                int ky = boardY + kp[1] * CELL_SIZE;
                context.fill(kx, ky, kx + CELL_SIZE, ky + CELL_SIZE, 0x66FF0000);
            }
        }

        if (selectedX >= 0 && selectedY >= 0) {
            int sx = boardX + selectedX * CELL_SIZE;
            int sy = boardY + selectedY * CELL_SIZE;
            context.fill(sx, sy, sx + CELL_SIZE, sy + CELL_SIZE, 0x5500E5FF);
        }

        if (tracesEnabled && !currentLegalMoves.isEmpty()) {
            for (ChessMove move : currentLegalMoves) {
                int mx = boardX + move.getToX() * CELL_SIZE;
                int my = boardY + move.getToY() * CELL_SIZE;
                boolean isCapture = board.getPiece(move.getToX(), move.getToY()) != null || move.isEnPassant();
                if (isCapture) {
                    context.fill(mx, my, mx + CELL_SIZE, my + CELL_SIZE, 0x55FF5252);
                } else {
                    context.fill(mx, my, mx + CELL_SIZE, my + CELL_SIZE, 0x4400E5FF);
                }
            }
        }

        ChessRenderMode piecesMode = MinigameRegistry.getConfig(ChessVisibleConfig.class).piecesMode;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (isDragging && dragExitedCell && x == dragStartX && y == dragStartY) {
                    continue;
                }
                if (isAnimatingTarget(x, y)) {
                    continue;
                }
                ChessPiece piece = board.getPiece(x, y);
                if (piece != null) {
                    int px = boardX + x * CELL_SIZE + 2;
                    int py = boardY + y * CELL_SIZE + 2;
                    Identifier tex = ChessTextures.getPieceTexture(piece.getType(), piece.getColor(), piecesMode);
                    context.drawTexture(tex, px, py, 0, 0, PIECE_SIZE, PIECE_SIZE, PIECE_SIZE, PIECE_SIZE);
                }
            }
        }

        for (MoveAnimation anim : activeAnimations) {
            float t = (float) (now - anim.startTime) / (float) anim.duration;
            if (t < 0.0f) t = 0.0f;
            if (t > 1.0f) t = 1.0f;
            float smoothT = t * t * (3.0f - 2.0f * t);
            int curX = Math.round(anim.fromPixelX + (anim.toPixelX - anim.fromPixelX) * smoothT);
            int curY = Math.round(anim.fromPixelY + (anim.toPixelY - anim.fromPixelY) * smoothT);
            Identifier tex = ChessTextures.getPieceTexture(anim.piece.getType(), anim.piece.getColor(), piecesMode);
            context.drawTexture(tex, curX, curY, 0, 0, PIECE_SIZE, PIECE_SIZE, PIECE_SIZE, PIECE_SIZE);
        }

        if (isDragging && dragExitedCell && draggedPiece != null) {
            Identifier dragTex = ChessTextures.getPieceTexture(draggedPiece.getType(), draggedPiece.getColor(), piecesMode);
            context.drawTexture(dragTex, mouseX - PIECE_SIZE / 2, mouseY - PIECE_SIZE / 2, 0, 0, PIECE_SIZE, PIECE_SIZE, PIECE_SIZE, PIECE_SIZE);
        }

        Text statusText;
        int statusColor;
        if (gameOver) {
            if (winner == ChessColor.WHITE) {
                statusText = Text.translatable("minigame.chess.checkmate_white");
                statusColor = 0xFF55FF55;
            } else if (winner == ChessColor.BLACK) {
                statusText = Text.translatable("minigame.chess.checkmate_black");
                statusColor = 0xFFFF5555;
            } else {
                statusText = gameOverReason != null ? gameOverReason : Text.translatable("minigame.chess.stalemate");
                statusColor = 0xFFFFFF55;
            }
        } else if (botThinking) {
            statusText = Text.translatable("minigame.chess.bot_thinking");
            statusColor = 0xFFAAAAAA;
        } else if (board.isInCheck(board.getTurn())) {
            statusText = Text.translatable("minigame.chess.check");
            statusColor = 0xFFFF3333;
        } else if (board.getTurn() == ChessColor.WHITE) {
            statusText = Text.translatable("minigame.chess.white_turn");
            statusColor = 0xFFFFFFFF;
        } else {
            statusText = Text.translatable("minigame.chess.black_turn");
            statusColor = 0xFFAAAAAA;
        }
        int tw = textRenderer.getWidth(statusText);
        context.drawText(textRenderer, statusText, (width - tw) / 2, startY - 14, statusColor, true);

        if (pendingPromotion) {
            context.fill(startX, startY, startX + GUI_W, startY + GUI_W, 0x99000000);
            int pw = 96;
            int ph = 42;
            int px = (width - pw) / 2;
            int py = (height - ph) / 2;
            context.fill(px, py, px + pw, py + ph, 0xEE2A2A2A);
            context.fill(px, py, px + pw, py + 1, 0xFFFFFFFF);
            context.fill(px, py + ph - 1, px + pw, py + ph, 0xFFFFFFFF);
            context.fill(px, py, px + 1, py + ph, 0xFFFFFFFF);
            context.fill(px + pw - 1, py, px + pw, py + ph, 0xFFFFFFFF);

            Text promoTitle = Text.translatable("minigame.chess.promote");
            int ptw = textRenderer.getWidth(promoTitle);
            context.drawText(textRenderer, promoTitle, (width - ptw) / 2, py + 4, 0xFFFFFFFF, false);

            ChessPieceType[] promoTypes = {ChessPieceType.QUEEN, ChessPieceType.ROOK, ChessPieceType.BISHOP, ChessPieceType.KNIGHT};
            for (int i = 0; i < 4; i++) {
                int itemX = px + 8 + i * 21;
                int itemY = py + 17;
                if (mouseX >= itemX && mouseX < itemX + 18 && mouseY >= itemY && mouseY < itemY + 18) {
                    context.fill(itemX, itemY, itemX + 18, itemY + 18, 0x55FFFFFF);
                }
                Identifier pTex = ChessTextures.getPieceTexture(promoTypes[i], actualPlayerColor, piecesMode);
                context.drawTexture(pTex, itemX + 1, itemY + 1, 0, 0, PIECE_SIZE, PIECE_SIZE, PIECE_SIZE, PIECE_SIZE);
            }
        }
    }

    private boolean isAnimatingTarget(int x, int y) {
        for (MoveAnimation anim : activeAnimations) {
            if (anim.boardToX == x && anim.boardToY == y) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (pendingPromotion) {
            int pw = 96;
            int ph = 42;
            int px = (width - pw) / 2;
            int py = (height - ph) / 2;
            ChessPieceType[] promoTypes = {ChessPieceType.QUEEN, ChessPieceType.ROOK, ChessPieceType.BISHOP, ChessPieceType.KNIGHT};
            for (int i = 0; i < 4; i++) {
                int itemX = px + 8 + i * 21;
                int itemY = py + 17;
                if (mouseX >= itemX && mouseX < itemX + 18 && mouseY >= itemY && mouseY < itemY + 18) {
                    completePromotion(promoTypes[i]);
                    return true;
                }
            }
            return true;
        }

        if (gameOver || botThinking) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int boardX = (width - GUI_W) / 2 + BOARD_OFFSET;
        int boardY = (height - GUI_W) / 2 + BOARD_OFFSET;
        int cellX = (int) Math.floor((mouseX - boardX) / (double) CELL_SIZE);
        int cellY = (int) Math.floor((mouseY - boardY) / (double) CELL_SIZE);

        if (cellX >= 0 && cellX < 8 && cellY >= 0 && cellY < 8) {
            if (selectedX >= 0 && selectedY >= 0) {
                ChessMove move = findLegalMove(selectedX, selectedY, cellX, cellY);
                if (move != null) {
                    if (move.getPromotion() != null) {
                        pendingPromotion = true;
                        promoFromX = selectedX;
                        promoFromY = selectedY;
                        promoToX = cellX;
                        promoToY = cellY;
                        return true;
                    }
                    executePlayerMove(move);
                    return true;
                }

                ChessPiece clickedPiece = board.getPiece(cellX, cellY);
                if (clickedPiece != null && clickedPiece.getColor() == actualPlayerColor && board.getTurn() == actualPlayerColor) {
                    selectPiece(cellX, cellY);
                    startDrag(cellX, cellY, clickedPiece);
                    return true;
                }

                clearSelection();
                return true;
            } else {
                ChessPiece clickedPiece = board.getPiece(cellX, cellY);
                if (clickedPiece != null && clickedPiece.getColor() == actualPlayerColor && board.getTurn() == actualPlayerColor) {
                    selectPiece(cellX, cellY);
                    startDrag(cellX, cellY, clickedPiece);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDragging) {
            boolean wasExited = dragExitedCell;
            int startX = dragStartX;
            int startY = dragStartY;
            isDragging = false;
            dragExitedCell = false;
            draggedPiece = null;

            if (wasExited) {
                int boardX = (width - GUI_W) / 2 + BOARD_OFFSET;
                int boardY = (height - GUI_W) / 2 + BOARD_OFFSET;
                int cellX = (int) Math.floor((mouseX - boardX) / (double) CELL_SIZE);
                int cellY = (int) Math.floor((mouseY - boardY) / (double) CELL_SIZE);

                if (cellX >= 0 && cellX < 8 && cellY >= 0 && cellY < 8) {
                    if (cellX != startX || cellY != startY) {
                        ChessMove move = findLegalMove(startX, startY, cellX, cellY);
                        if (move != null) {
                            if (move.getPromotion() != null) {
                                pendingPromotion = true;
                                promoFromX = startX;
                                promoFromY = startY;
                                promoToX = cellX;
                                promoToY = cellY;
                                return true;
                            }
                            int releasePx = (int) Math.round(mouseX - PIECE_SIZE / 2.0);
                            int releasePy = (int) Math.round(mouseY - PIECE_SIZE / 2.0);
                            executePlayerMove(move, true, releasePx, releasePy);
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            resetGame();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && pendingPromotion) {
            pendingPromotion = false;
            clearSelection();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void selectPiece(int x, int y) {
        selectedX = x;
        selectedY = y;
        currentLegalMoves.clear();
        currentLegalMoves.addAll(board.generateLegalMovesForPiece(x, y));
    }

    private void startDrag(int x, int y, ChessPiece piece) {
        isDragging = true;
        dragExitedCell = false;
        draggedPiece = piece;
        dragStartX = x;
        dragStartY = y;
    }

    private void clearSelection() {
        selectedX = -1;
        selectedY = -1;
        currentLegalMoves.clear();
        isDragging = false;
        dragExitedCell = false;
        draggedPiece = null;
        dragStartX = -1;
        dragStartY = -1;
    }

    private ChessMove findLegalMove(int fromX, int fromY, int toX, int toY) {
        for (ChessMove move : currentLegalMoves) {
            if (move.getFromX() == fromX && move.getFromY() == fromY && move.getToX() == toX && move.getToY() == toY) {
                return move;
            }
        }
        return null;
    }

    private ChessMove findLegalMoveWithPromotion(int fromX, int fromY, int toX, int toY, ChessPieceType promoType) {
        List<ChessMove> moves = board.generateLegalMovesForPiece(fromX, fromY);
        for (ChessMove move : moves) {
            if (move.getFromX() == fromX && move.getFromY() == fromY && move.getToX() == toX && move.getToY() == toY && move.getPromotion() == promoType) {
                return move;
            }
        }
        return null;
    }

    private void executePlayerMove(ChessMove move) {
        executePlayerMove(move, false, 0, 0);
    }

    private void executePlayerMove(ChessMove move, boolean fromDrag, int dragReleasePx, int dragReleasePy) {
        clearSelection();
        applyMove(move, fromDrag, dragReleasePx, dragReleasePy);

        if (!gameOver && board.getTurn() != actualPlayerColor) {
            triggerBotMove();
        }
    }

    private void completePromotion(ChessPieceType type) {
        pendingPromotion = false;
        ChessMove move = findLegalMoveWithPromotion(promoFromX, promoFromY, promoToX, promoToY, type);
        if (move != null) {
            executePlayerMove(move);
        }
    }

    private void triggerBotMove() {
        botThinking = true;
        long startTime = System.currentTimeMillis();
        ChessBoard boardCopy = board.copy();
        ChessBotMode mode = MinigameRegistry.getConfig(ChessVisibleConfig.class).botMode;

        CompletableFuture.supplyAsync(() -> bot.findBestMove(boardCopy, mode)).thenAccept(bestMove -> {
            long elapsed = System.currentTimeMillis() - startTime;
            long delay = Math.max(0, 350 - elapsed);
            botMoveReadyTime = System.currentTimeMillis() + delay;
            pendingBotMove = bestMove;
        });
    }

    private void applyMove(ChessMove move) {
        applyMove(move, false, 0, 0);
    }

    private void applyMove(ChessMove move, boolean fromDrag, int dragReleasePx, int dragReleasePy) {
        int startX = (width - GUI_W) / 2;
        int startY = (height - GUI_W) / 2;
        int boardX = startX + BOARD_OFFSET;
        int boardY = startY + BOARD_OFFSET;

        boolean isCapture = move.isEnPassant() || board.getPiece(move.getToX(), move.getToY()) != null;
        board.makeMove(move);
        lastMove = move;

        ChessPiece movedPiece = board.getPiece(move.getToX(), move.getToY());
        int toPx = boardX + move.getToX() * CELL_SIZE + 2;
        int toPy = boardY + move.getToY() * CELL_SIZE + 2;
        int fromPx = fromDrag ? dragReleasePx : (boardX + move.getFromX() * CELL_SIZE + 2);
        int fromPy = fromDrag ? dragReleasePy : (boardY + move.getFromY() * CELL_SIZE + 2);
        long duration = fromDrag ? 60 : 180;

        activeAnimations.add(new MoveAnimation(movedPiece, fromPx, fromPy, toPx, toPy, move.getToX(), move.getToY(), duration));

        if (move.isCastling()) {
            if (move.getToX() == 6) {
                ChessPiece rook = board.getPiece(5, move.getToY());
                int rFrom = boardX + 7 * CELL_SIZE + 2;
                int rTo = boardX + 5 * CELL_SIZE + 2;
                int rY = boardY + move.getToY() * CELL_SIZE + 2;
                activeAnimations.add(new MoveAnimation(rook, rFrom, rY, rTo, rY, 5, move.getToY(), 180));
            } else if (move.getToX() == 2) {
                ChessPiece rook = board.getPiece(3, move.getToY());
                int rFrom = boardX + 0 * CELL_SIZE + 2;
                int rTo = boardX + 3 * CELL_SIZE + 2;
                int rY = boardY + move.getToY() * CELL_SIZE + 2;
                activeAnimations.add(new MoveAnimation(rook, rFrom, rY, rTo, rY, 3, move.getToY(), 180));
            }
        }

        if (board.isCheckmate()) {
            gameOver = true;
            winner = board.getTurn().opposite();
            if (winner == actualPlayerColor) {
                playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f);
            } else {
                playSound(SoundEvents.ENTITY_VILLAGER_DEATH, 0.9f);
            }
        } else if (board.isStalemate()) {
            gameOver = true;
            winner = null;
            gameOverReason = Text.translatable("minigame.chess.stalemate");
            playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 0.8f);
        } else if (board.isInsufficientMaterial()) {
            gameOver = true;
            winner = null;
            gameOverReason = Text.translatable("minigame.chess.draw_material");
            playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 0.8f);
        } else if (board.isThreefoldRepetition()) {
            gameOver = true;
            winner = null;
            gameOverReason = Text.translatable("minigame.chess.draw_repetition");
            playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 0.8f);
        } else if (board.isFiftyMoveRule()) {
            gameOver = true;
            winner = null;
            gameOverReason = Text.translatable("minigame.chess.draw_50");
            playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 0.8f);
        } else if (board.isInCheck(board.getTurn())) {
            playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1.0f);
        } else if (isCapture) {
            playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.6f);
        } else {
            playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.2f);
        }
    }

    private void resetGame() {
        board.initStartingPosition();
        clearSelection();
        lastMove = null;
        pendingPromotion = false;
        pendingBotMove = null;
        botThinking = false;
        gameOver = false;
        winner = null;
        gameOverReason = null;
        activeAnimations.clear();

        ChessPlayerColor pref = MinigameRegistry.getConfig(ChessVisibleConfig.class).playerColor;
        if (pref == ChessPlayerColor.WHITE) {
            actualPlayerColor = ChessColor.WHITE;
        } else if (pref == ChessPlayerColor.BLACK) {
            actualPlayerColor = ChessColor.BLACK;
        } else {
            actualPlayerColor = Math.random() < 0.5 ? ChessColor.WHITE : ChessColor.BLACK;
        }

        playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0f);

        if (actualPlayerColor == ChessColor.BLACK) {
            triggerBotMove();
        }
    }

    private void playSound(SoundEvent sound, float pitch) {
        PlayingSoundManager.playSound(sound, pitch, volume());
    }

    private float volume() {
        return PlayingSoundManager.vol(MinigameRegistry.getConfig(ChessVisibleConfig.class).volume);
    }

    @Override
    public void close() {
        mc.setScreen(parent);
    }
}
