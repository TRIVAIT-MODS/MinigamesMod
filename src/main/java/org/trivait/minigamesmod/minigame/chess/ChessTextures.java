package org.trivait.minigamesmod.minigame.chess;

import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;

public class ChessTextures {
    public static final Identifier CLASSIC_WHITE_BISHOP = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/white_bishop.png");
    public static final Identifier CLASSIC_WHITE_KING = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/white_king.png");
    public static final Identifier CLASSIC_WHITE_KNIGHT = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/white_knight.png");
    public static final Identifier CLASSIC_WHITE_PAWN = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/white_pawn.png");
    public static final Identifier CLASSIC_WHITE_QUEEN = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/white_queen.png");
    public static final Identifier CLASSIC_WHITE_ROOK = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/white_rook.png");

    public static final Identifier CLASSIC_BLACK_BISHOP = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/black_bishop.png");
    public static final Identifier CLASSIC_BLACK_KING = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/black_king.png");
    public static final Identifier CLASSIC_BLACK_KNIGHT = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/black_knight.png");
    public static final Identifier CLASSIC_BLACK_PAWN = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/black_pawn.png");
    public static final Identifier CLASSIC_BLACK_QUEEN = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/black_queen.png");
    public static final Identifier CLASSIC_BLACK_ROOK = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/classic/black_rook.png");

    public static final Identifier WOODEN_WHITE_BISHOP = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_light_bishop.png");
    public static final Identifier WOODEN_WHITE_KING = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_light_king.png");
    public static final Identifier WOODEN_WHITE_KNIGHT = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_light_knight.png");
    public static final Identifier WOODEN_WHITE_PAWN = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_light_pawn.png");
    public static final Identifier WOODEN_WHITE_QUEEN = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_light_queen.png");
    public static final Identifier WOODEN_WHITE_ROOK = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_light_rook.png");

    public static final Identifier WOODEN_BLACK_BISHOP = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_dark_bishop.png");
    public static final Identifier WOODEN_BLACK_KING = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_dark_king.png");
    public static final Identifier WOODEN_BLACK_KNIGHT = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_dark_knight.png");
    public static final Identifier WOODEN_BLACK_PAWN = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_dark_pawn.png");
    public static final Identifier WOODEN_BLACK_QUEEN = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_dark_queen.png");
    public static final Identifier WOODEN_BLACK_ROOK = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/chess/piece/wooden/wood_dark_rook.png");

    public static Identifier getPieceTexture(ChessPieceType type, ChessColor color, ChessRenderMode mode) {
        boolean classic = mode == ChessRenderMode.CLASSIC;
        boolean white = color == ChessColor.WHITE;

        if (classic) {
            if (white) {
                switch (type) {
                    case PAWN: return CLASSIC_WHITE_PAWN;
                    case KNIGHT: return CLASSIC_WHITE_KNIGHT;
                    case BISHOP: return CLASSIC_WHITE_BISHOP;
                    case ROOK: return CLASSIC_WHITE_ROOK;
                    case QUEEN: return CLASSIC_WHITE_QUEEN;
                    case KING: return CLASSIC_WHITE_KING;
                }
            } else {
                switch (type) {
                    case PAWN: return CLASSIC_BLACK_PAWN;
                    case KNIGHT: return CLASSIC_BLACK_KNIGHT;
                    case BISHOP: return CLASSIC_BLACK_BISHOP;
                    case ROOK: return CLASSIC_BLACK_ROOK;
                    case QUEEN: return CLASSIC_BLACK_QUEEN;
                    case KING: return CLASSIC_BLACK_KING;
                }
            }
        } else {
            if (white) {
                switch (type) {
                    case PAWN: return WOODEN_WHITE_PAWN;
                    case KNIGHT: return WOODEN_WHITE_KNIGHT;
                    case BISHOP: return WOODEN_WHITE_BISHOP;
                    case ROOK: return WOODEN_WHITE_ROOK;
                    case QUEEN: return WOODEN_WHITE_QUEEN;
                    case KING: return WOODEN_WHITE_KING;
                }
            } else {
                switch (type) {
                    case PAWN: return WOODEN_BLACK_PAWN;
                    case KNIGHT: return WOODEN_BLACK_KNIGHT;
                    case BISHOP: return WOODEN_BLACK_BISHOP;
                    case ROOK: return WOODEN_BLACK_ROOK;
                    case QUEEN: return WOODEN_BLACK_QUEEN;
                    case KING: return WOODEN_BLACK_KING;
                }
            }
        }
        return CLASSIC_WHITE_PAWN;
    }
}
