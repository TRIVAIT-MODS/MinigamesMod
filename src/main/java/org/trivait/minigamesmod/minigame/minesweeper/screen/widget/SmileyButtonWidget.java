package org.trivait.minigamesmod.minigame.minesweeper.screen.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minigamesmod.MinigamesMod;

public class SmileyButtonWidget extends ClickableWidget {

    private static final Identifier TEX_PLAYING = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/minesweeper/smiley_playing.png");
    private static final Identifier TEX_WIN     = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/minesweeper/smiley_win.png");
    private static final Identifier TEX_LOSE    = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/minesweeper/smiley_lose.png");
    private static final Identifier TEX_HOVER   = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/minesweeper/smiley_hover.png");

    public enum State { PLAYING, WIN, LOSE }

    private final Runnable onPress;
    private boolean pressed = false;
    private State state = State.PLAYING;

    public SmileyButtonWidget(int x, int y, int size, Runnable onPress) {
        super(x, y, size, size, Text.empty());
        this.onPress = onPress;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = getX(), y = getY(), w = width, h = height;
        boolean hovered = isHovered();

        int border = pressed ? 0xFF5E5E5E : (hovered ? 0xFFB5B5B5 : 0xFF7A7A7A);
        int bg     = pressed ? 0xFF232323 : (hovered ? 0xFF3A3A3A : 0xFF2E2E2E);

        context.fill(x - 1, y - 1, x + w + 1, y + h + 1, border);
        context.fill(x, y, x + w, y + h, bg);

        Identifier tex = hovered ? TEX_HOVER : switch (state) {
            case WIN  -> TEX_WIN;
            case LOSE -> TEX_LOSE;
            default   -> TEX_PLAYING;
        };

        int pad = Math.max(1, Math.min(3, Math.min(w, h) / 10));
        int offset = pressed ? 1 : 0;
        int ix = x + pad + offset, iy = y + pad + offset;
        int iw = Math.max(1, w - pad * 2), ih = Math.max(1, h - pad * 2);
        context.drawTexture(RenderLayer::getGuiTextured, tex, ix, iy, 0, 0, iw, ih, iw, ih);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (active) onPress.run();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || button != 0 || !isMouseOver(mouseX, mouseY)) return false;
        pressed = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) pressed = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
