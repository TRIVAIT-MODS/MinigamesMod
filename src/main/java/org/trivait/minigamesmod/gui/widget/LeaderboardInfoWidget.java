package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.trivait.minigamesmod.leaderboard.Leaderboard;
import org.trivait.minigamesmod.leaderboard.SheetsApi;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardInfoWidget extends AbstractWidget {

    private static final int SIZE = 20;
    private static final Identifier TEX_SCORE = Identifier.withDefaultNamespace("textures/gui/sprites/icon/info.png");

    private Leaderboard leaderboard;

    public LeaderboardInfoWidget(int x, int y, Leaderboard leaderboard) {
        super(x, y, SIZE, SIZE, Component.empty());
        this.leaderboard = leaderboard;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEX_SCORE, getX(), getY(), 0, 0, SIZE, SIZE, SIZE, SIZE);

        if (isMouseOver(mouseX, mouseY)) {
            List<Component> tooltip = buildTooltip();
            context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
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
        if (leaderboard.getConditions()!=null) {
            lines.add(Component.translatable("minigame.leaderboard.conditions").withStyle(style -> style.withBold(true)));
            lines.addAll(leaderboard.getConditions());
        }

        return lines;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
