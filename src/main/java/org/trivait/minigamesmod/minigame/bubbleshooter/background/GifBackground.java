package org.trivait.minigamesmod.minigame.bubbleshooter.background;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GifBackground extends Background {
    private static final Identifier MOD_ID = Identifier.of("minigamesmod", "bubbleshooter_gif");
    private final List<Frame> frames = new ArrayList<>();
    private float totalTime = 0;
    private float totalDuration = 0;
    private boolean loaded = false;
    private int gifWidth = 1;
    private int gifHeight = 1;

    public GifBackground() {
        super(Text.translatable("minigame.bubbleshooter.background.gif"));
    }

    private void loadGif() {
        loaded = true;
        File gifFile = new File(MinecraftClient.getInstance().runDirectory, "config/minigames/bubbleshooter.gif");
        if (!gifFile.exists()) {
            return;
        }

        try (ImageInputStream stream = ImageIO.createImageInputStream(gifFile)) {
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            reader.setInput(stream);

            int count = reader.getNumImages(true);

            IIOMetadata streamMetadata = reader.getStreamMetadata();
            if (streamMetadata != null) {
                String globalFormat = streamMetadata.getNativeMetadataFormatName();
                if (globalFormat != null) {
                    IIOMetadataNode globalRoot = (IIOMetadataNode) streamMetadata.getAsTree(globalFormat);
                    var screenDesc = globalRoot.getElementsByTagName("LogicalScreenDescriptor");
                    if (screenDesc.getLength() > 0) {
                        IIOMetadataNode node = (IIOMetadataNode) screenDesc.item(0);
                        this.gifWidth = Integer.parseInt(node.getAttribute("logicalScreenWidth"));
                        this.gifHeight = Integer.parseInt(node.getAttribute("logicalScreenHeight"));
                    }
                }
            }

            if (this.gifWidth <= 1) this.gifWidth = reader.getWidth(0);
            if (this.gifHeight <= 1) this.gifHeight = reader.getHeight(0);

            BufferedImage master = new BufferedImage(gifWidth, gifHeight, BufferedImage.TYPE_INT_ARGB);
            BufferedImage previous = null;
            Graphics2D g = master.createGraphics();
            g.setBackground(new Color(0, 0, 0, 0));

            for (int i = 0; i < count; i++) {
                BufferedImage frameImage = reader.read(i);
                IIOMetadata metadata = reader.getImageMetadata(i);

                int delay = 10;
                int offsetX = 0;
                int offsetY = 0;
                String disposalMethod = "doNotDispose";

                String metaFormat = metadata.getNativeMetadataFormatName();
                if (metaFormat != null) {
                    IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);

                    var gce = root.getElementsByTagName("GraphicControlExtension");
                    if (gce.getLength() > 0) {
                        IIOMetadataNode node = (IIOMetadataNode) gce.item(0);
                        delay = Integer.parseInt(node.getAttribute("delayTime"));
                        disposalMethod = node.getAttribute("disposalMethod");
                    }

                    var id = root.getElementsByTagName("ImageDescriptor");
                    if (id.getLength() > 0) {
                        IIOMetadataNode node = (IIOMetadataNode) id.item(0);
                        offsetX = Integer.parseInt(node.getAttribute("imageLeftPosition"));
                        offsetY = Integer.parseInt(node.getAttribute("imageTopPosition"));
                    }
                }

                if (i > 0) {
                    String prevDisposal = frames.get(i - 1).disposalMethod;
                    if ("restoreToBackgroundColor".equals(prevDisposal)) {
                        Frame prevFrame = frames.get(i - 1);
                        g.setComposite(AlphaComposite.Clear);
                        g.fillRect(prevFrame.offX, prevFrame.offY, prevFrame.w, prevFrame.h);
                        g.setComposite(AlphaComposite.SrcOver);
                    } else if ("restoreToPrevious".equals(prevDisposal) && previous != null) {
                        g.setComposite(AlphaComposite.Src);
                        g.drawImage(previous, 0, 0, null);
                        g.setComposite(AlphaComposite.SrcOver);
                    }
                }

                if ("restoreToPrevious".equals(disposalMethod)) {
                    previous = new BufferedImage(gifWidth, gifHeight, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D pg = previous.createGraphics();
                    pg.drawImage(master, 0, 0, null);
                    pg.dispose();
                }

                g.drawImage(frameImage, offsetX, offsetY, null);

                NativeImage nativeImage = new NativeImage(gifWidth, gifHeight, false);
                for (int y = 0; y < gifHeight; y++) {
                    for (int x = 0; x < gifWidth; x++) {
                        int argb = master.getRGB(x, y);
                        int a = (argb >> 24) & 0xFF;
                        int r = (argb >> 16) & 0xFF;
                        int gChannel = (argb >> 8) & 0xFF;
                        int b = argb & 0xFF;

                        if (a == 0) {
                            argb = 0xFFFFFFFF;
                        }

                        nativeImage.setColorArgb(x, y, argb);
                    }
                }

                Identifier frameId = Identifier.of(MOD_ID.getNamespace(), MOD_ID.getPath() + "_frame_" + i);
                NativeImageBackedTexture texture = new NativeImageBackedTexture(frameId::toString, nativeImage);
                MinecraftClient.getInstance().getTextureManager().registerTexture(frameId, texture);

                float durationSeconds = delay / 100.0f;
                if (durationSeconds <= 0) {
                    durationSeconds = 0.1f;
                }

                frames.add(new Frame(frameId, durationSeconds, disposalMethod, offsetX, offsetY, frameImage.getWidth(), frameImage.getHeight()));
                totalDuration += durationSeconds;
            }
            g.dispose();
            reader.dispose();
        } catch (IOException ignored) {}
    }

    @Override
    public void render(int x, int y, int width, int height, DrawContext ctx, float delta, int mouseX, int mouseY) {
        if (!loaded) {
            loadGif();
        }

        if (frames.isEmpty()) {
            ctx.fill(x, y, x + width, y + height, 0xFFFFFFFF);
            return;
        }

        totalTime += (delta / 20.0f);
        if (totalTime >= totalDuration) {
            totalTime %= totalDuration;
        }

        Identifier currentFrameId = frames.get(0).id;
        float elapsed = 0;
        for (Frame frame : frames) {
            elapsed += frame.duration;
            if (totalTime <= elapsed) {
                currentFrameId = frame.id;
                break;
            }
        }

        ctx.drawTexture(RenderLayer::getGuiTextured, currentFrameId, x, y, 0, 0, width, height, width, height);
    }

    private record Frame(Identifier id, float duration, String disposalMethod, int offX, int offY, int w, int h) {}
}