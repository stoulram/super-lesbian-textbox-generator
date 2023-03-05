/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.swing.util;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;

import org.jetbrains.annotations.NotNull;

public final class ImageUtils {
    private ImageUtils() {
        throw new UnsupportedOperationException("ImageUtils only contains static declarations.");
    }

    public static @NotNull BufferedImage scaleImage(@NotNull BufferedImage image, int newWidth, int newHeight) {
        var scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        var g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setBackground(ColorUtils.TRANSPARENT);
        g.clearRect(0, 0, newWidth, newHeight);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaled;
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class CanEncodeImageAndSuffixFilter implements ServiceRegistry.Filter {
        private final @NotNull ImageTypeSpecifier type;
        private final @NotNull String fileSuffix;

        public CanEncodeImageAndSuffixFilter(@NotNull ImageTypeSpecifier type, @NotNull String fileSuffix) {
            this.type = type;
            this.fileSuffix = fileSuffix;
        }

        @Override
        public boolean filter(Object provider) {
            var iws = (ImageWriterSpi) provider;
            if (!iws.canEncodeImage(type)) {
                return false;
            }
            var fileSuffixes = iws.getFileSuffixes();
            if (fileSuffixes != null) {
                for (String candidate : fileSuffixes) {
                    if (fileSuffix.equalsIgnoreCase(candidate)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static void writeImage(@NotNull RenderedImage image, @NotNull Path path) throws IOException {
        String fileName = path.getFileName().toString();

        String fileSuffix = "";
        int dotIdx = fileName.lastIndexOf('.');
        if (dotIdx >= 0) {
            fileSuffix = fileName.substring(dotIdx + 1);
        }

        if (fileSuffix.isEmpty()) {
            throw new IOException("Can't write image to \"" + path + "\": no file extension");
        }

        var it = IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class,
                new CanEncodeImageAndSuffixFilter(ImageTypeSpecifier.createFromRenderedImage(image), fileSuffix),
                true);
        if (!it.hasNext()) {
            throw new IOException("Can't write image to \"" + path + "\": couldn't find ImageWriter for file extension \"." + fileSuffix + "\"\n"
                    + "(or image type cannot be encoded with this extension)");
        }

        var writer = it.next().createWriterInstance();
        try (var os = Files.newOutputStream(path);
             var out = ImageIO.createImageOutputStream(os)) {
            writer.setOutput(out);
            writer.write(image);
        } finally {
            writer.dispose();
        }
    }
}
