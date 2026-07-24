package org.trivait.minigamesmod.mixin;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("RETURN"))
    private void addMinesweeperButton(CallbackInfo ci) {
        Config cfg = MinesweeperMod.CONFIG;
        if (cfg.pauseMenuButtonPosition == null) {
            cfg.pauseMenuButtonPosition = PauseMenuButtonPosition.RIGHT_NEXT_ROW;
        }
        TextIconButtonWidget minesweeperBtn = TextIconButtonWidget.builder(
                Text.empty(),
                (button) -> {
                    SavedGame saved = MinesweeperMod.getSavedGame();
                    if (saved != null) {
                        this.client.setScreen(new MinesweeperScreen(saved, cfg.enableAnimations, GameMode.DEFAULT));
                    } else {
                        this.client.setScreen(new MinesweeperScreen(new GameSettings(cfg.gridWidth, cfg.gridHeight, cfg.mines), cfg.enableAnimations, GameMode.DEFAULT));
                    }
                },
                true
        ).width(20).texture(Identifier.of("minesweeper", "icon/button"), 16, 16).build();

        for (ButtonWidget button : this.children().stream().filter(e -> e instanceof ButtonWidget).map(e -> (ButtonWidget) e).toList()) {
            if (button.getMessage().equals(Text.translatable("menu.returnToGame"))) {
                int buttonX = button.getX();
                int buttonY = button.getY();
                int buttonWidth = button.getWidth();

                int x = cfg.pauseMenuButtonPosition.getX(buttonX, buttonWidth);
                int y = cfg.pauseMenuButtonPosition.getY(buttonY);

                minesweeperBtn.setPosition(x, y);
                break;
            }
        }

        this.addDrawableChild(minesweeperBtn);
    }
}