package org.trivait.minigamesmod.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ArrowButtonWidget extends AbstractWidget {

    private static final Identifier SPRITE_LEFT  = Identifier.withDefaultNamespace("transferable_list/unselect");
    private static final Identifier SPRITE_RIGHT = Identifier.withDefaultNamespace("transferable_list/select");
    private static final Identifier SPRITE_LEFT_HIGHLIGHTED  = Identifier.withDefaultNamespace("transferable_list/unselect_highlighted");
    private static final Identifier SPRITE_RIGHT_HIGHLIGHTED = Identifier.withDefaultNamespace("transferable_list/select_highlighted");

    public enum Direction { LEFT, RIGHT }

    private final Direction direction;
    private final Runnable onClick;

    public ArrowButtonWidget(int x, int y, int size, Direction direction, Runnable onClick) {
        super(x, y, size, size, Component.empty());
        this.direction = direction;
        this.onClick = onClick;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Identifier sprite = switch (direction) {
            case LEFT  -> isHovered() ? SPRITE_LEFT_HIGHLIGHTED  : SPRITE_LEFT;
            case RIGHT -> isHovered() ? SPRITE_RIGHT_HIGHLIGHTED : SPRITE_RIGHT;
        };
        context.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        onClick.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        defaultButtonNarrationText(builder);
    }
}
