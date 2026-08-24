package org.trivait.minigamesmod.gui.widget;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.trivait.minigamesmod.leaderboard.Leaderboard;
import org.trivait.minigamesmod.leaderboard.LeaderboardEntry;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardWidget extends AbstractWidget {

    private static final int BG        = 0xCC1A1A1A;
    private static final int BORDER    = 0xFF555555;
    private static final int HEADER_BG = 0xFF2B2B2B;
    private static final int ROW_ODD   = 0xFF222222;
    private static final int ROW_EVEN  = 0xFF1A1A1A;
    private static final int ROW_HOVER = 0xFF2E2E2E;
    private static final int ROW_H     = 13;
    private static final int HEADER_H  = 14;
    private static final int PAD_X     = 6;
    private static final int SB_W      = 4;

    private static final int COLOR_WHITE  = 0xFFFFFFFF;
    private static final int COLOR_GOLD   = 0xFFFFD700;
    private static final int COLOR_SILVER = 0xFFB0B0B0;
    private static final int COLOR_BRONZE = 0xFFCD7F32;

    private static final boolean SMOOTH =
            FabricLoader.getInstance().isModLoaded("smoothscroll");

    private final Leaderboard leaderboard;

    private List<LeaderboardEntry> entries = new ArrayList<>();
    private boolean loading = true;
    private String error = null;

    private int scrollOffset = 0;
    private float smoothScrollOffset = 0f;
    private float scrollTarget = 0f;

    public LeaderboardWidget(int x, int y, int width, int height, Leaderboard leaderboard) {
        super(x, y, width, height, Component.empty());
        this.leaderboard = leaderboard;
        refresh();
    }

    public void refresh() {
        loading = true;
        error = null;

        leaderboard.getEntries().whenComplete((list, throwable) ->
                Minecraft.getInstance().execute(() -> {
                    if (throwable != null) {
                        System.out.println("[LeaderboardWidget] ERROR:");
                        throwable.printStackTrace();

                        entries = List.of();
                        error = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
                    } else {
                        entries = list;
                        error = null;
                    }
                    scrollOffset = 0;
                    smoothScrollOffset = 0;
                    scrollTarget = 0;
                    loading = false;
                })
        );
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        ctx.fill(x, y, x + w, y + h, BG);
        ctx.fill(x, y, x + w, y + 1, BORDER);
        ctx.fill(x, y + h - 1, x + w, y + h, BORDER);
        ctx.fill(x, y, x + 1, y + h, BORDER);
        ctx.fill(x + w - 1, y, x + w, y + h, BORDER);

        ctx.fill(x + 1, y + 1, x + w - 1, y + HEADER_H + 1, HEADER_BG);

        ctx.text(mc.font, Component.literal("Leaderboard"), x + PAD_X, y + 4, COLOR_WHITE, false);

        int listY = y + HEADER_H + 2;
        int listH = h - HEADER_H - 2;

        if (loading) {
            drawCentered(ctx, mc, "Loading...", x, listY, w, listH, 0xFFAAAAAA);
            return;
        }

        if (error != null) {
            drawCentered(ctx, mc, "Error: " + error, x, listY, w, listH, 0xFFFF5555);
            return;
        }
        int visibleRows = listH / ROW_H;
        int maxScroll = Math.max(0, entries.size() - visibleRows);

        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        if (SMOOTH) {
            scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll));
            smoothScrollOffset += (scrollTarget - smoothScrollOffset) * 0.25f;

            if (Math.abs(smoothScrollOffset - scrollTarget) < 0.01f)
                smoothScrollOffset = scrollTarget;
        }

        boolean needsScrollbar = entries.size() > visibleRows && maxScroll > 0;
        int rowW = w - 2 - (needsScrollbar ? SB_W + 2 : 0);

        float renderOffset = SMOOTH ? smoothScrollOffset : scrollOffset;
        int baseRow = (int) renderOffset;
        int pixelOffset = (int) ((renderOffset - baseRow) * ROW_H);

        ctx.enableScissor(x + 1, listY, x + 1 + rowW, y + h - 2);

        int rankW = mc.font.width("000. ");

        for (int i = 0; i < visibleRows + 2 && i + baseRow < entries.size(); i++) {

            int rowY = listY + i * ROW_H - pixelOffset;
            int rank = i + baseRow + 1;

            LeaderboardEntry entry = entries.get(i + baseRow);

            boolean hovered = mouseX >= x + 1 && mouseX < x + 1 + rowW && mouseY >= rowY && mouseY < rowY + ROW_H;

            ctx.fill(x + 1, rowY, x + 1 + rowW, rowY + ROW_H, hovered ? ROW_HOVER : (rank % 2 == 0 ? ROW_EVEN : ROW_ODD));

            int placeColor = placeColor(rank);
            boolean top3 = rank <= 3;

            MutableComponent rankText = Component.literal(rank + ".").withStyle(s -> s.withBold(top3));

            ctx.text(mc.font, rankText, x + PAD_X, rowY + 2, placeColor, false);

            MutableComponent nameText = Component.literal(entry.name()).withStyle(s -> s.withBold(true));
            int nameColor = entry.name().equalsIgnoreCase(Minecraft.getInstance().getGameProfile().name())
                    ? 0xFF0094FF
                    : (top3 ? placeColor : COLOR_WHITE);
            ctx.text(mc.font, nameText, x + PAD_X + rankW, rowY + 2, nameColor, false);

            MutableComponent valueText = Component.literal(String.valueOf(entry.value())).withStyle(s -> s.withBold(true));

            int vx = x + 1 + rowW - PAD_X - mc.font.width(valueText);

            ctx.text(mc.font, valueText, vx, rowY + 2, top3 ? placeColor : COLOR_WHITE, false);
        }

        ctx.disableScissor();

        if (needsScrollbar) {

            int sbX = x + w - SB_W - 2;

            float ratio = (float) visibleRows / entries.size();

            int thumbH = Math.max(8, (int) (listH * ratio));

            float scrollFrac = maxScroll == 0 ? 0f : renderOffset / maxScroll;

            int thumbY = listY + (int) ((listH - thumbH) * scrollFrac);

            ctx.fill(sbX, listY, sbX + SB_W, y + h - 1, 0xFF333333);

            ctx.fill(sbX, thumbY, sbX + SB_W, thumbY + thumbH, 0xFF888888);
        }
    }    private static int placeColor(int rank) {
        return switch (rank) {
            case 1 -> COLOR_GOLD;
            case 2 -> COLOR_SILVER;
            case 3 -> COLOR_BRONZE;
            default -> COLOR_WHITE;
        };
    }

    private void drawCentered(GuiGraphicsExtractor ctx, Minecraft mc, String text, int x, int y, int w, int h, int color) {
        int tw = mc.font.width(text);

        ctx.text(mc.font, text, x + (w - tw) / 2, y + (h - mc.font.lineHeight) / 2, color, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isMouseOver(mouseX, mouseY))
            return false;

        int visibleRows = (height - HEADER_H - 2) / ROW_H;
        int maxScroll = Math.max(0, entries.size() - visibleRows);

        if (SMOOTH) {
            scrollTarget = Math.max(0, Math.min(scrollTarget - (float) (verticalAmount * 3), maxScroll));

            scrollOffset = (int) scrollTarget;
        } else {
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int) (verticalAmount * 3), maxScroll));
        }

        return true;
    }

    private int getEntryCount() {
        return entries.size();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }
}