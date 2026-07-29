package org.trivait.minigamesmod.minigame.minesweeper.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.minigame.minesweeper.leaderboard.SheetsApi;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardVersionWidget extends ClickableWidget {

    private static final int SIZE = 26;
    private static final Identifier TEX_SCORE = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/minesweeper/api.png");

    public ScoreboardVersionWidget(int x, int y) {
        super(x, y, SIZE, SIZE, Text.empty());
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.drawTexture(RenderLayer::getGuiTextured, TEX_SCORE, getX(), getY(), 0, 0, SIZE, SIZE, SIZE, SIZE);

        if (isMouseOver(mouseX, mouseY)) {
            List<Text> tooltip = buildTooltip();
            ctx.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip, mouseX, mouseY);
        }
    }

    private List<Text> buildTooltip() {
        List<Text> lines = new ArrayList<>();
        String scriptVer = SheetsApi.getScriptVersion() != null ? SheetsApi.getScriptVersion() : "?";
        String apiVer = SheetsApi.SCOREBOARD_API_VERSION;
        lines.add(Text.translatable("minigame.minesweeper.leaderboard.version.script", scriptVer));
        lines.add(Text.translatable("minigame.minesweeper.leaderboard.version.api", apiVer));
        if (SheetsApi.isVersionMismatch()) {
            lines.add(Text.translatable("minigame.minesweeper.leaderboard.version.update_mod").styled(s -> s.withColor(0xFF5555)));
        }
        return lines;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
