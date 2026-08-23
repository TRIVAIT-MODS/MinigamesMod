package org.trivait.minigamesmod.gui;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.trivait.minigamesmod.api.MinigameDefinition;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.config.Config;
import org.trivait.minigamesmod.gui.widget.ArrowButtonWidget;
import org.trivait.minigamesmod.gui.widget.MainConfigButton;
import org.trivait.minigamesmod.gui.widget.MinigameCardWidget;
import org.trivait.minigamesmod.leaderboard.Leaderboard;
import org.trivait.minigamesmod.leaderboard.SheetsApi;
import org.trivait.minigamesmod.minigame.minesweeper.screen.SelectLeaderboardScreen;

import java.util.ArrayList;
import java.util.List;

public class MinigameListScreen extends Screen {
    private static final int CARD_WIDTH = 90;
    private static final int CARD_HEIGHT = 110;
    private static final int CARD_SPACING = 20;
    private static final int ARROW_SIZE = 32;
    private static final float CENTER_SCALE = 1.2f;
    private static final float ANIM_SPEED = 0.15f;

    private final @Nullable Screen parent;
    private int selectedIndex = MinigameRegistry.getAll().size() / 2;
    private float animOffset = 0f;
    private final List<MinigameCardWidget> cards = new ArrayList<>();
    private final List<ArrowButtonWidget> arrows = new ArrayList<>();

    private ButtonWidget leaderboardBtn;
    private boolean leaderboardBlocked;
    private boolean leaderboardNotSupported;

    public MinigameListScreen(@Nullable Screen parent) {
        super(Text.translatable("screen.minigames.list.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cards.clear();
        arrows.clear();

        List<MinigameDefinition> all = List.copyOf(MinigameRegistry.getAll());

        int centerX = this.width / 2;
        int cardY = this.height / 2 - CARD_HEIGHT / 2 - 10;

        for (int i = 0; i < all.size(); i++) {
            MinigameDefinition mg = all.get(i);
            int cx = centerX + (i - selectedIndex) * (CARD_WIDTH + CARD_SPACING) - CARD_WIDTH / 2;

            MinigameCardWidget card = new MinigameCardWidget(cx, cardY, CARD_WIDTH, CARD_HEIGHT, mg, () -> {
                mg.onStart();
                this.client.setScreen(mg.createScreen(this));
            });

            cards.add(card);
            this.addDrawableChild(card);
        }

        updateCardScales(0f);

        int arrowY = this.height / 2 - ARROW_SIZE / 2 - 10;

        ArrowButtonWidget leftArrow = new ArrowButtonWidget(5, arrowY, ARROW_SIZE, ArrowButtonWidget.Direction.LEFT, this::scrollLeft);
        ArrowButtonWidget rightArrow = new ArrowButtonWidget(width - 32, arrowY, ARROW_SIZE, ArrowButtonWidget.Direction.RIGHT, this::scrollRight);

        arrows.add(leftArrow);
        arrows.add(rightArrow);

        this.addDrawableChild(leftArrow);
        this.addDrawableChild(rightArrow);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), b -> this.close()).dimensions(this.width / 2 - 50, this.height - 28, 100, 20).build());

        leaderboardBtn = ButtonWidget.builder(Text.translatable("minigame.leaderboard"), b -> {
            if (!leaderboardBtn.active) {
                return;
            }

            MinigameDefinition selected = all.get(selectedIndex);
            Leaderboard leaderboard = selected.getLeaderboard();

            if (leaderboard != null) {
                MinecraftClient.getInstance().setScreen(new LeaderboardScreen(leaderboard, this));
            } else if (selected.getId().equals("minesweeper")) {
                MinecraftClient.getInstance().setScreen(new SelectLeaderboardScreen(this));
            }
        }).dimensions(width / 2 - 35, height / 2 + 70, 70, 20).build();

        this.addDrawableChild(leaderboardBtn);
        this.addDrawableChild(new MainConfigButton(width-20-5, 5));
        updateActive();
    }

    private void scrollLeft() {
        if (selectedIndex > 0) {
            selectedIndex--;
            animOffset = -(CARD_WIDTH + CARD_SPACING);
            updateActive();
        }
    }

    private void scrollRight() {
        List<MinigameDefinition> all = List.copyOf(MinigameRegistry.getAll());

        if (selectedIndex < all.size() - 1) {
            selectedIndex++;
            animOffset = CARD_WIDTH + CARD_SPACING;
            updateActive();
        }
    }

    private void updateActive() {
        List<MinigameDefinition> all = List.copyOf(MinigameRegistry.getAll());

        if (all.isEmpty()) {
            leaderboardBlocked = true;
            leaderboardNotSupported = false;
            leaderboardBtn.active = false;
            return;
        }

        MinigameDefinition selected = all.get(selectedIndex);
        Leaderboard leaderboard = selected.getLeaderboard();
        boolean hasLeaderboard = leaderboard != null;
        boolean isMinesweeper = selected.getId().equals("minesweeper");

        leaderboardBlocked = !SheetsApi.leaderboardsEnabled;
        leaderboardNotSupported = !hasLeaderboard && !isMinesweeper;
        leaderboardBtn.active = !leaderboardBlocked && !leaderboardNotSupported;
    }

    private void updateCardScales(float offset) {
        int centerX = this.width / 2;
        int cardY = this.height / 2 - CARD_HEIGHT / 2 - 10;

        for (int i = 0; i < cards.size(); i++) {
            MinigameCardWidget card = cards.get(i);
            float slotOffset = (i - selectedIndex) * (CARD_WIDTH + CARD_SPACING) + offset;
            int cx = centerX + (int) slotOffset - CARD_WIDTH / 2;

            card.setX(cx);
            card.setY(cardY);

            float dist = Math.abs(slotOffset) / (CARD_WIDTH + CARD_SPACING);
            float scale = 1f + (CENTER_SCALE - 1f) * Math.max(0f, 1f - dist);

            card.setScale(scale);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ArrowButtonWidget arrow : arrows) {
            if (arrow.isMouseOver(mouseX, mouseY)) {
                return arrow.mouseClicked(mouseX, mouseY, button);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (Math.abs(animOffset) > 0.5f) {
            animOffset *= 1f - ANIM_SPEED;
        } else {
            animOffset = 0f;
        }

        updateCardScales(animOffset);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, -1);

        if (cards.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.minigames.list.empty"), this.width / 2, this.height / 2 - 10, 0xFF888888);
        }

        if (leaderboardBtn != null && mouseX >= leaderboardBtn.getX() && mouseX < leaderboardBtn.getX() + leaderboardBtn.getWidth() && mouseY >= leaderboardBtn.getY() && mouseY < leaderboardBtn.getY() + leaderboardBtn.getHeight()) {
            if (leaderboardBlocked) {
                context.drawTooltip(this.textRenderer, Text.translatable("minigame.leaderboard.disabled"), mouseX, mouseY);
            } else if (leaderboardNotSupported) {
                context.drawTooltip(this.textRenderer, Text.translatable("minigame.leaderboard.notSupported"), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0 || horizontalAmount < 0) {
            scrollRight();
        } else if (verticalAmount > 0 || horizontalAmount > 0) {
            scrollLeft();
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            scrollLeft();
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            scrollRight();
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        int cardY = this.height / 2 - CARD_HEIGHT / 2 - 10;

        context.drawHorizontalLine(0, this.width, cardY - 4, -1);
        context.drawHorizontalLine(0, this.width, cardY + CARD_HEIGHT + 4, -1);
        context.fill(0, cardY - 4, this.width, cardY + CARD_HEIGHT + 8, 0x30FFFFFF);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}