package org.trivait.minigamesmod.minigame.solitaire;

import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;

public class SolitaireTextures {
    public static final Identifier GUI_TEXTURE = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/solitaire/gui.png");
    public static final Identifier OVERLAY_CARD = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/solitaire/overlay.png");
    public static final Identifier CLOSED_CARD = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/solitaire/closed.png");

    public static Identifier cardTexture(SolitaireGame.Suit suit, int rank) {
        String value = switch (rank) {
            case 1 -> "ace";
            case 11 -> "joker";
            case 12 -> "queen";
            case 13 -> "king";
            default -> Integer.toString(rank);
        };
        return Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/solitaire/" + suit.textureName + "_" + value + ".png");
    }
}
