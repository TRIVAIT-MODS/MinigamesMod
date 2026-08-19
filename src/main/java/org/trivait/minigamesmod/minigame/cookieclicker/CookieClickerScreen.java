package org.trivait.minigamesmod.minigame.cookieclicker;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3x2fStack;
import org.trivait.minigamesmod.MinigamesMod;
import org.trivait.minigamesmod.api.MinigameRegistry;
import org.trivait.minigamesmod.api.PlayingSoundManager;
import org.trivait.minigamesmod.gui.widget.ConfigButton;
import org.trivait.minigamesmod.minigame.cookieclicker.gui.CookieWidget;
import org.trivait.minigamesmod.minigame.cookieclicker.gui.ShopWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CookieClickerScreen extends Screen {
    private Screen parent;
    public CookieClicker minigame;
    private CookieWidget cookieWidget;

    public String playerName;
    public double cookies;
    public float cookiesPerClick;
    public int clickUpgradeCount;
    public int clickUpgradePrice;
    public int woodenCursorCount;
    public int woodenCursorPrice;
    public int stoneCursorCount;
    public int stoneCursorPrice;
    public int copperCursorCount;
    public int copperCursorPrice;
    public int goldenCursorCount;
    public int goldenCursorPrice;
    public int ironCursorCount;
    public int ironCursorPrice;
    public int diamondCursorCount;
    public int diamondCursorPrice;
    public int netheriteCursorCount;
    public int netheriteCursorPrice;
    public int clickDoublerCount;
    public int clickDoublerPrice;
    public float CPS;

    private int ticker = 0;
    private float cookieSpawnCD = 0;
    private boolean loaded = false;

    private static final Identifier BACKGROUND_TEXTURE = Identifier.of(MinigamesMod.MOD_ID, "textures/minigame/cookie_clicker/background.png");

    private List<Cookie> cookiesWidgets = new ArrayList<>();

    public CookieClickerScreen(Screen parent, CookieClicker minigame) {
        super(Text.empty());
        this.parent = parent;
        this.minigame = minigame;
    }

    @Override
    protected void init() {
        int gameWidth = width - 80;
        int gameHeight = height - 40;
        int gameX = (width - gameWidth) / 2;
        int gameY = (height - gameHeight) / 2;

        int cookiePanelWidth = gameWidth / 3 * 2;
        int upgradePanelWidth = gameWidth - cookiePanelWidth;

        if (!loaded) {
            loaded=true;
            load();
        }

        ButtonWidget returnButton = TextIconButtonWidget.builder(Text.empty(), b -> close(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/return"), 15, 15)
                .build();
        returnButton.setTooltip(Tooltip.of(Text.translatable("minigame.2048.undo")));
        returnButton.setDimensionsAndPosition(20, 20, 10, 10);

        ButtonWidget restartButton = TextIconButtonWidget.builder(Text.empty(), b -> clearGame(), true)
                .texture(Identifier.of(MinigamesMod.MOD_ID, "icon/restart"), 15, 15)
                .build();
        restartButton.setTooltip(Tooltip.of(Text.translatable("minigame.restart")));
        restartButton.setDimensionsAndPosition(20, 20, 10, 35);

        addDrawableChild(returnButton);
        addDrawableChild(new ConfigButton(10, 60, minigame));

        int cookieHeight = gameHeight - 40;
        int size = cookieHeight - 40;
        int availableSpaceY = gameHeight - 40;
        int topOffset = 40 + (availableSpaceY - size) / 2;

        cookieWidget = new CookieWidget(gameX + cookiePanelWidth / 2 - size / 2, gameY + topOffset, size, size, this);
        addDrawableChild(cookieWidget);
        addDrawableChild(restartButton);
        addDrawableChild(new ShopWidget(gameX + gameWidth - upgradePanelWidth + 1, gameY + 30, upgradePanelWidth - 1, gameHeight - 30, this));

        playerName = MinecraftClient.getInstance().getSession().getUsername();
    }

    @Override
    public void tick() {
        ticker++;
        if (ticker == 20) {
            ticker = 0;
            cookies += CPS;
        }

        float spawnCPS = Math.min(MinigameRegistry.getConfig(CookieClickerVisualConfig.class).maxCookieRate, CPS);
        cookieSpawnCD += spawnCPS / 20.0f;

        while (cookieSpawnCD >= 1.0f) {
            cookieSpawnCD -= 1.0f;
            spawnCookie();
        }
    }

    private void load() {
        cookies = MinigameRegistry.getConfig(CookieClickerConfig.class).cookies;
        cookiesPerClick = MinigameRegistry.getConfig(CookieClickerConfig.class).cookiesPerClick;
        clickUpgradeCount = MinigameRegistry.getConfig(CookieClickerConfig.class).clickUpgradeCount;
        clickUpgradePrice = MinigameRegistry.getConfig(CookieClickerConfig.class).clickUpgradePrice;
        woodenCursorPrice = MinigameRegistry.getConfig(CookieClickerConfig.class).woodenCursorPrice;
        woodenCursorCount = MinigameRegistry.getConfig(CookieClickerConfig.class).woodenCursorCount;
        stoneCursorPrice = MinigameRegistry.getConfig(CookieClickerConfig.class).stoneCursorPrice;
        stoneCursorCount = MinigameRegistry.getConfig(CookieClickerConfig.class).stoneCursorCount;
        copperCursorPrice = MinigameRegistry.getConfig(CookieClickerConfig.class).copperCursorPrice;
        copperCursorCount = MinigameRegistry.getConfig(CookieClickerConfig.class).copperCursorCount;
        goldenCursorPrice = MinigameRegistry.getConfig(CookieClickerConfig.class).goldenCursorPrice;
        goldenCursorCount = MinigameRegistry.getConfig(CookieClickerConfig.class).goldenCursorCount;
        ironCursorPrice = MinigameRegistry.getConfig(CookieClickerConfig.class).ironCursorPrice;
        ironCursorCount = MinigameRegistry.getConfig(CookieClickerConfig.class).ironCursorCount;
        diamondCursorPrice = MinigameRegistry.getConfig(CookieClickerConfig.class).diamondCursorPrice;
        diamondCursorCount = MinigameRegistry.getConfig(CookieClickerConfig.class).diamondCursorCount;
        netheriteCursorPrice = MinigameRegistry.getConfig(CookieClickerConfig.class).netheriteCursorPrice;
        netheriteCursorCount = MinigameRegistry.getConfig(CookieClickerConfig.class).netheriteCursorCount;
        clickDoublerCount = MinigameRegistry.getConfig(CookieClickerConfig.class).clickDoublerCount;
        clickDoublerPrice = MinigameRegistry.getConfig(CookieClickerConfig.class).clickDoublerPrice;
        CPS = MinigameRegistry.getConfig(CookieClickerConfig.class).CPS;
    }

    private void spawnCookie() {
        int gameWidth = width - 80;
        int gameHeight = height - 40;
        int gameX = (width - gameWidth) / 2;
        int gameY = (height - gameHeight) / 2;
        Random random = new Random();

        int cookiePanelWidth = gameWidth / 3 * 2;

        int x = random.nextInt(gameX, gameX + cookiePanelWidth);
        int y = gameY - 10;

        cookiesWidgets.add(new Cookie(x, y));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        Matrix3x2fStack matrices = context.getMatrices();

        int gameWidth = width - 80;
        int gameHeight = height - 40;
        int gameX = (width - gameWidth) / 2;
        int gameY = (height - gameHeight) / 2;

        int cookiePanelWidth = gameWidth / 3 * 2;
        int upgradePanelWidth = gameWidth - cookiePanelWidth;

        context.drawCenteredTextWithShadow(tr, String.format(Text.translatable("minigame.cookieclicker.bakery").getString(), playerName), gameX + (cookiePanelWidth / 2), gameY + 20 + 1 - 2, -1);

        String cookiesString = cookiesToString((long) cookies);

        matrices.pushMatrix();
        matrices.translate(gameX + (cookiePanelWidth / 2), gameY + 40);
        matrices.scale(1.3f, 1.3f);
        context.drawCenteredTextWithShadow(tr, Text.translatable("minigame.cookieclicker.cookies").append(cookiesString), 0, 0, -1);
        matrices.popMatrix();
        if (CPS != 0) {
            matrices.pushMatrix();
            matrices.translate(gameX + (cookiePanelWidth / 2), gameY + 58);
            matrices.scale(0.7f, 0.7f);
            context.drawCenteredTextWithShadow(tr, Text.translatable("minigame.cookieclicker.cookiesPerSecond").append(cookiesToString(CPS)), 0, 0, -1);
            matrices.popMatrix();
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int gameWidth = width - 80;
        int gameHeight = height - 40;
        int gameX = (width - gameWidth) / 2;
        int gameY = (height - gameHeight) / 2;

        int cookiePanelWidth = gameWidth / 3 * 2;
        int upgradePanelWidth = gameWidth - cookiePanelWidth;

        context.drawHorizontalLine(gameX, gameX + gameWidth, gameY, -1);
        context.drawHorizontalLine(gameX, gameX + gameWidth, gameY + gameHeight, -1);
        context.drawVerticalLine(gameX, gameY, gameY + gameHeight, -1);
        context.drawVerticalLine(gameX + gameWidth, gameY, gameY + gameHeight, -1);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, gameX + 1, gameY + 1, 0, 0, gameWidth - 1, gameHeight - 1, 32, 32);
        context.drawVerticalLine(gameX + cookiePanelWidth, gameY, gameY + gameHeight, -1);

        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("minigame.cookieclicker.shop").styled(style -> style.withBold(true)), gameX + cookiePanelWidth + upgradePanelWidth / 2, gameY + 11, -1);

        context.enableScissor(gameX + 1, gameY + 1, gameX + cookiePanelWidth, gameY + gameHeight - 1);
        List<Cookie> toRemove = new ArrayList<>();
        for (Cookie cookie : cookiesWidgets) {
            if (cookie.y >= gameY + gameHeight + 20) {
                toRemove.add(cookie);
            } else {
                cookie.render(context);
            }
        }
        cookiesWidgets.removeAll(toRemove);
        context.disableScissor();

        context.fill(gameX + 1, gameY + 19 - 2, gameX + cookiePanelWidth, gameY + 31 - 2, 0x60000000);
        context.fill(gameX + 1, gameY + 37, gameX + cookiePanelWidth, gameY + 55, 0x60000000);
    }

    public static String cookiesToString(long value) {
        String[] suffixes = {
                "", " million", " billion", " trillion", " quadrillion", " quintillion",
                " sextillion", " septillion", " octillion", " nonillion", " decillion",
                " undecillion", " duodecillion", " tredecillion", " quattuordecillion",
                " quindecillion"
        };

        if (value < 1_000_000) {
            return String.format(Locale.US, "%,d", value);
        }

        double number = value / 1_000_000.0;
        int suffixIndex = 1;

        while (number >= 1000.0 && suffixIndex < suffixes.length - 1) {
            number /= 1000.0;
            suffixIndex++;
        }

        return String.format(Locale.US, "%.3f%s", number, suffixes[suffixIndex]);
    }

    public static String cookiesToString(double value) {
        String[] suffixes = {
                "", " million", " billion", " trillion", " quadrillion", " quintillion",
                " sextillion", " septillion", " octillion", " nonillion", " decillion",
                " undecillion", " duodecillion", " tredecillion", " quattuordecillion",
                " quindecillion"
        };

        if (value < 1_000_000.0) {
            return String.format(Locale.US, "%,.1f", value);
        }

        double number = value / 1_000_000.0;
        int suffixIndex = 1;

        while (number >= 1000.0 && suffixIndex < suffixes.length - 1) {
            number /= 1000.0;
            suffixIndex++;
        }

        return String.format(Locale.US, "%.1f%s", number, suffixes[suffixIndex]);
    }


    private void saveGame() {
        MinigameRegistry.getConfig(CookieClickerConfig.class).cookies = cookies;
        MinigameRegistry.getConfig(CookieClickerConfig.class).cookiesPerClick = cookiesPerClick;
        MinigameRegistry.getConfig(CookieClickerConfig.class).clickUpgradeCount = clickUpgradeCount;
        MinigameRegistry.getConfig(CookieClickerConfig.class).clickUpgradePrice = clickUpgradePrice;
        MinigameRegistry.getConfig(CookieClickerConfig.class).woodenCursorCount = woodenCursorCount;
        MinigameRegistry.getConfig(CookieClickerConfig.class).woodenCursorPrice = woodenCursorPrice;
        MinigameRegistry.getConfig(CookieClickerConfig.class).stoneCursorCount = stoneCursorCount;
        MinigameRegistry.getConfig(CookieClickerConfig.class).stoneCursorPrice = stoneCursorPrice;
        MinigameRegistry.getConfig(CookieClickerConfig.class).copperCursorCount = copperCursorCount;
        MinigameRegistry.getConfig(CookieClickerConfig.class).copperCursorPrice = copperCursorPrice;
        MinigameRegistry.getConfig(CookieClickerConfig.class).goldenCursorCount = goldenCursorCount;
        MinigameRegistry.getConfig(CookieClickerConfig.class).goldenCursorPrice = goldenCursorPrice;
        MinigameRegistry.getConfig(CookieClickerConfig.class).ironCursorCount = ironCursorCount;
        MinigameRegistry.getConfig(CookieClickerConfig.class).ironCursorPrice = ironCursorPrice;
        MinigameRegistry.getConfig(CookieClickerConfig.class).diamondCursorCount = diamondCursorCount;
        MinigameRegistry.getConfig(CookieClickerConfig.class).diamondCursorPrice = diamondCursorPrice;
        MinigameRegistry.getConfig(CookieClickerConfig.class).netheriteCursorCount = netheriteCursorCount;
        MinigameRegistry.getConfig(CookieClickerConfig.class).netheriteCursorPrice = netheriteCursorPrice;
        MinigameRegistry.getConfig(CookieClickerConfig.class).clickDoublerPrice = clickDoublerPrice;
        MinigameRegistry.getConfig(CookieClickerConfig.class).clickDoublerCount = clickDoublerCount;
        MinigameRegistry.getConfig(CookieClickerConfig.class).CPS = CPS;
        AutoConfig.getConfigHolder(CookieClickerConfig.class).save();
    }

    public static float vol() {
        return PlayingSoundManager.vol(MinigameRegistry.getConfig(CookieClickerVisualConfig.class).volume);
    }

    @Override
    public void close() {
        saveGame();
        MinecraftClient.getInstance().setScreen(parent);
    }

    public void clearGame() {
        AutoConfig.getConfigHolder(CookieClickerConfig.class).resetToDefault();
        load();
    }

    public class Cookie {
        private final int x;
        private int y;
        private final int rotateP;
        private final float scale;
        private int rotate;

        public Cookie(int x, int y) {
            Random random = new Random();

            this.x = x;
            this.y = y;
            this.rotate = 13;
            this.rotateP = random.nextInt(1, 6);
            this.scale = random.nextFloat(0.9f, 1.2f);
        }

        public void render(DrawContext ctx) {
            Matrix3x2fStack matrices = ctx.getMatrices();
            rotate += switch (rotateP) {
                case 1 -> -2;
                case 2 -> -1;
                case 3 -> 0;
                case 4 -> 1;
                case 5 -> 2;
                default -> 10;
            };
            y++;

            matrices.pushMatrix();
            matrices.translate(x, y);
            matrices.scale(scale, scale);
            matrices.rotate((float) Math.toRadians(rotate));
            ctx.drawTexture(RenderPipelines.GUI_TEXTURED,
                    Identifier.ofVanilla("textures/item/cookie.png"),
                    -8, -8, 0, 0, 16, 16, 16, 16
            );
            matrices.popMatrix();
        }
    }
}
