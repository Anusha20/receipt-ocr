package org.korosoft.javaocr.tools.filter;

import org.korosoft.javaocr.core.MutableImage;
import org.korosoft.javaocr.core.api.ImageFilter;

/**
 * Abstract filter to correct levels of the {@link MutableImage}.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public abstract class AbstractLevelsCorrector implements ImageFilter {
    /**
     * Dimension of a rectangle within source image (measured as a fraction if full image dimension) used to calculate
     * histogram auto-levels. If image is 200x100 and {@code centralSquareDimension} equals 0.3, then a portion of size
     * 60x30 in the center of the image will be used to calculate levels.
     */
    private double centralSquareDimension = 0.3;

    /**
     * {@inheritDoc}
     */
    public MutableImage doFilter(MutableImage source) {
        final byte[] pixels = source.pixels;
        final int w = source.width;
        final int h = source.height;

        int xMin = (int) (w * (1 - centralSquareDimension) / 2);
        int yMin = (int) (h * (1 - centralSquareDimension) / 2);
        int xMax = w - xMin;
        int yMax = h - yMin;

        int levels[] = new int[256];

        // Calculate levels
        int p = source.firstPixel + source.fullLine * yMin + xMin;
        for (int y = yMin; y < yMax; y++) {
            for (int x = xMin; x < xMax; x++) {
                levels[(int) pixels[p] & 255]++;
                p++;
            }
            p += source.fullLine - xMax + xMin;
        }

        int whiteLevelIdx = 0;
        int blackLevelIdx = -1;

        // Find 'white' level and 'black' level.
        // White is the most frequent, Black is the darkest
        for (int i = 0; i < levels.length; i++) {
            if (levels[whiteLevelIdx] < levels[i]) {
                whiteLevelIdx = i;
            }
            if (blackLevelIdx == -1 && levels[i] > 0) {
                blackLevelIdx = i;
            }

        }
        correctLevels(source, whiteLevelIdx, blackLevelIdx);


        return source;
    }

    /**
     * Implement this method to correct image levels
     *
     * @param source     Source image.
     * @param whiteColor Detected light (most frequent) color value.
     * @param blackColor Detected dark (darkest on the image) color value.
     */
    protected abstract void correctLevels(final MutableImage source, final int whiteColor, final int blackColor);

    // Getters and setters

    /**
     * Returns central square dimension. Dimension of a rectangle within source image (measured as a fraction if full image dimension) used to calculate
     * histogram auto-levels. If image is 200x100 and {@code centralSquareDimension} equals 0.3, then a portion of size
     * 60x30 in the center of the image will be used to calculate levels.
     *
     * @return Central square dimension.
     */
    public double getCentralSquareDimension() {
        return centralSquareDimension;
    }

    /**
     * Sets Dimension of a rectangle within source image (measured as a fraction if full image dimension) used to calculate
     * histogram auto-levels. If image is 200x100 and {@code centralSquareDimension} equals 0.3, then a portion of size
     * 60x30 in the center of the image will be used to calculate levels.
     *
     * @param centralSquareDimension Central square dimension.
     */
    public void setCentralSquareDimension(double centralSquareDimension) {
        this.centralSquareDimension = centralSquareDimension;
    }
}