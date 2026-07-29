package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ArrowButtonWidget extends ClickableWidget {

    private static final Identifier SPRITE_LEFT  = Identifier.ofVanilla("transferable_list/unselect");
    private static final Identifier SPRITE_RIGHT = Identifier.ofVanilla("transferable_list/select");
    private static final Identifier SPRITE_LEFT_HIGHLIGHTED  = Identifier.ofVanilla("transferable_list/unselect_highlighted");
    private static final Identifier SPRITE_RIGHT_HIGHLIGHTED = Identifier.ofVanilla("transferable_list/select_highlighted");

    public enum Direction { LEFT, RIGHT }

    private final Direction direction;
    private final Runnable onClick;

    public ArrowButtonWidget(int x, int y, int size, Direction direction, Runnable onClick) {
        super(x, y, size, size, Text.empty());
        this.direction = direction;
        this.onClick = onClick;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Identifier sprite = switch (direction) {
            case LEFT  -> isHovered() ? SPRITE_LEFT_HIGHLIGHTED  : SPRITE_LEFT;
            case RIGHT -> isHovered() ? SPRITE_RIGHT_HIGHLIGHTED : SPRITE_RIGHT;
        };
        context.drawGuiTexture(RenderLayer::getGuiTextured, sprite, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onClick.run();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
