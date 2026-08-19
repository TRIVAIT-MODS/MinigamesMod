package org.trivait.minigamesmod.minigame.cookieclicker.gui;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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

public class ShopWidget extends ClickableWidget {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of(MinigamesMod.MOD_ID, "minigame/cookie_clicker/shop_background");
    private static final Identifier SHOP_ENTRY_TEXTURE = Identifier.of(MinigamesMod.MOD_ID, "minigame/cookie_clicker/shop_entry_background");
    private CookieClickerScreen screen;

    private List<ShopEntry> entries;
    public float scroll = 0;

    public ShopWidget(int x, int y, int width, int height, CookieClickerScreen screen) {
        super(x, y, width, height, Text.empty());
        this.screen = screen;

        entries = List.of(
                new ShopEntry(Text.translatable("minigame.cookieclicker.upgrade_click"),
                        () -> screen.clickUpgradePrice,
                        Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.cookiesPerClick++;
                                screen.clickUpgradeCount++;
                                screen.clickUpgradePrice = (int)(screen.clickUpgradePrice * 1.02f);
                            }
                        }, () -> screen.clickUpgradeCount, Text.translatable("minigame.cookieclicker.upgrade_click.desc"), 2),
                new ShopEntry(Text.translatable("minigame.cookieclicker.wooden_cursor"),
                        () -> screen.woodenCursorPrice,
                        Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/wooden_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=0.5f;
                                screen.woodenCursorCount++;
                                screen.woodenCursorPrice = (int)(screen.woodenCursorPrice * 1.03f);
                            }
                        }, () -> screen.woodenCursorCount, Text.translatable("minigame.cookieclicker.wooden_cursor.desc"), 3),
                new ShopEntry(Text.translatable("minigame.cookieclicker.stone_cursor"),
                        () -> screen.stoneCursorPrice,
                        Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/stone_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=3.5f;
                                screen.stoneCursorCount++;
                                screen.stoneCursorPrice = (int)(screen.stoneCursorPrice * 1.04f);
                            }
                        }, () -> screen.stoneCursorCount, Text.translatable("minigame.cookieclicker.stone_cursor.desc"), 4),
                new ShopEntry(Text.translatable("minigame.cookieclicker.copper_cursor"),
                        () -> screen.copperCursorPrice,
                        Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/copper_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=20.0f;
                                screen.copperCursorCount++;
                                screen.copperCursorPrice = (int)(screen.copperCursorPrice * 1.04f);
                            }
                        }, () -> screen.copperCursorCount, Text.translatable("minigame.cookieclicker.copper_cursor.desc"), 4),
                new ShopEntry(Text.translatable("minigame.cookieclicker.golden_cursor"),
                        () -> screen.goldenCursorPrice,
                        Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/golden_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=100.0f;
                                screen.goldenCursorCount++;
                                screen.goldenCursorPrice = (int)(screen.goldenCursorPrice * 1.05f);
                            }
                        }, () -> screen.goldenCursorCount, Text.translatable("minigame.cookieclicker.golden_cursor.desc"), 5),
                new ShopEntry(Text.translatable("minigame.cookieclicker.iron_cursor"),
                        () -> screen.ironCursorPrice,
                        Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/iron_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=500.0f;
                                screen.ironCursorCount++;
                                screen.ironCursorPrice = (int)(screen.ironCursorPrice * 1.05f);
                            }
                        }, () -> screen.ironCursorCount, Text.translatable("minigame.cookieclicker.iron_cursor.desc"), 5),
                new ShopEntry(Text.translatable("minigame.cookieclicker.diamond_cursor"),
                        () -> screen.diamondCursorPrice,
                        Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/diamond_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=1000.0f;
                                screen.diamondCursorCount++;
                                screen.diamondCursorPrice = (int)(screen.diamondCursorPrice * 1.04f);
                            }
                        }, () -> screen.diamondCursorCount, Text.translatable("minigame.cookieclicker.diamond_cursor.desc"), 4),
                new ShopEntry(Text.translatable("minigame.cookieclicker.netherite_cursor"),
                        () -> screen.netheriteCursorPrice,
                        Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/netherite_cursor.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.CPS+=10000.0f;
                                screen.netheriteCursorCount++;
                                screen.netheriteCursorPrice = (int)(screen.netheriteCursorPrice * 1.03f);
                            }
                        }, () -> screen.netheriteCursorCount, Text.translatable("minigame.cookieclicker.netherite_cursor.desc"), 3),
                new ShopEntry(Text.translatable("minigame.cookieclicker.clicker_doubler"),
                        () -> screen.clickDoublerPrice,
                        Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/cursor_doubler.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.cookies-=entry.price.getAsInt();
                                screen.cookiesPerClick*=2;
                                screen.clickDoublerCount++;
                                screen.clickDoublerPrice = (int)(screen.clickDoublerPrice * 3.5f);
                            }
                        }, () -> screen.clickDoublerCount, Text.translatable("minigame.cookieclicker.clicker_doubler.desc"), 250),
                new ShopEntry(Text.translatable("item.minecraft.amethyst_shard"),
                        () -> 500000000,
                        Identifier.ofVanilla("textures/item/amethyst_shard.png"),
                        (mc, entry, action) -> {
                            if (screen.cookies>=entry.price.getAsInt()) {
                                screen.clearGame();
                                PlayingSoundManager.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
                                screen.minigame.getLeaderboard().doPost(screen.playerName, 1);
                            }
                        }, () -> 0, Text.translatable("minigame.cookieclicker.amethyst_shard.desc"), 0)
        );
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        context.drawGuiTexture(RenderLayer::getGuiTextured, BACKGROUND_TEXTURE, getX(), getY(), getWidth(), getHeight());
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        SoundEvent sound = MinigameRegistry.getConfig(CookieClickerVisualConfig.class).cookieClickerSounds ? ModSounds.BUY : SoundEvents.UI_BUTTON_CLICK.value();
        if (this.active && this.visible&&isMouseOverEntries(mouseX, mouseY)) {
            if (this.isValidClickButton(button)) {
                boolean bl = this.isMouseOver(mouseX, mouseY);
                if (bl) {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, 1, CookieClickerScreen.vol()));
                    this.onClick(mouseX, mouseY);
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

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    public class ShopEntry {
        private final Text name;
        private IntSupplier price;
        private final Identifier texture;
        private final ShopEvent onClick;
        private final IntSupplier getCount;
        private int x;
        private int y;
        private int width;
        private int height;
        private final Text tooltipText;
        private final int procent;

        public ShopEntry(Text name, IntSupplier price, Identifier texture, ShopEvent onClick, IntSupplier getCount, Text tooltipText, int procent) {
            this.name = name;
            this.price = price;
            this.texture = texture;
            this.onClick = onClick;
            this.getCount = getCount;
            this.tooltipText = tooltipText;
            this.procent = procent;
        }

        public void render(DrawContext ctx, int x, int y, int width, int height, CookieClickerScreen screen, int mouseX, int mouseY) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            ctx.drawGuiTexture(RenderLayer::getGuiTextured, SHOP_ENTRY_TEXTURE, x, y, width, height);
            ctx.drawTexture(RenderLayer::getGuiTextured, texture, x, y, 0, 0, height, height, height, height);
            String priceString = CookieClickerScreen.cookiesToString(price.getAsInt());
            float textScale = getTextScale(textRenderer, priceString);
            float textHeight = textRenderer.fontHeight * textScale;
            float halfHeight = height / 2.0f;
            float nameY = y + (halfHeight - textHeight) / 2.0f;
            float priceY = y + halfHeight + (halfHeight - textHeight) / 2.0f;
            ctx.getMatrices().push();
            ctx.getMatrices().translate(x + height + 5, nameY, 0);
            ctx.getMatrices().scale(textScale, textScale, 1.0f);
            ctx.drawText(textRenderer, name, 0, 0, screen.cookies < price.getAsInt() ? 0xFFA0A0A0 : 0xFFFFFFFF, true);
            ctx.getMatrices().pop();
            String countString = "" + getCount.getAsInt();
            ctx.getMatrices().push();
            ctx.getMatrices().scale(1.5f, 1.5f, 1.0f);
            ctx.drawText(textRenderer, countString, (int)((x + width - 5 - textRenderer.getWidth(countString) - 4) / 1.5f), (int)((y + height / 2 - 5) / 1.5f), 0x80000000, false);
            ctx.getMatrices().pop();
            ctx.getMatrices().push();
            ctx.getMatrices().translate(x + height + 5, priceY, 0);
            ctx.getMatrices().scale(textScale, textScale, 1.0f);
            ctx.drawText(textRenderer, priceString, 0, 0, screen.cookies >= price.getAsInt() ? Colors.GREEN : Colors.RED, false);
            ctx.getMatrices().pop();
            if (screen.cookies < price.getAsInt()) ctx.fill(x, y, x + width, y + height, 0x60000000);
            else if (isMouseOver(mouseX, mouseY)) ctx.fill(x, y, x + width, y + height, 0x20FFFFFF);
        }

        private float getTextScale(TextRenderer textRenderer, String priceString) {
            int availableWidth = width - height - 12;
            int maxWidth = Math.max(textRenderer.getWidth(name), textRenderer.getWidth(priceString));
            return Math.min(1.0f, (float) availableWidth / Math.max(1, maxWidth));
        }

        public void renderTooltip(DrawContext ctx, int mouseY) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int border = 3;
            int tooltipWidth = width / 3 * 2;
            int tooltipHeight = height;
            int startX = x - tooltipWidth - 4;
            int startY = mouseY - tooltipHeight / 2;
            int contentWidth = tooltipWidth - border * 2;
            int contentHeight = tooltipHeight - border * 2;

            String description = tooltipText.getString();
            String percentText = procent > 0 ? "+"+procent+Text.translatable("minigame.cookieclicker.procent_per_click").getString() : "";

            int descriptionWidth = textRenderer.getWidth(description);
            int percentWidth = textRenderer.getWidth(percentText);
            int maxWidth = Math.max(descriptionWidth, percentWidth);

            int lines = (!description.isEmpty() ? 1 : 0) + (!percentText.isEmpty() ? 1 : 0);

            float textScale = Math.min(1.0f, (float) contentWidth / Math.max(1, maxWidth));
            textScale = Math.min(textScale, (float) contentHeight / Math.max(1, lines * textRenderer.fontHeight));

            int lineHeight = (int) (textRenderer.fontHeight * textScale);
            int totalTextHeight = lines * lineHeight;

            ctx.drawGuiTexture(RenderLayer::getGuiTextured, BACKGROUND_TEXTURE, startX, startY, tooltipWidth, tooltipHeight);

            int currentY = startY + (tooltipHeight - totalTextHeight) / 2 - 1;

            if (!description.isEmpty()) {
                int scaledWidth = (int) (descriptionWidth * textScale);
                int textX = startX + (tooltipWidth - scaledWidth) / 2;

                ctx.getMatrices().push();
                ctx.getMatrices().translate(textX, currentY, 0);
                ctx.getMatrices().scale(textScale, textScale, 1.0f);
                ctx.drawText(textRenderer, description, 0, 0, Colors.WHITE, true);
                ctx.getMatrices().pop();

                currentY += lineHeight;
            }

            if (!percentText.isEmpty()) {
                int scaledWidth = (int) (percentWidth * textScale);
                int textX = startX + (tooltipWidth - scaledWidth) / 2;

                ctx.getMatrices().push();
                ctx.getMatrices().translate(textX, currentY + 2, 0);
                ctx.getMatrices().scale(textScale, textScale, 1.0f);
                ctx.drawText(textRenderer, percentText, 0, 0, Colors.GREEN, false);
                ctx.getMatrices().pop();
            }
        }

        public void click(int action) {
            onClick.run(MinecraftClient.getInstance(), this, action);
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= (double)x && mouseY >= (double)y && mouseX < (double)(x + this.width) && mouseY < (double)(y + this.height);
        }
    }
}
