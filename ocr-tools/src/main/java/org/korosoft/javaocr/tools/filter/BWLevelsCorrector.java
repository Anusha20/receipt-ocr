package org.korosoft.javaocr.tools.filter;

import org.korosoft.javaocr.core.MutableImage;

/**
 * Filter to correct level based on idea that at the center of the image should be high-contrast image to
 * be improved. It is suitable for correcting photo images like receipts.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class BWLevelsCorrector extends AbstractLevelsCorrector {
    /**
     * White color threshold between most frequent color and 'black' which is considered to be darkest color with non-zero
     * frequency. When the darkest color is 10, most frequent is 120 and threshold is 0.7, effective white color threshold
     * will be 10 + (120 - 10) * 0.7 = 87
     */
    private double whiteThreshold = 0.5;

    private static final byte WHITE = -1;
    private static final byte BLACK = 0;

    @Override
    protected void correctLevels(final MutableImage source, final int whiteColor, final int blackColor) {
        final byte[] pixels = source.pixels;
        final int w = source.width;
        final int h = source.height;
        int p;// calculate real white threshold
        int thresholdLevel = (int) (blackColor + (whiteColor - blackColor) * whiteThreshold);

        // update levels
        p = source.firstPixel;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixels[p] = ((int) pixels[p] & 255) < thresholdLevel ? BLACK : WHITE;
                p++;
            }
            p += source.lineSpan;
        }
    }


    // Getters and setters

    /**
     * Returns white color threshold between most frequent color and 'black' which is considered to be darkest color with non-zero
     * frequency. When the darkest color is 10, most frequent is 120 and threshold is 0.7, effective white color threshold
     * will be 10 + (120 - 10) * 0.7 = 87
     *
     * @return White color threshold.
     */
    public double getWhiteThreshold() {
        return whiteThreshold;
    }

    /**
     * Sets white color threshold between most frequent color and 'black' which is considered to be darkest color with non-zero
     * frequency. When the darkest color is 10, most frequent is 120 and threshold is 0.7, effective white color threshold
     * will be 10 + (120 - 10) * 0.7 = 87
     *
     * @param whiteThreshold White color threshold.
     */
    public void setWhiteThreshold(double whiteThreshold) {
        this.whiteThreshold = whiteThreshold;
    }
}