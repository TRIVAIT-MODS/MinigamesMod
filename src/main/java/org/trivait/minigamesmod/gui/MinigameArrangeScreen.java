package org.trivait.minigamesmod.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.trivait.minigamesmod.api.MinigameDefinition;
import org.trivait.minigamesmod.config.MinigameLayoutConfig;
import org.trivait.minigamesmod.gui.widget.ArrowButtonWidget;
import org.trivait.minigamesmod.gui.widget.MainConfigButton;
import org.trivait.minigamesmod.gui.widget.RearrangeButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinigameArrangeScreen extends Screen {
    private static final int CARD_WIDTH = 90;
    private static final int CARD_HEIGHT = 110;
    private static final int CARD_SPACING = 20;
    private static final int BORDER = 3;
    private static final int LABEL_HEIGHT = 16;
    private static final int ARROW_SIZE = 32;
    private static final int BADGE_SIZE = 18;
    private static final float CENTER_SCALE = 1.2f;
    private static final float ANIM_SPEED = 0.15f;

    private static final Identifier BADGE_MINUS = Identifier.of("minigamesmod", "icon/badge_minus");
    private static final Identifier BADGE_MINUS_HOVER = Identifier.of("minigamesmod", "icon/badge_minus_hover");
    private static final Identifier BADGE_PLUS = Identifier.of("minigamesmod", "icon/badge_plus");
    private static final Identifier BADGE_PLUS_HOVER = Identifier.of("minigamesmod", "icon/badge_plus_hover");

    private final @Nullable Screen parent;
    private int topSelectedIndex;
    private float topAnimOffset;
    private int bottomSelectedIndex = 0;
    private float bottomAnimOffset = 0f;

    private float transitionProgress = 0f;
    private boolean isClosing = false;
    private long lastFrameTime = System.currentTimeMillis();

    private int currentTopY;
    private int currentBottomY;

    private boolean isDragging = false;
    private boolean dragSourceTop = true;
    private int draggedIndex = -1;
    private @Nullable MinigameDefinition draggedMinigame;
    private float dragOffsetX;
    private float dragOffsetY;
    private float dragCurrentMouseX;
    private float dragCurrentMouseY;
    private long lastEdgeScrollTime = 0;

    private final Map<String, Float> visualXMap = new HashMap<>();

    private ArrowButtonWidget leftTopArrow;
    private ArrowButtonWidget rightTopArrow;
    private ArrowButtonWidget leftBottomArrow;
    private ArrowButtonWidget rightBottomArrow;
    private ButtonWidget backBtn;

    public MinigameArrangeScreen(@Nullable Screen parent, int initialSelectedIndex, float initialAnimOffset) {
        super(Text.translatable("screen.minigames.list.title"));
        this.parent = parent;
        this.topSelectedIndex = initialSelectedIndex;
        this.topAnimOffset = initialAnimOffset;
    }

    @Override
    protected void init() {
        int normalCardY = this.height / 2 - CARD_HEIGHT / 2 - 10;
        currentTopY = normalCardY;
        currentBottomY = this.height + 20;

        int topArrowY = currentTopY + CARD_HEIGHT / 2 - ARROW_SIZE / 2;
        leftTopArrow = new ArrowButtonWidget(5, topArrowY, ARROW_SIZE, ArrowButtonWidget.Direction.LEFT, this::scrollTopLeft);
        rightTopArrow = new ArrowButtonWidget(width - 32, topArrowY, ARROW_SIZE, ArrowButtonWidget.Direction.RIGHT, this::scrollTopRight);

        int bottomArrowY = currentBottomY + CARD_HEIGHT / 2 - ARROW_SIZE / 2;
        leftBottomArrow = new ArrowButtonWidget(5, bottomArrowY, ARROW_SIZE, ArrowButtonWidget.Direction.LEFT, this::scrollBottomLeft);
        rightBottomArrow = new ArrowButtonWidget(width - 32, bottomArrowY, ARROW_SIZE, ArrowButtonWidget.Direction.RIGHT, this::scrollBottomRight);

        backBtn = ButtonWidget.builder(Text.translatable("gui.back"), b -> this.close())
                .dimensions(this.width / 2 - 50, this.height - 28, 100, 20)
                .build();
        this.addDrawableChild(backBtn);

        this.addDrawableChild(new RearrangeButton(width - 20 - 5 - 20 - 4, 5, () -> true, b -> this.close()));
        this.addDrawableChild(new MainConfigButton(width - 20 - 5, 5));
    }

    private void scrollTopLeft() {
        if (topSelectedIndex > 0) {
            topSelectedIndex--;
            topAnimOffset = -(CARD_WIDTH + CARD_SPACING);
        }
    }

    private void scrollTopRight() {
        List<MinigameDefinition> visible = MinigameLayoutConfig.get().getVisibleList();
        if (topSelectedIndex < visible.size() - 1) {
            topSelectedIndex++;
            topAnimOffset = CARD_WIDTH + CARD_SPACING;
        }
    }

    private void scrollBottomLeft() {
        if (bottomSelectedIndex > 0) {
            bottomSelectedIndex--;
            bottomAnimOffset = -(CARD_WIDTH + CARD_SPACING);
        }
    }

    private void scrollBottomRight() {
        List<MinigameDefinition> hidden = MinigameLayoutConfig.get().getHiddenList();
        if (bottomSelectedIndex < hidden.size() - 1) {
            bottomSelectedIndex++;
            bottomAnimOffset = CARD_WIDTH + CARD_SPACING;
        }
    }

    @Override
    public void close() {
        if (!isClosing) {
            isClosing = true;
        }
    }

    private float getTopCardScale(float cardX) {
        float slotOffset = (cardX + CARD_WIDTH / 2f) - (this.width / 2f);
        float dist = Math.abs(slotOffset) / (CARD_WIDTH + CARD_SPACING);
        return 1f + (CENTER_SCALE - 1f) * Math.max(0f, 1f - dist);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isClosing) {
            return true;
        }

        if (leftTopArrow != null && leftTopArrow.isMouseOver(mouseX, mouseY)) {
            return leftTopArrow.mouseClicked(mouseX, mouseY, button);
        }
        if (rightTopArrow != null && rightTopArrow.isMouseOver(mouseX, mouseY)) {
            return rightTopArrow.mouseClicked(mouseX, mouseY, button);
        }
        if (currentBottomY < this.height) {
            if (leftBottomArrow != null && leftBottomArrow.isMouseOver(mouseX, mouseY)) {
                return leftBottomArrow.mouseClicked(mouseX, mouseY, button);
            }
            if (rightBottomArrow != null && rightBottomArrow.isMouseOver(mouseX, mouseY)) {
                return rightBottomArrow.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        List<MinigameDefinition> visible = MinigameLayoutConfig.get().getVisibleList();
        for (int i = 0; i < visible.size(); i++) {
            MinigameDefinition mg = visible.get(i);
            int cardX = Math.round(visualXMap.getOrDefault(mg.getId(), (float) getTopCardTargetX(i)));
            int cx = cardX + CARD_WIDTH / 2;
            int cy = currentTopY + CARD_HEIGHT / 2;
            float scale = getTopCardScale(cardX);
            float localMouseX = ((float) mouseX - cx) / scale + cx;
            float localMouseY = ((float) mouseY - cy) / scale + cy;

            int badgeX = cardX + CARD_WIDTH - BADGE_SIZE - 1;
            int badgeY = currentTopY + 1;
            if (localMouseX >= badgeX && localMouseX <= badgeX + BADGE_SIZE && localMouseY >= badgeY && localMouseY <= badgeY + BADGE_SIZE) {
                int cardVisualX = cardX;
                List<MinigameDefinition> hiddenBefore = MinigameLayoutConfig.get().getHiddenList();
                boolean wasHiddenEmpty = hiddenBefore.isEmpty();
                MinigameLayoutConfig.get().hide(mg.getId());
                if (wasHiddenEmpty) {
                    bottomSelectedIndex = 0;
                    bottomAnimOffset = 0f;
                }
                visualXMap.put(mg.getId(), (float) cardVisualX);
                clampIndices();
                return true;
            }
        }

        List<MinigameDefinition> hidden = MinigameLayoutConfig.get().getHiddenList();
        for (int j = 0; j < hidden.size(); j++) {
            MinigameDefinition mg = hidden.get(j);
            int cardX = Math.round(visualXMap.getOrDefault(mg.getId(), (float) getBottomCardTargetX(j)));

            int badgeX = cardX + CARD_WIDTH - BADGE_SIZE - 1;
            int badgeY = currentBottomY + 1;
            if (mouseX >= badgeX && mouseX <= badgeX + BADGE_SIZE && mouseY >= badgeY && mouseY <= badgeY + BADGE_SIZE) {
                int cardVisualX = cardX;
                List<MinigameDefinition> visibleBefore = MinigameLayoutConfig.get().getVisibleList();
                boolean wasVisibleEmpty = visibleBefore.isEmpty();
                MinigameLayoutConfig.get().show(mg.getId());
                if (wasVisibleEmpty) {
                    topSelectedIndex = 0;
                    topAnimOffset = 0f;
                }
                visualXMap.put(mg.getId(), (float) cardVisualX);
                clampIndices();
                return true;
            }
        }

        for (int i = 0; i < visible.size(); i++) {
            MinigameDefinition mg = visible.get(i);
            int cardX = Math.round(visualXMap.getOrDefault(mg.getId(), (float) getTopCardTargetX(i)));
            int cx = cardX + CARD_WIDTH / 2;
            int cy = currentTopY + CARD_HEIGHT / 2;
            float scale = getTopCardScale(cardX);
            float localMouseX = ((float) mouseX - cx) / scale + cx;
            float localMouseY = ((float) mouseY - cy) / scale + cy;

            if (localMouseX >= cardX && localMouseX <= cardX + CARD_WIDTH && localMouseY >= currentTopY && localMouseY <= currentTopY + CARD_HEIGHT) {
                isDragging = true;
                dragSourceTop = true;
                draggedIndex = i;
                draggedMinigame = mg;
                dragOffsetX = (float) mouseX - cardX;
                dragOffsetY = (float) mouseY - currentTopY;
                dragCurrentMouseX = (float) mouseX;
                dragCurrentMouseY = (float) mouseY;
                lastEdgeScrollTime = 0;
                return true;
            }
        }

        for (int j = 0; j < hidden.size(); j++) {
            MinigameDefinition mg = hidden.get(j);
            int cardX = Math.round(visualXMap.getOrDefault(mg.getId(), (float) getBottomCardTargetX(j)));

            if (mouseX >= cardX && mouseX <= cardX + CARD_WIDTH && mouseY >= currentBottomY && mouseY <= currentBottomY + CARD_HEIGHT) {
                isDragging = true;
                dragSourceTop = false;
                draggedIndex = j;
                draggedMinigame = mg;
                dragOffsetX = (float) mouseX - cardX;
                dragOffsetY = (float) mouseY - currentBottomY;
                dragCurrentMouseX = (float) mouseX;
                dragCurrentMouseY = (float) mouseY;
                lastEdgeScrollTime = 0;
                return true;
            }
        }

        return false;
    }

    private void checkDragReorder() {
        if (!isDragging || draggedMinigame == null) {
            return;
        }
        if (dragSourceTop) {
            boolean insideTop = (dragCurrentMouseY >= currentTopY - 12 && dragCurrentMouseY <= currentTopY + CARD_HEIGHT + 12);
            if (insideTop) {
                float curDragX = dragCurrentMouseX - dragOffsetX;
                float draggedCenter = curDragX + CARD_WIDTH / 2f;
                List<MinigameDefinition> visible = MinigameLayoutConfig.get().getVisibleList();
                while (draggedIndex > 0) {
                    float leftCenter = getTopCardTargetX(draggedIndex - 1) + CARD_WIDTH / 2f;
                    if (draggedCenter < leftCenter) {
                        MinigameLayoutConfig.get().moveVisible(draggedIndex, draggedIndex - 1);
                        draggedIndex--;
                    } else {
                        break;
                    }
                }
                while (draggedIndex < visible.size() - 1) {
                    float rightCenter = getTopCardTargetX(draggedIndex + 1) + CARD_WIDTH / 2f;
                    if (draggedCenter > rightCenter) {
                        MinigameLayoutConfig.get().moveVisible(draggedIndex, draggedIndex + 1);
                        draggedIndex++;
                    } else {
                        break;
                    }
                }
            }
        } else {
            boolean insideBottom = (dragCurrentMouseY >= currentBottomY - 12);
            if (insideBottom) {
                float curDragX = dragCurrentMouseX - dragOffsetX;
                float draggedCenter = curDragX + CARD_WIDTH / 2f;
                List<MinigameDefinition> hidden = MinigameLayoutConfig.get().getHiddenList();
                while (draggedIndex > 0) {
                    float leftCenter = getBottomCardTargetX(draggedIndex - 1) + CARD_WIDTH / 2f;
                    if (draggedCenter < leftCenter) {
                        MinigameLayoutConfig.get().moveHidden(draggedIndex, draggedIndex - 1);
                        draggedIndex--;
                    } else {
                        break;
                    }
                }
                while (draggedIndex < hidden.size() - 1) {
                    float rightCenter = getBottomCardTargetX(draggedIndex + 1) + CARD_WIDTH / 2f;
                    if (draggedCenter > rightCenter) {
                        MinigameLayoutConfig.get().moveHidden(draggedIndex, draggedIndex + 1);
                        draggedIndex++;
                    } else {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging && draggedMinigame != null) {
            dragCurrentMouseX = (float) mouseX;
            dragCurrentMouseY = (float) mouseY;
            visualXMap.put(draggedMinigame.getId(), dragCurrentMouseX - dragOffsetX);
            checkDragReorder();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDragging && draggedMinigame != null) {
            float midY = (currentTopY + CARD_HEIGHT + currentBottomY) / 2f;
            if (dragSourceTop) {
                if (mouseY >= midY) {
                    List<MinigameDefinition> hiddenBefore = MinigameLayoutConfig.get().getHiddenList();
                    boolean wasHiddenEmpty = hiddenBefore.isEmpty();
                    int targetIdx = getBottomDropIndex((float) mouseX);
                    MinigameLayoutConfig.get().moveToHidden(draggedMinigame.getId(), targetIdx);
                    if (wasHiddenEmpty) {
                        bottomSelectedIndex = 0;
                        bottomAnimOffset = 0f;
                    }
                    visualXMap.put(draggedMinigame.getId(), (float) mouseX - dragOffsetX);
                }
            } else {
                if (mouseY < midY) {
                    List<MinigameDefinition> visibleBefore = MinigameLayoutConfig.get().getVisibleList();
                    boolean wasVisibleEmpty = visibleBefore.isEmpty();
                    int targetIdx = getTopDropIndex((float) mouseX);
                    MinigameLayoutConfig.get().moveToVisible(draggedMinigame.getId(), targetIdx);
                    if (wasVisibleEmpty) {
                        topSelectedIndex = 0;
                        topAnimOffset = 0f;
                    }
                    visualXMap.put(draggedMinigame.getId(), (float) mouseX - dragOffsetX);
                }
            }
            isDragging = false;
            draggedMinigame = null;
            lastEdgeScrollTime = 0;
            clampIndices();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float midY = (currentTopY + CARD_HEIGHT + currentBottomY) / 2f;
        if (mouseY < midY) {
            if (verticalAmount < 0 || horizontalAmount < 0) {
                scrollTopRight();
            } else if (verticalAmount > 0 || horizontalAmount > 0) {
                scrollTopLeft();
            }
        } else {
            if (verticalAmount < 0 || horizontalAmount < 0) {
                scrollBottomRight();
            } else if (verticalAmount > 0 || horizontalAmount > 0) {
                scrollBottomLeft();
            }
        }
        if (isDragging) {
            checkDragReorder();
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            scrollTopLeft();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            scrollTopRight();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void clampIndices() {
        List<MinigameDefinition> visible = MinigameLayoutConfig.get().getVisibleList();
        if (visible.isEmpty()) {
            topSelectedIndex = 0;
        } else {
            topSelectedIndex = Math.max(0, Math.min(topSelectedIndex, visible.size() - 1));
        }

        List<MinigameDefinition> hidden = MinigameLayoutConfig.get().getHiddenList();
        if (hidden.isEmpty()) {
            bottomSelectedIndex = 0;
        } else {
            bottomSelectedIndex = Math.max(0, Math.min(bottomSelectedIndex, hidden.size() - 1));
        }
    }

    private int getTopCardTargetX(int index) {
        int centerX = this.width / 2;
        float slotOffset = (index - topSelectedIndex) * (CARD_WIDTH + CARD_SPACING) + topAnimOffset;
        return centerX + (int) slotOffset - CARD_WIDTH / 2;
    }

    private int getBottomCardTargetX(int index) {
        int centerX = this.width / 2;
        float slotOffset = (index - bottomSelectedIndex) * (CARD_WIDTH + CARD_SPACING) + bottomAnimOffset;
        return centerX + (int) slotOffset - CARD_WIDTH / 2;
    }

    private int getTopDropIndex(float mouseX) {
        List<MinigameDefinition> visible = MinigameLayoutConfig.get().getVisibleList();
        if (visible.isEmpty()) {
            return 0;
        }
        float slotW = CARD_WIDTH + CARD_SPACING;
        float startX = this.width / 2f - topSelectedIndex * slotW + topAnimOffset;
        int idx = Math.round((mouseX - startX) / slotW);
        return Math.max(0, Math.min(idx, visible.size()));
    }

    private int getBottomDropIndex(float mouseX) {
        List<MinigameDefinition> hidden = MinigameLayoutConfig.get().getHiddenList();
        if (hidden.isEmpty()) {
            return 0;
        }
        float slotW = CARD_WIDTH + CARD_SPACING;
        float startX = this.width / 2f - bottomSelectedIndex * slotW + bottomAnimOffset;
        int idx = Math.round((mouseX - startX) / slotW);
        return Math.max(0, Math.min(idx, hidden.size()));
    }

    private void handleEdgeScrolling() {
        if (!isDragging || draggedMinigame == null) {
            lastEdgeScrollTime = 0;
            return;
        }

        int edgeThreshold = 45;
        boolean inLeft = dragCurrentMouseX < edgeThreshold;
        boolean inRight = dragCurrentMouseX > this.width - edgeThreshold;

        if (!inLeft && !inRight) {
            lastEdgeScrollTime = 0;
            return;
        }

        long now = System.currentTimeMillis();
        if (lastEdgeScrollTime == 0 || now - lastEdgeScrollTime >= 500) {
            float midY = (currentTopY + CARD_HEIGHT + currentBottomY) / 2f;
            boolean inTop = dragCurrentMouseY < midY;
            if (inTop) {
                if (inLeft) {
                    scrollTopLeft();
                } else {
                    scrollTopRight();
                }
            } else {
                if (inLeft) {
                    scrollBottomLeft();
                } else {
                    scrollBottomRight();
                }
            }
            lastEdgeScrollTime = now;
            checkDragReorder();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = System.currentTimeMillis();
        float dt = Math.min(0.05f, (now - lastFrameTime) / 1000f);
        lastFrameTime = now;

        float animSpeed = 4.2f;
        if (!isClosing) {
            transitionProgress = Math.min(1.0f, transitionProgress + dt * animSpeed);
        } else {
            transitionProgress = Math.max(0.0f, transitionProgress - dt * animSpeed);
            if (transitionProgress <= 0.0f) {
                this.client.setScreen(new MinigameListScreen(parent, topSelectedIndex, topAnimOffset));
                return;
            }
        }

        float t = (float) Math.sin(transitionProgress * Math.PI / 2.0);

        int normalCardY = this.height / 2 - CARD_HEIGHT / 2 - 10;
        int targetTopY = this.height < 300 ? 34 : 44;

        int minBottomY = targetTopY + CARD_HEIGHT + 20;
        int preferredBottomY = this.height - CARD_HEIGHT - 32;
        int targetBottomY = Math.max(minBottomY, preferredBottomY);

        currentTopY = (int) MathHelper.lerp(t, (float) normalCardY, (float) targetTopY);
        currentBottomY = (int) MathHelper.lerp(t, (float) (this.height + 20), (float) targetBottomY);

        int topArrowY = currentTopY + CARD_HEIGHT / 2 - ARROW_SIZE / 2;
        if (leftTopArrow != null) {
            leftTopArrow.setY(topArrowY);
        }
        if (rightTopArrow != null) {
            rightTopArrow.setY(topArrowY);
        }

        int bottomArrowY = currentBottomY + CARD_HEIGHT / 2 - ARROW_SIZE / 2;
        if (leftBottomArrow != null) {
            leftBottomArrow.setY(bottomArrowY);
        }
        if (rightBottomArrow != null) {
            rightBottomArrow.setY(bottomArrowY);
        }

        if (!isDragging) {
            if (Math.abs(topAnimOffset) > 0.5f) {
                topAnimOffset *= 1f - ANIM_SPEED;
            } else {
                topAnimOffset = 0f;
            }

            if (Math.abs(bottomAnimOffset) > 0.5f) {
                bottomAnimOffset *= 1f - ANIM_SPEED;
            } else {
                bottomAnimOffset = 0f;
            }
        }

        handleEdgeScrolling();

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, -1);

        renderTopRow(context, mouseX, mouseY);

        if (currentBottomY < this.height) {
            renderBottomRow(context, mouseX, mouseY);
        }

        renderDraggingCard(context, mouseX, mouseY);

        if (leftTopArrow != null) {
            leftTopArrow.render(context, mouseX, mouseY, delta);
        }
        if (rightTopArrow != null) {
            rightTopArrow.render(context, mouseX, mouseY, delta);
        }
        if (currentBottomY < this.height) {
            if (leftBottomArrow != null) {
                leftBottomArrow.render(context, mouseX, mouseY, delta);
            }
            if (rightBottomArrow != null) {
                rightBottomArrow.render(context, mouseX, mouseY, delta);
            }
        }

        if (backBtn != null) {
            backBtn.render(context, mouseX, mouseY, delta);
        }
    }

    private void renderTopRow(DrawContext context, int mouseX, int mouseY) {
        List<MinigameDefinition> visible = MinigameLayoutConfig.get().getVisibleList();
        if (visible.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.minigames.layout.visible_empty"), this.width / 2, currentTopY + CARD_HEIGHT / 2 - 4, 0xFF888888);
            return;
        }

        for (int i = 0; i < visible.size(); i++) {
            MinigameDefinition mg = visible.get(i);
            int targetX = getTopCardTargetX(i);
            float currentX = visualXMap.computeIfAbsent(mg.getId(), k -> (float) targetX);

            if (isDragging && dragSourceTop && mg == draggedMinigame) {
                boolean insideTop = (dragCurrentMouseY >= currentTopY - 12 && dragCurrentMouseY <= currentTopY + CARD_HEIGHT + 12);
                if (insideTop) {
                    currentX = dragCurrentMouseX - dragOffsetX;
                    visualXMap.put(mg.getId(), currentX);
                } else {
                    continue;
                }
            } else {
                currentX += (targetX - currentX) * 0.35f;
                visualXMap.put(mg.getId(), currentX);
            }

            int cardX = Math.round(currentX);
            int cardY = currentTopY;
            float scale = getTopCardScale(currentX);

            int cx = cardX + CARD_WIDTH / 2;
            int cy = cardY + CARD_HEIGHT / 2;
            float localMouseX = ((float) mouseX - cx) / scale + cx;
            float localMouseY = ((float) mouseY - cy) / scale + cy;

            boolean hovered = !isDragging && localMouseX >= cardX && localMouseX <= cardX + CARD_WIDTH && localMouseY >= cardY && localMouseY <= cardY + CARD_HEIGHT;
            int badgeX = cardX + CARD_WIDTH - BADGE_SIZE - 1;
            int badgeY = cardY + 1;
            boolean badgeHovered = !isDragging && localMouseX >= badgeX && localMouseX <= badgeX + BADGE_SIZE && localMouseY >= badgeY && localMouseY <= badgeY + BADGE_SIZE;

            drawCard(context, mg, cardX, cardY, scale, hovered, true, false, badgeHovered);
        }
    }

    private void renderBottomRow(DrawContext context, int mouseX, int mouseY) {
        List<MinigameDefinition> hidden = MinigameLayoutConfig.get().getHiddenList();
        if (hidden.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.minigames.layout.hidden_empty"), this.width / 2, currentBottomY + CARD_HEIGHT / 2 - 4, 0xFF888888);
            return;
        }

        for (int j = 0; j < hidden.size(); j++) {
            MinigameDefinition mg = hidden.get(j);
            int targetX = getBottomCardTargetX(j);
            float currentX = visualXMap.computeIfAbsent(mg.getId(), k -> (float) targetX);

            if (isDragging && !dragSourceTop && mg == draggedMinigame) {
                boolean insideBottom = (dragCurrentMouseY >= currentBottomY - 12);
                if (insideBottom) {
                    currentX = dragCurrentMouseX - dragOffsetX;
                    visualXMap.put(mg.getId(), currentX);
                } else {
                    continue;
                }
            } else {
                currentX += (targetX - currentX) * 0.35f;
                visualXMap.put(mg.getId(), currentX);
            }

            int cardX = Math.round(currentX);
            int cardY = currentBottomY;
            float scale = 1.0f;

            boolean hovered = !isDragging && mouseX >= cardX && mouseX <= cardX + CARD_WIDTH && mouseY >= cardY && mouseY <= cardY + CARD_HEIGHT;
            int badgeX = cardX + CARD_WIDTH - BADGE_SIZE - 1;
            int badgeY = cardY + 1;
            boolean badgeHovered = !isDragging && mouseX >= badgeX && mouseX <= badgeX + BADGE_SIZE && mouseY >= badgeY && mouseY <= badgeY + BADGE_SIZE;

            drawCard(context, mg, cardX, cardY, scale, hovered, true, true, badgeHovered);
        }
    }

    private void renderDraggingCard(DrawContext context, int mouseX, int mouseY) {
        if (!isDragging || draggedMinigame == null) {
            return;
        }

        boolean insideTop = dragSourceTop && (dragCurrentMouseY >= currentTopY - 12 && dragCurrentMouseY <= currentTopY + CARD_HEIGHT + 12);
        boolean insideBottom = !dragSourceTop && (dragCurrentMouseY >= currentBottomY - 12);

        if (!insideTop && !insideBottom) {
            int dragX = Math.round(dragCurrentMouseX - dragOffsetX);
            int dragY = Math.round(dragCurrentMouseY - dragOffsetY);

            drawCard(context, draggedMinigame, dragX, dragY, 1.0f, true, false, false, false);
        }
    }

    private void drawCard(DrawContext context, MinigameDefinition mg, int x, int y, float scale, boolean hovered, boolean showBadge, boolean isPlusBadge, boolean badgeHovered) {
        int cx = x + CARD_WIDTH / 2;
        int cy = y + CARD_HEIGHT / 2;

        context.getMatrices().push();
        context.getMatrices().translate(cx, cy, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-cx, -cy, 0);

        int w = CARD_WIDTH;
        int h = CARD_HEIGHT;
        int borderColor = hovered ? 0xFFFFFFFF : 0xFFCECECE;

        context.fill(x, y, x + w, y + BORDER, borderColor);
        context.fill(x, y + h - BORDER, x + w, y + h, borderColor);
        context.fill(x, y, x + BORDER, y + h, borderColor);
        context.fill(x + w - BORDER, y, x + w, y + h, borderColor);

        int imgArea = h - LABEL_HEIGHT - BORDER * 2;
        int imgX = x + BORDER;
        int imgY = y + BORDER;
        int imgW = w - BORDER * 2;

        context.fill(imgX, imgY, imgX + imgW, imgY + imgArea, 0xFF666666);

        Identifier icon = mg.getIcon();
        if (icon != null) {
            context.drawTexture(icon, imgX, imgY, 0, 0, imgW, imgArea, imgW, imgArea);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("?").styled(s -> s.withBold(true)), x + w / 2, imgY + imgArea / 2, 0xFFFFFFFF);
        }

        int labelY = y + h - LABEL_HEIGHT - BORDER + 2;
        context.fill(x + BORDER, labelY - 2, x + w - BORDER, y + h - BORDER, 0xCC333333);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(mg.getDisplayName().getString()).styled(style -> style.withBold(true)), x + w / 2, labelY + 1, 0xFFFFFFFF);

        if (showBadge) {
            int badgeX = x + CARD_WIDTH - BADGE_SIZE - 1;
            int badgeY = y + 1;
            Identifier badgeTexture;
            if (isPlusBadge) {
                badgeTexture = badgeHovered ? BADGE_PLUS_HOVER : BADGE_PLUS;
            } else {
                badgeTexture = badgeHovered ? BADGE_MINUS_HOVER : BADGE_MINUS;
            }
            context.drawGuiTexture(badgeTexture, badgeX, badgeY, BADGE_SIZE, BADGE_SIZE);
        }

        context.getMatrices().pop();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        context.drawHorizontalLine(0, this.width, currentTopY - 4, -1);
        context.drawHorizontalLine(0, this.width, currentTopY + CARD_HEIGHT + 4, -1);
        context.fill(0, currentTopY - 4, this.width, currentTopY + CARD_HEIGHT + 4, 0x30FFFFFF);

        if (currentBottomY < this.height) {
            context.drawHorizontalLine(0, this.width, currentBottomY - 4, -1);
            context.fill(0, currentBottomY - 4, this.width, this.height, 0x30FFFFFF);
        }
    }
}
