package org.korosoft.javaocr.core;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class fot basic {@link MutableImage} manipulations.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public final class ImgUtil {
    /**
     * Loads mutable image from stream supported by {@link ImageIO#read(javax.imageio.stream.ImageInputStream)} method.
     *
     * @param stream Input stream.
     * @return read {@link MutableImage}
     * @throws IOException When UO exception occurs
     */
    public static MutableImage readMutableImageFromSupportedStream(InputStream stream) throws IOException {
        final BufferedImage bufferedImage = ImageIO.read(stream);
        final int w = bufferedImage.getWidth();
        final int h = bufferedImage.getHeight();
        final byte[] pixels = new byte[w * h];
        int p = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = bufferedImage.getRGB(x, y);
                int c1 = rgb & 0xff;
                int c2 = (rgb >> 8) & 0xff;
                int c3 = (rgb >> 16) & 0xff;
                int grayscale = (c1 + c2 + c3) / 3;
                if (grayscale > 255) {
                    grayscale = 255;
                }
                pixels[p++] = (byte) grayscale;
            }
        }
        return new MutableImage(pixels, 0, 0, w, h);
    }

    /**
     * Converts image to printable text. Handy for debugging.
     *
     * @param image Image to convert.
     * @return ASCII image representation.
     */
    public static String imageToText(MutableImage image) {
        StringBuilder builder = new StringBuilder((image.width + 1) * image.height);
        char pseudo[] = "█▓▒░▪◦·".toCharArray();
        int p = image.firstPixel;
        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                int i = (int) image.pixels[p] & 255 / (255 / pseudo.length);
                if (i < 0) {
                    i = 0;
                }
                if (i >= pseudo.length) {
                    i = pseudo.length - 1;
                }
                builder.append(pseudo[i]);
                p++;
            }
            builder.append("\n");
            p += image.lineSpan;
        }
        return builder.toString();
    }

    /**
     * Private constructor prevents instantiation
     */
    private ImgUtil() {
    }
}
