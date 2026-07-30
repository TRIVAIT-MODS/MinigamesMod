package org.trivait.minigamesmod.minigame.minesweeper.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.minigame.minesweeper.leaderboard.SheetsApi;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardVersionWidget extends AbstractWidget {

    private static final int SIZE = 26;
    private static final Identifier TEX_SCORE = Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/minesweeper/api.png");

    public ScoreboardVersionWidget(int x, int y) {
        super(x, y, SIZE, SIZE, Component.empty());
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        ctx.blit(RenderPipelines.GUI_TEXTURED, TEX_SCORE, getX(), getY(), 0, 0, SIZE, SIZE, SIZE, SIZE);

        if (isMouseOver(mouseX, mouseY)) {
            List<Component> tooltip = buildTooltip();
            ctx.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    private List<Component> buildTooltip() {
        List<Component> lines = new ArrayList<>();
        String scriptVer = SheetsApi.getScriptVersion() != null ? SheetsApi.getScriptVersion() : "?";
        String apiVer = SheetsApi.SCOREBOARD_API_VERSION;
        lines.add(Component.translatable("minigame.minesweeper.leaderboard.version.script", scriptVer));
        lines.add(Component.translatable("minigame.minesweeper.leaderboard.version.api", apiVer));
        if (SheetsApi.isVersionMismatch()) {
            lines.add(Component.translatable("minigame.minesweeper.leaderboard.version.update_mod").withStyle(s -> s.withColor(0xFF5555)));
        }
        return lines;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
