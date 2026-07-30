package org.trivait.minigamesmod.mixin;

import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.config.Config;
import org.trivait.minigamesmod.config.util.PauseMenuButtonPosition;
import org.trivait.minigamesmod.gui.widget.MinigamesButton;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createPauseMenu", at = @At("RETURN"))
    private void addMinigamesButton(CallbackInfo ci) {
        Config cfg = MinigamesMod.CONFIG;
        if (cfg.pauseMenuButtonPosition == null) {
            cfg.pauseMenuButtonPosition = PauseMenuButtonPosition.RIGHT_NEXT_ROW;
        }

        MinigamesButton btn = new MinigamesButton(5, 5);

        for (Button button : this.children().stream().filter(e -> e instanceof Button).map(e -> (Button) e).toList()) {
            if (button.getMessage().equals(Component.translatable("menu.returnToGame"))) {
                int x = cfg.pauseMenuButtonPosition.getX(button.getX(), button.getWidth());
                int y = cfg.pauseMenuButtonPosition.getY(button.getY());
                btn = new MinigamesButton(x, y);
                break;
            }
        }
        if (cfg.pauseMenuButtonPosition!=PauseMenuButtonPosition.ICON_BUTTON && cfg.pauseMenuButton) {
            this.addRenderableWidget(btn);
        }
    }
}
