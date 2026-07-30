package org.trivait.minigamesmod.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.config.Config;
import org.trivait.minigamesmod.config.util.MainMenuButtonPosition;
import org.trivait.minigamesmod.gui.widget.MinigamesButton;
import org.trivait.minigamesmod.minigame.minesweeper.game.SavedGame;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addMinesweeperButton(CallbackInfo ci) {
        Config cfg = MinigamesMod.CONFIG;
        if (cfg.mainMenuButtonPosition == null) {
            cfg.mainMenuButtonPosition = MainMenuButtonPosition.RIGHT_MULTIPLAYER;
        }
        MinigamesButton btn = new MinigamesButton(0, 0);

        btn.setPosition(
                cfg.mainMenuButtonPosition.getX(this.width),
                cfg.mainMenuButtonPosition.getY(this.height) + (getModMenuState() ? 24 : 0)
        );

        if ((cfg.mainMenuButtonPosition==MainMenuButtonPosition.RIGHT_MODS||cfg.mainMenuButtonPosition==MainMenuButtonPosition.LEFT_MODS) && !getModMenuState()) {
            btn.setPosition(
                    btn.getX(),
                    btn.getY()+24
            );
        }

        if (!cfg.mainMenuButtonPosition.equals(MainMenuButtonPosition.ICON_BUTTON)) {
            this.addRenderableWidget(btn);
        }
    }

    private boolean getModMenuState() {
        if ((ModMenuConfig.MODIFY_TITLE_SCREEN.getValue() && ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC)) {
            return false;
        }
        return true;
    }
}