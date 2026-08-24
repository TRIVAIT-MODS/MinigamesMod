package org.trivait.minigamesmod.minigame.bubbleshooter.background;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GifBackground extends Background {

    private static final Identifier TEXTURE_PREFIX =
            Identifier.fromNamespaceAndPath("minigamesmod", "bubbleshooter_gif");

    private final List<Frame> frames = new ArrayList<>();

    private float totalTime = 0.0f;
    private float totalDuration = 0.0f;

    private boolean loaded = false;
    private long lastModified = -1L;
    private int loadId = 0;

    private int gifWidth = 1;
    private int gifHeight = 1;

    public GifBackground() {
        super(Component.translatable("minigame.bubbleshooter.background.gif"));
    }

    public void loadGif() {
        File gifFile = getGifFile();

        if (!gifFile.exists()) {
            clearFrames();
            loaded = true;
            lastModified = -1L;
            return;
        }

        long modified = gifFile.lastModified();

        if (loaded && modified == lastModified) {
            return;
        }

        reloadGif(gifFile, modified);
    }

    private File getGifFile() {
        return new File(Minecraft.getInstance().gameDirectory, "config/minigames/bubbleshooter.gif");
    }

    private void reloadGif(File gifFile, long modified) {
        clearFrames();

        totalTime = 0.0f;
        totalDuration = 0.0f;
        gifWidth = 1;
        gifHeight = 1;
        loaded = false;

        loadId++;

        List<Frame> newFrames = new ArrayList<>();

        try (ImageInputStream stream = ImageIO.createImageInputStream(gifFile)) {

            if (stream == null) {
                loaded = true;
                lastModified = modified;
                return;
            }

            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();

            try {
                reader.setInput(stream);

                int count = reader.getNumImages(true);

                if (count <= 0) {
                    loaded = true;
                    lastModified = modified;
                    return;
                }

                readLogicalScreenSize(reader);

                BufferedImage master = new BufferedImage(gifWidth, gifHeight, BufferedImage.TYPE_INT_ARGB);

                Graphics2D graphics = master.createGraphics();

                try {
                    graphics.setComposite(AlphaComposite.Src);
                    graphics.clearRect(0, 0, gifWidth, gifHeight);

                    BufferedImage previous = null;

                    for (int i = 0; i < count; i++) {
                        BufferedImage frameImage = reader.read(i);
                        IIOMetadata metadata = reader.getImageMetadata(i);

                        FrameInfo info = readFrameInfo(metadata, frameImage);

                        if (i > 0 && !newFrames.isEmpty()) {
                            Frame previousFrame = newFrames.get(newFrames.size() - 1);

                            if ("restoreToBackgroundColor".equals(previousFrame.disposalMethod)) {
                                graphics.setComposite(AlphaComposite.Clear);
                                graphics.fillRect(previousFrame.offX, previousFrame.offY, previousFrame.width, previousFrame.height);
                                graphics.setComposite(AlphaComposite.SrcOver);
                            } else if ("restoreToPrevious".equals(previousFrame.disposalMethod) && previous != null) {
                                graphics.setComposite(AlphaComposite.Src);
                                graphics.drawImage(previous, 0, 0, null);
                                graphics.setComposite(AlphaComposite.SrcOver);
                            }
                        }

                        if ("restoreToPrevious".equals(info.disposalMethod)) {
                            previous = copyImage(master);
                        }

                        graphics.setComposite(AlphaComposite.SrcOver);
                        graphics.drawImage(frameImage, info.offsetX, info.offsetY, null);

                        NativeImage nativeImage = convertToNativeImage(master);

                        Identifier frameId = Identifier.fromNamespaceAndPath(TEXTURE_PREFIX.getNamespace(), TEXTURE_PREFIX.getPath() + "_" + loadId + "_frame_" + i);

                        DynamicTexture texture = new DynamicTexture(frameId::toString, nativeImage);

                        Minecraft.getInstance().getTextureManager().register(frameId, texture);

                        float durationSeconds =
                                info.delayCentiseconds / 100.0f;

                        if (durationSeconds <= 0.0f) {
                            durationSeconds = 0.1f;
                        }

                        Frame frame = new Frame(frameId, texture, durationSeconds, info.disposalMethod, info.offsetX, info.offsetY, info.width, info.height);

                        newFrames.add(frame);
                        totalDuration += durationSeconds;
                    }
                } finally {
                    graphics.dispose();
                }
            } finally {
                reader.dispose();
            }

            frames.addAll(newFrames);

            loaded = true;
            lastModified = modified;

            if (frames.isEmpty()) {
                totalTime = 0.0f;
                totalDuration = 0.0f;
            }

        } catch (Exception ignored) {
            clearFrames();

            loaded = true;
            lastModified = modified;
            totalTime = 0.0f;
            totalDuration = 0.0f;
        }
    }

    private void readLogicalScreenSize(ImageReader reader) throws IOException {
        IIOMetadata streamMetadata = reader.getStreamMetadata();

        if (streamMetadata != null) {
            String format = streamMetadata.getNativeMetadataFormatName();

            if (format != null) {
                IIOMetadataNode root = (IIOMetadataNode) streamMetadata.getAsTree(format);

                var screenDesc = root.getElementsByTagName("LogicalScreenDescriptor");

                if (screenDesc.getLength() > 0) {
                    IIOMetadataNode node = (IIOMetadataNode) screenDesc.item(0);

                    String width = node.getAttribute("logicalScreenWidth");

                    String height = node.getAttribute("logicalScreenHeight");

                    if (!width.isEmpty()) {
                        gifWidth = Integer.parseInt(width);
                    }

                    if (!height.isEmpty()) {
                        gifHeight = Integer.parseInt(height);
                    }
                }
            }
        }

        if (gifWidth <= 1) {
            gifWidth = reader.getWidth(0);
        }

        if (gifHeight <= 1) {
            gifHeight = reader.getHeight(0);
        }
    }

    private FrameInfo readFrameInfo(IIOMetadata metadata, BufferedImage frameImage) {
        int delay = 10;
        int offsetX = 0;
        int offsetY = 0;

        String disposalMethod = "doNotDispose";

        String format = metadata.getNativeMetadataFormatName();

        if (format != null) {
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(format);

            var gce = root.getElementsByTagName("GraphicControlExtension");

            if (gce.getLength() > 0) {
                IIOMetadataNode node = (IIOMetadataNode) gce.item(0);

                String delayValue = node.getAttribute("delayTime");

                if (!delayValue.isEmpty()) {
                    delay = Integer.parseInt(delayValue);
                }

                String disposal = node.getAttribute("disposalMethod");

                if (!disposal.isEmpty()) {
                    disposalMethod = disposal;
                }
            }

            var imageDescriptor = root.getElementsByTagName("ImageDescriptor");

            if (imageDescriptor.getLength() > 0) {
                IIOMetadataNode node = (IIOMetadataNode) imageDescriptor.item(0);

                String x = node.getAttribute("imageLeftPosition");

                String y = node.getAttribute("imageTopPosition");

                if (!x.isEmpty()) {
                    offsetX = Integer.parseInt(x);
                }

                if (!y.isEmpty()) {
                    offsetY = Integer.parseInt(y);
                }
            }
        }

        return new FrameInfo(delay, offsetX, offsetY, disposalMethod, frameImage.getWidth(), frameImage.getHeight());
    }

    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = copy.createGraphics();

        try {
            graphics.setComposite(AlphaComposite.Src);
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        return copy;
    }

    private NativeImage convertToNativeImage(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if (a == 0) {
                    r = 255;
                    g = 255;
                    b = 255;
                    a = 255;
                }

                int abgr = (a << 24) | (b << 16) | (g << 8) | r;

                nativeImage.setPixel(x, y, argb);
            }
        }

        return nativeImage;
    }

    private void clearFrames() {
        Minecraft client = Minecraft.getInstance();

        for (Frame frame : frames) {
            try {
                client.getTextureManager().release(frame.id);
            } catch (Exception ignored) {
            }

            try {
                frame.texture.close();
            } catch (Exception ignored) {
            }
        }

        frames.clear();

        totalTime = 0.0f;
        totalDuration = 0.0f;
    }

    @Override
    public void render(int x, int y, int width, int height, GuiGraphicsExtractor ctx, float delta, int mouseX, int mouseY) {
        File gifFile = getGifFile();

        if (!gifFile.exists()) {
            if (!loaded || lastModified != -1L) {
                clearFrames();
                loaded = true;
                lastModified = -1L;
            }
        } else {
            long modified = gifFile.lastModified();

            if (!loaded || modified != lastModified) {
                loadGif();
            }
        }

        if (frames.isEmpty() || totalDuration <= 0.0f) {
            ctx.fill(x, y, x + width, y + height, 0xFFFFFFFF);
            return;
        }

        totalTime += delta / 20.0f;

        if (totalTime >= totalDuration) {
            totalTime %= totalDuration;
        }

        if (totalTime < 0.0f) {
            totalTime = 0.0f;
        }

        Identifier currentFrameId = frames.get(0).id;

        float elapsed = 0.0f;

        for (Frame frame : frames) {
            elapsed += frame.duration;

            if (totalTime < elapsed) {
                currentFrameId = frame.id;
                break;
            }
        }

        ctx.blit(RenderPipelines.GUI_TEXTURED, currentFrameId, x, y, 0.0f, 0.0f, width, height, width, height);
    }

    public void close() {
        clearFrames();

        loaded = false;
        lastModified = -1L;
        totalTime = 0.0f;
        totalDuration = 0.0f;
    }

    private record Frame(Identifier id, DynamicTexture texture, float duration, String disposalMethod, int offX, int offY, int width, int height) { }

    private record FrameInfo(int delayCentiseconds, int offsetX, int offsetY, String disposalMethod, int width, int height) { }
}