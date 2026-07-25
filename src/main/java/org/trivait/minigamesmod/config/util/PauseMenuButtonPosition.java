package org.trivait.minigamesmod.config.util;

import net.minecraft.text.Text;

public enum PauseMenuButtonPosition {
    LEFT_SAME_ROW(ButtonSide.LEFT, PauseRowOffset.SAME, Text.translatable("config.minigames.pause_menu_button_position.left_same_row")),
    LEFT_NEXT_ROW(ButtonSide.LEFT, PauseRowOffset.NEXT, Text.translatable("config.minigames.pause_menu_button_position.left_next_row")),
    LEFT_2_ROWS_DOWN(ButtonSide.LEFT, PauseRowOffset.TWO_DOWN, Text.translatable("config.minigames.pause_menu_button_position.left_2_rows_down")),
    LEFT_3_ROWS_DOWN(ButtonSide.LEFT, PauseRowOffset.THREE_DOWN, Text.translatable("config.minigames.pause_menu_button_position.left_3_rows_down")),
    LEFT_4_ROWS_DOWN(ButtonSide.LEFT, PauseRowOffset.FOUR_DOWN, Text.translatable("config.minigames.pause_menu_button_position.left_4_rows_down")),
    RIGHT_SAME_ROW(ButtonSide.RIGHT, PauseRowOffset.SAME, Text.translatable("config.minigames.pause_menu_button_position.right_same_row")),
    RIGHT_NEXT_ROW(ButtonSide.RIGHT, PauseRowOffset.NEXT, Text.translatable("config.minigames.pause_menu_button_position.right_next_row")),
    RIGHT_2_ROWS_DOWN(ButtonSide.RIGHT, PauseRowOffset.TWO_DOWN, Text.translatable("config.minigames.pause_menu_button_position.right_2_rows_down")),
    RIGHT_3_ROWS_DOWN(ButtonSide.RIGHT, PauseRowOffset.THREE_DOWN, Text.translatable("config.minigames.pause_menu_button_position.right_3_rows_down")),
    RIGHT_4_ROWS_DOWN(ButtonSide.RIGHT, PauseRowOffset.FOUR_DOWN, Text.translatable("config.minigames.pause_menu_button_position.right_4_rows_down"));

    private final ButtonSide side;
    private final PauseRowOffset rowOffset;
    private final Text displayName;

    PauseMenuButtonPosition(ButtonSide side, PauseRowOffset rowOffset, Text displayName) {
        this.side = side;
        this.rowOffset = rowOffset;
        this.displayName = displayName;
    }

    public int getX(int baseButtonX, int baseButtonWidth) {
        return side == ButtonSide.LEFT ? (baseButtonX - 24) : (baseButtonX + baseButtonWidth + 4);
    }

    public int getY(int baseButtonY) {
        return baseButtonY + rowOffset.getYOffset();
    }

    @Override
    public String toString() {
        return this.displayName.getString();
    }
}