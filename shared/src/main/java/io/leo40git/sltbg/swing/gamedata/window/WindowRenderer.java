/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.gamedata.window;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class WindowRenderer {
    public static final int PROMPT_FRAME_COUNT = WindowPrompt.FRAME_COUNT;
    public static final int COLOR_COUNT = 32;

    private static final int SRC_IMAGE_SIZE = 128;
    private static final int SRC_COLOR_START_X = 64;
    private static final int SRC_COLOR_START_Y = 96;
    private static final int SRC_COLOR_SIZE = 8;

    private @NotNull WindowVersion version;
    private @NotNull BufferedImage skinImage;

    private final WindowBackground background;
    private final WindowFrame frame;
    private final WindowPrompt prompt;
    private final Color[] colors;

    public WindowRenderer(@NotNull WindowVersion version, @NotNull BufferedImage skinImage, @NotNull WindowTone tone, float opacity) {
        checkOpacity(opacity);
        checkSkin(version, skinImage);

        this.version = version;
        this.skinImage = skinImage;

        background = new WindowBackground(version, tone, opacity, skinImage);
        frame = new WindowFrame(version, skinImage);
        prompt = new WindowPrompt(version, skinImage);
        colors = new Color[COLOR_COUNT];
        initColors();
    }

    private static void checkOpacity(float opacity) {
        if (opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException("opacity is out of bounds: must be between 0 and 1 (inclusive), but was %g"
                    .formatted(opacity));
        }
    }

    private static void checkSkin(@NotNull WindowVersion version, @NotNull BufferedImage skinImage) {
        int imageSize = version.scale(SRC_IMAGE_SIZE);
        if (skinImage.getWidth() != imageSize || skinImage.getHeight() != imageSize) {
            throw new IllegalArgumentException("skinImage has incorrect dimensions: expected %d x %1$d, got %d x %d"
                    .formatted(imageSize, skinImage.getWidth(), skinImage.getHeight()));
        }
    }

    public @NotNull WindowVersion getVersion() {
        return version;
    }

    public @NotNull BufferedImage getSkinImage() {
        return skinImage;
    }

    public @NotNull WindowTone getTone() {
        return background.getTone();
    }

    public float getOpacity() {
        return background.getOpacity();
    }

    public void setSkin(@NotNull WindowVersion version, @NotNull BufferedImage skinImage) {
        checkSkin(version, skinImage);

        this.version = version;
        this.skinImage = skinImage;

        background.setSkin(version, skinImage);
        frame.setSkin(version, skinImage);
        prompt.setSkin(version, skinImage);
        initColors();
    }

    public void setTone(@NotNull WindowTone tone) {
        background.setTone(tone);
    }

    public void setOpacity(float opacity) {
        checkOpacity(opacity);
        background.setOpacity(opacity);
    }

    public int getMargin() {
        return version.getMargin();
    }

    public void paintBackground(@NotNull Graphics g, int x, int y, int width, int height, @Nullable ImageObserver observer) {
        background.paint(g, x, y, width, height, observer);
    }

    public void paintBackgroundWithMargin(@NotNull Graphics g, int x, int y, int width, int height, @Nullable ImageObserver observer) {
        int margin = version.getMargin();
        background.paint(g, x + margin, y + margin, width - margin * 2, height - margin * 2, observer);
    }

    public void paintFrame(@NotNull Graphics g, int x, int y, int width, int height, @Nullable ImageObserver observer) {
        frame.paint(g, x, y, width, height, observer);
    }

    public int getPadding() {
        return version.getPadding();
    }

    public void paintPrompt(@NotNull Graphics g, @Range(from = 0, to = PROMPT_FRAME_COUNT - 1) int frame,
                            int x, int y, @Nullable ImageObserver observer) {
        prompt.paintFrame(g, frame, x, y, observer);
    }

    public int getPromptSize() {
        return prompt.getFrameSize();
    }

    public void paintPromptInFrame(@NotNull Graphics g, @Range(from = 0, to = PROMPT_FRAME_COUNT - 1) int frame,
                                   int x, int y, int width, int height, @Nullable ImageObserver observer) {
        int size = prompt.getFrameSize();
        x += width / 2 - size / 2;
        y += height - size;
        prompt.paintFrame(g, frame, x, y, observer);
    }

    private void initColors() {
        // this is simple: there are 32 colored squares on the Window sheet,
        //  these directly map to the available 32 preset colors

        final int size = version.scale(SRC_COLOR_SIZE);
        final int startX = version.scale(SRC_COLOR_START_X);
        int y = version.scale(SRC_COLOR_START_Y);

        int x = startX;
        for (int i = 0; i < COLOR_COUNT; i++) {
            colors[i] = new Color(skinImage.getRGB(x, y), false);
            x += size;
            if (x >= skinImage.getWidth()) {
                x = startX;
                y += size;
            }
        }
    }

    public @NotNull Color getColor(@Range(from = 0, to = COLOR_COUNT - 1) int index) {
        return colors[index];
    }
}
