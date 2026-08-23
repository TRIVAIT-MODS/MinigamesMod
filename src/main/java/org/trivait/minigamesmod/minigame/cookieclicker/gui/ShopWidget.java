package org.trivait.minigamesmod.minigame.cookieclicker.gui;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonColors;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.ModSounds;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.leaderboard.Leaderboard;
import org.trivait.minigamesmod.minigame.cookieclicker.CookieClickerScreen;
import org.trivait.minigamesmod.minigame.cookieclicker.CookieClickerVisualConfig;
import org.trivait.minigamesmod.minigame.cookieclicker.util.ShopEvent;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ShopWidget extends AbstractWidget {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "minigame/cookie_clicker/shop_background");
    private static final Identifier SHOP_ENTRY_TEXTURE = Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "minigame/cookie_clicker/shop_entry_background");
    private CookieClickerScreen screen;

    private List<ShopEntry> entries;
    public float scroll = 0;

    public ShopWidget(int x, int y, int width, int height, CookieClickerScreen screen) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;

        entries = List.of(
                new ShopEntry(Component.translatable("minigame.cookieclicker.upgrade_click"),
                        () -> screen.clickUpgradePrice,
                        Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.cookiesPerClick++;
                                screen.clickUpgradeCount++;
                                screen.clickUpgradePrice = (int)(screen.clickUpgradePrice * 1.02f);
                            }
                        }, () -> screen.clickUpgradeCount, Component.translatable("minigame.cookieclicker.upgrade_click.desc"), 2),
                new ShopEntry(Component.translatable("minigame.cookieclicker.wooden_cursor"),
                        () -> screen.woodenCursorPrice,
                        Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/wooden_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=0.5f;
                                screen.woodenCursorCount++;
                                screen.woodenCursorPrice = (int)(screen.woodenCursorPrice * 1.03f);
                            }
                        }, () -> screen.woodenCursorCount, Component.translatable("minigame.cookieclicker.wooden_cursor.desc"), 3),
                new ShopEntry(Component.translatable("minigame.cookieclicker.stone_cursor"),
                        () -> screen.stoneCursorPrice,
                        Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/stone_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=3.5f;
                                screen.stoneCursorCount++;
                                screen.stoneCursorPrice = (int)(screen.stoneCursorPrice * 1.04f);
                            }
                        }, () -> screen.stoneCursorCount, Component.translatable("minigame.cookieclicker.stone_cursor.desc"), 4),
                new ShopEntry(Component.translatable("minigame.cookieclicker.copper_cursor"),
                        () -> screen.copperCursorPrice,
                        Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/copper_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=20.0f;
                                screen.copperCursorCount++;
                                screen.copperCursorPrice = (int)(screen.copperCursorPrice * 1.04f);
                            }
                        }, () -> screen.copperCursorCount, Component.translatable("minigame.cookieclicker.copper_cursor.desc"), 4),
                new ShopEntry(Component.translatable("minigame.cookieclicker.golden_cursor"),
                        () -> screen.goldenCursorPrice,
                        Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/golden_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=100.0f;
                                screen.goldenCursorCount++;
                                screen.goldenCursorPrice = (int)(screen.goldenCursorPrice * 1.05f);
                            }
                        }, () -> screen.goldenCursorCount, Component.translatable("minigame.cookieclicker.golden_cursor.desc"), 5),
                new ShopEntry(Component.translatable("minigame.cookieclicker.iron_cursor"),
                        () -> screen.ironCursorPrice,
                        Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/iron_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=500.0f;
                                screen.ironCursorCount++;
                                screen.ironCursorPrice = (int)(screen.ironCursorPrice * 1.05f);
                            }
                        }, () -> screen.ironCursorCount, Component.translatable("minigame.cookieclicker.iron_cursor.desc"), 5),
                new ShopEntry(Component.translatable("minigame.cookieclicker.diamond_cursor"),
                        () -> screen.diamondCursorPrice,
                        Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/diamond_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=1000.0f;
                                screen.diamondCursorCount++;
                                screen.diamondCursorPrice = (int)(screen.diamondCursorPrice * 1.04f);
                            }
                        }, () -> screen.diamondCursorCount, Component.translatable("minigame.cookieclicker.diamond_cursor.desc"), 4),
                new ShopEntry(Component.translatable("minigame.cookieclicker.netherite_cursor"),
                        () -> screen.netheriteCursorPrice,
                        Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/netherite_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=10000.0f;
                                screen.netheriteCursorCount++;
                                screen.netheriteCursorPrice = (int)(screen.netheriteCursorPrice * 1.03f);
                            }
                        }, () -> screen.netheriteCursorCount, Component.translatable("minigame.cookieclicker.netherite_cursor.desc"), 3),
                new ShopEntry(Component.translatable("minigame.cookieclicker.clicker_doubler"),
                        () -> screen.clickDoublerPrice,
                        Identifier.fromNamespaceAndPath(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/cursor_doubler.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.cookiesPerClick*=2;
                                screen.clickDoublerCount++;
                                screen.clickDoublerPrice = (int)(screen.clickDoublerPrice * 3.5f);
                            }
                        }, () -> screen.clickDoublerCount, Component.translatable("minigame.cookieclicker.clicker_doubler.desc"), 250),
                new ShopEntry(Component.translatable("item.minecraft.amethyst_shard"),
                        () -> 350000000,
                        Identifier.withDefaultNamespace("textures/item/amethyst_shard.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.clearGame();
                                PlayingSoundManager.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
                                screen.minigame.getLeaderboard().doPost(1);
                            }
                        }, () -> 0, Component.translatable("minigame.cookieclicker.amethyst_shard.desc"), 0)
        );
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, getX(), getY(), getWidth(), getHeight());
        int entryWidth = width-6;
        int entryHeight = (height-6)/8;

        int entryY = getY()+3;
        int entryX = getX()+3;

        int visibleHeight = height-6;

        context.enableScissor(entryX-width-3, entryY, entryX+entryWidth, entryY+visibleHeight);
        for (ShopEntry shopEntry : entries) {
            shopEntry.render(context, entryX, (int) (entryY+scroll), entryWidth, entryHeight, screen, mouseX, mouseY);
            entryY+=entryHeight+1;
        }
        context.disableScissor();
        int listTop = getY() + 3;
        int listBottom = listTop + height - 6;

        if (mouseY >= listTop && mouseY < listBottom) {
            for (ShopEntry shopEntry : entries) {
                if (shopEntry.isMouseOver(mouseX, mouseY)) {
                    shopEntry.renderTooltip(context, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        SoundEvent sound = MinigameRegistry.getConfig(CookieClickerVisualConfig.class).cookieClickerSounds ? ModSounds.BUY : SoundEvents.UI_BUTTON_CLICK.value();
        if (this.active && this.visible&&isMouseOverEntries(mouseX, mouseY)) {
            if (this.isValidClickButton(click.buttonInfo())) {
                boolean bl = this.isMouseOver(mouseX, mouseY);
                if (bl) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1, CookieClickerScreen.vol()));
                    this.onClick(click, doubled);
                    for (ShopEntry entry : entries) {
                        if (entry.isMouseOver(mouseX, mouseY)) {
                            entry.click(button);
                        }
                    }
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float entryHeight = (height - 6) / 8;
        float contentHeight = entries.size() * (entryHeight + 1) - 1;
        float visibleHeight = height-6;

        float maxScroll = Math.min(0, visibleHeight - contentHeight);

        scroll += (float) verticalAmount * 10.0f;
        scroll = Math.max(maxScroll, Math.min(0, scroll));

        return true;
    }

    public boolean isMouseOverEntries(double mouseX, double mouseY) {
        for (ShopEntry shopEntry : entries) {
            if (shopEntry.isMouseOver(mouseX, mouseY)&&shopEntry.price.getAsInt()<=screen.cookies) return true;
        }

        return false;
    }

    public class ShopEntry {
        private final Component name;
        private IntSupplier price;
        private final Identifier texture;
        private final ShopEvent onClick;
        private final IntSupplier getCount;
        private int x;
        private int y;
        private int width;
        private int height;
        private final Component tooltipText;
        private final int procent;

        public ShopEntry(Component name, IntSupplier price, Identifier texture, ShopEvent onClick, IntSupplier getCount, Component tooltipText, int procent) {
            this.name = name;
            this.price = price;
            this.texture = texture;
            this.onClick = onClick;
            this.getCount = getCount;
            this.tooltipText = tooltipText;
            this.procent = procent;
        }

        public void render(GuiGraphicsExtractor ctx, int x, int y, int width, int height, CookieClickerScreen screen, int mouseX, int mouseY) {
            Font textRenderer = Minecraft.getInstance().font;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            ctx.blitSprite(RenderPipelines.GUI_TEXTURED, SHOP_ENTRY_TEXTURE, x, y, width, height);
            ctx.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, height, height, height, height);
            String priceString = CookieClickerScreen.cookiesToString(price.getAsInt());
            float textScale = getTextScale(textRenderer, priceString);
            float textHeight = textRenderer.lineHeight * textScale;
            float halfHeight = height / 2.0f;
            float nameY = y + (halfHeight - textHeight) / 2.0f;
            float priceY = y + halfHeight + (halfHeight - textHeight) / 2.0f;
            ctx.pose().pushMatrix();
            ctx.pose().translate(x + height + 5, nameY);
            ctx.pose().scale(textScale, textScale);
            ctx.text(textRenderer, name, 0, 0, -1, true);
            ctx.pose().popMatrix();
            String countString = "" + getCount.getAsInt();
            ctx.pose().pushMatrix();
            ctx.pose().scale(1.5f, 1.5f);
            ctx.text(textRenderer, countString, (int)((x + width - 5 - textRenderer.width(countString) - 4) / 1.5f), (int)((y + height / 2 - 5) / 1.5f), 0x80000000, false);
            ctx.pose().popMatrix();
            ctx.pose().pushMatrix();
            ctx.pose().translate(x + height + 5, priceY);
            ctx.pose().scale(textScale, textScale);
            ctx.text(textRenderer, priceString, 0, 0, screen.cookies >= price.getAsInt() ? CommonColors.GREEN : CommonColors.RED, false);
            ctx.pose().popMatrix();
            if (screen.cookies < price.getAsInt()) ctx.fill(x, y, x + width, y + height, 0x60000000);
            else if (isMouseOver(mouseX, mouseY)) ctx.fill(x, y, x + width, y + height, 0x20FFFFFF);
        }

        private float getTextScale(Font textRenderer, String priceString) {
            int availableWidth = width - height - 12;
            int maxWidth = Math.max(textRenderer.width(name), textRenderer.width(priceString));
            return Math.min(1.0f, (float) availableWidth / Math.max(1, maxWidth));
        }

        public void renderTooltip(GuiGraphicsExtractor ctx, int mouseY) {
            Font textRenderer = Minecraft.getInstance().font;
            int border = 3;
            int tooltipWidth = width / 3 * 2;
            int tooltipHeight = height;
            int startX = x - tooltipWidth - 4;
            int startY = mouseY - tooltipHeight / 2;
            int contentWidth = tooltipWidth - border * 2;
            int contentHeight = tooltipHeight - border * 2;

            String description = tooltipText.getString();
            String percentText = procent > 0 ? "+"+procent+Component.translatable("minigame.cookieclicker.procent_per_click").getString() : "";

            int descriptionWidth = textRenderer.width(description);
            int percentWidth = textRenderer.width(percentText);
            int maxWidth = Math.max(descriptionWidth, percentWidth);

            int lines = (!description.isEmpty() ? 1 : 0) + (!percentText.isEmpty() ? 1 : 0);

            float textScale = Math.min(1.0f, (float) contentWidth / Math.max(1, maxWidth));
            textScale = Math.min(textScale, (float) contentHeight / Math.max(1, lines * textRenderer.lineHeight));

            int lineHeight = (int) (textRenderer.lineHeight * textScale);
            int totalTextHeight = lines * lineHeight;

            ctx.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, startX, startY, tooltipWidth, tooltipHeight);

            int currentY = startY + (tooltipHeight - totalTextHeight) / 2 - 1;

            if (!description.isEmpty()) {
                int scaledWidth = (int) (descriptionWidth * textScale);
                int textX = startX + (tooltipWidth - scaledWidth) / 2;

                ctx.pose().pushMatrix();
                ctx.pose().translate(textX, currentY);
                ctx.pose().scale(textScale, textScale);
                ctx.text(textRenderer, description, 0, 0, CommonColors.WHITE, true);
                ctx.pose().popMatrix();

                currentY += lineHeight;
            }

            if (!percentText.isEmpty()) {
                int scaledWidth = (int) (percentWidth * textScale);
                int textX = startX + (tooltipWidth - scaledWidth) / 2;

                ctx.pose().pushMatrix();
                ctx.pose().translate(textX, currentY + 2);
                ctx.pose().scale(textScale, textScale);
                ctx.text(textRenderer, percentText, 0, 0, CommonColors.GREEN, false);
                ctx.pose().popMatrix();
            }
        }

        public void click(int action) {
            onClick.run(Minecraft.getInstance(), this, action);
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= (double)x && mouseY >= (double)y && mouseX < (double)(x + this.width) && mouseY < (double)(y + this.height);
        }
    }
}
