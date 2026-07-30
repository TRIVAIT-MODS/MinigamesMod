package org.trivait.minigamesmod.mixin;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.config.Config;
import org.trivait.minigamesmod.config.util.PauseMenuButtonPosition;
import org.trivait.minigamesmod.gui.widget.MinigamesButton;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("RETURN"))
    private void addMinigamesButton(CallbackInfo ci) {
        Config cfg = MinigamesMod.CONFIG;
        if (cfg.pauseMenuButtonPosition == null) {
            cfg.pauseMenuButtonPosition = PauseMenuButtonPosition.RIGHT_NEXT_ROW;
        }

        MinigamesButton btn = new MinigamesButton(5, 5);

        for (ButtonWidget button : this.children().stream().filter(e -> e instanceof ButtonWidget).map(e -> (ButtonWidget) e).toList()) {
            if (button.getMessage().equals(Text.translatable("menu.returnToGame"))) {
                int x = cfg.pauseMenuButtonPosition.getX(button.getX(), button.getWidth());
                int y = cfg.pauseMenuButtonPosition.getY(button.getY());
                btn = new MinigamesButton(x, y);
                break;
            }
        }
        if (cfg.pauseMenuButton) {
            this.addDrawableChild(btn);
        }
    }
}
