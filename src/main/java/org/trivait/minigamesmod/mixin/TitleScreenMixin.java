package org.trivait.minigamesmod.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.config.Config;
import org.trivait.minigamesmod.config.util.MainMenuButtonPosition;
import org.trivait.minigamesmod.gui.widget.MinigamesButton;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addMinigamesButton(CallbackInfo ci) {
        Config cfg = MinigamesMod.CONFIG;
        if (cfg.mainMenuButtonPosition == null) {
            cfg.mainMenuButtonPosition = MainMenuButtonPosition.RIGHT_MULTIPLAYER;
        }
        this.addDrawableChild(new MinigamesButton(
                cfg.mainMenuButtonPosition.getX(this.width),
                cfg.mainMenuButtonPosition.getY(this.height)
        ));
    }
}
