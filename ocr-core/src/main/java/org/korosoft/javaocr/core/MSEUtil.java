package org.korosoft.javaocr.core;

/**
 * Mean square error comparator for {@link MutableImage} instances.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public final class MSEUtil {
    /**
     * Calculates mean square difference between two symbol images. This algorithm uses larger image as the base for
     * comparison, so it works slower but has better statistics.
     * <p/>
     * Images are scaled to have equal width (smaller image is virtually enlarged) and aligned so base lines match.
     * <p/>
     * Non-overlapping parts of images are considered to be of specified default color.
     *
     * @param i1           One of two images to compare.
     * @param i2           One of two images to compare.
     * @param baseLine1    Base line for the first image
     * @param baseLine2    Base line for the second image
     * @param defaultColor Default color for non-overlapping image parts
     * @return MSEResult object containing sum of square error and number of pixels used. Means square error would be
     *         Math.sqrt(result.squareError / result.numPixels). result.squareError may be close to Long.MAX_VALUE, so
     *         do not add these values together in long format. Just switch to floating point evaluation.
     */
    public static MSEResult compareSlow(MutableImage i1, MutableImage i2, int baseLine1, int baseLine2, int defaultColor) {
        long squareError = 0;
        int numPixels = 0;

        if (i2.width > i1.width) {
            compareSlow(i2, i1, baseLine2, baseLine1, defaultColor);
        }
        // i1 is wider of of equal width to i2
        final int virtHeight2 = i2.height * i1.width / i2.width;
        final int virtBaseLine2 = baseLine2 * i1.width / i2.width;
        final int bottom2 = baseLine1 + virtHeight2 - virtBaseLine2;

        // Handle top not overlapped portion of images.

        int p1 = i1.firstPixel;
        if (virtBaseLine2 < baseLine1) { // i1 exceeds i2 at the top
            for (int y = baseLine1 - virtBaseLine2; y > 0; y--) {
                for (int x1 = 0; x1 < i1.width; x1++) {
                    final int v1 = (int) i1.pixels[p1] & 0xff;
                    squareError += (defaultColor - v1) * (defaultColor - v1);
                    numPixels++;
                    p1++;
                }
                p1 += i1.lineSpan;
            }
        } else {
            // i2 exceeds i1 at the top
            final int nonOverlapHeight = virtBaseLine2 - baseLine1;
            for (int y1 = 0; y1 < nonOverlapHeight; y1++) {
                final int y2 = y1 * i2.width / i1.width;
                for (int x1 = 0; x1 < i1.width; x1++) {
                    final int x2 = x1 * i2.width / i1.width;
                    final int p2 = i2.firstPixel + i2.fullLine * y2 + x2;
                    final int v2 = (int) i2.pixels[p2] & 0xff;
                    squareError += (defaultColor - v2) * (defaultColor - v2);
                    numPixels++;
                }
            }
        }

        // At this point p1 shows the starting overlapping pixel (if any)
        // Compare overlapping parts of the images
        int y1 = (p1 - i1.firstPixel) / i1.fullLine;
        final int p1bottom = i1.firstPixel + (bottom2 > i1.height ? i1.height : bottom2) * i1.fullLine;
        while (p1 < p1bottom) {
            for (int x1 = 0; x1 < i1.width; x1++) {
                final int x2 = x1 * i2.width / i1.width;
                final int y2 = (y1 - baseLine1 + virtBaseLine2) * i2.width / i1.width;
                final int p2 = i2.firstPixel + i2.fullLine * y2 + x2;
                final int v1 = (int) i1.pixels[p1] & 0xff;
                final int v2 = (int) i2.pixels[p2] & 0xff;
                squareError += (v2 - v1) * (v2 - v1);
                numPixels++;
                p1++;
            }
            p1 += i1.lineSpan;
            y1++;
        }

        // Handle bottom not overlapped portion of images.
        if (i1.height - baseLine1 > virtHeight2 - virtBaseLine2) { // There is exceeding portion of i1
            final int p1end = i1.firstPixel + i1.height * i1.fullLine - i1.lineSpan;
            while (p1 < p1end) {
                for (int x1 = i1.width; x1 > 0; x1--) {
                    final int v1 = (int) i1.pixels[p1] & 0xff;
                    squareError += (defaultColor - v1) * (defaultColor - v1);
                    numPixels++;
                    p1++;
                }
                p1 += i1.lineSpan;
            }
        } else { // There is exceeding portion of i1
            final int p2end = i2.firstPixel + i2.height * i2.fullLine - i2.lineSpan;
            for (int vy2 = virtBaseLine2 + i1.height - baseLine1; vy2 < virtHeight2; vy2++) {
                for (int x1 = 0; x1 < i1.width; x1++) {
                    final int x2 = x1 * i2.width / i1.width;
                    final int y2 = vy2 * i2.width / i1.width;
                    int p2 = i2.firstPixel + i2.fullLine * y2 + x2;
                    if (p2 < p2end) {
                        p2 -= i2.fullLine;
                    }
                    final int v2 = (int) i2.pixels[p2] & 0xff;
                    squareError += (defaultColor - v2) * (defaultColor - v2);
                    numPixels++;
                }
            }
        }
        return new MSEResult(squareError, numPixels);
    }

    /**
     * Calculates mean square difference between two images. This algorithm uses larger image as the base for
     * comparison, so it works slower but has better statistics.
     *
     * @param i1 One of two images to compare.
     * @param i2 One of two images to compare.
     * @return MSEResult object containing sum of square error and number of pixels used. Means square error would be
     *         Math.sqrt(result.squareError / result.numPixels). result.squareError may be close to Long.MAX_VALUE, so
     *         do not add these values together in long format. Just switch to floating point evaluation.
     */
    public static MSEResult compareSlow(MutableImage i1, MutableImage i2) {
        long squareError = 0;
        int numPixels = 0;

        // Stay in positive totalError values. Use floating point evaluation to merge values.
        if (i1.width > 128 || i2.width > 128 || i1.height > 128 || i2.height > 128) {
            throw new IllegalArgumentException("Slow comparison of images with any dimension larger than 128 is not supported");
        }
        // Iterate through larger dimensions for better accuracy
        if (i1.width > i2.width) {
            return compareSlow(i2, i1);
        }
        if (i2.height >= i1.height) {
            // i2 is wider and taller
            int p2 = i2.firstPixel;
            for (int y2 = 0; y2 < i2.height; y2++) {
                for (int x2 = 0; x2 < i2.width; x2++) {
                    int y1 = y2 * i1.height / i2.height;
                    int x1 = x2 * i1.width / i2.width;
                    int p1 = i1.firstPixel + i1.fullLine * y1 + x1;
                    int v1 = (int) i1.pixels[p1] & 0xff;
                    int v2 = (int) i2.pixels[p2] & 0xff;
                    squareError += (v2 - v1) * (v2 - v1);
                    numPixels++;
                    p2++;
                }
                p2 += i2.lineSpan;
            }
        } else {
            // i2 is wider but i1 is taller
            for (int y1 = 0; y1 < i1.height; y1++) {
                for (int x2 = 0; x2 < i2.width; x2++) {
                    int x1 = x2 * i1.width / i2.width;
                    int y2 = y1 * i2.height / i1.height;
                    int p2 = i2.firstPixel + i2.fullLine * y2 + x2;
                    int p1 = i1.firstPixel + i1.fullLine * y1 + x1;
                    int v1 = (int) i1.pixels[p1] & 0xff;
                    int v2 = (int) i2.pixels[p2] & 0xff;
                    squareError += (v2 - v1) * (v2 - v1);
                    numPixels++;
                }
            }
        }
        return new MSEResult(squareError, numPixels);
    }

    /**
     * Calculates mean square difference between two images. This algorithm uses smaller image as the base for
     * comparison, so it works faster but has worse statistics.
     *
     * @param i1 One of two images to compare.
     * @param i2 One of two images to compare.
     * @return MSEResult object containing sum of square error and number of pixels used. Means square error would be
     *         Math.sqrt(result.squareError / result.numPixels). result.squareError may be close to Long.MAX_VALUE, so
     *         do not add these values together in long format. Just switch to floating point evaluation.
     */
    public static MSEResult compareFast(MutableImage i1, MutableImage i2) {
        long squareError = 0;
        int numPixels = 0;

        // Stay in positive totalError values. Use floating point evaluation to merge values.
        if (Math.min(i1.width, i2.width) > 128 || Math.min(i1.height, i2.height) > 128) {
            throw new IllegalArgumentException("Fast comparison of images with common dimension larger than 128 is not supported");
        }
        // Iterate through larger dimensions for better accuracy
        if (i1.width > i2.width) {
            return compareFast(i2, i1);
        }
        if (i2.height >= i1.height) {
            // i2 is wider and taller
            int p1 = i1.firstPixel;
            for (int y1 = 0; y1 < i1.height; y1++) {
                for (int x1 = 0; x1 < i1.width; x1++) {
                    int y2 = y1 * i2.height / i1.height;
                    int x2 = x1 * i2.width / i1.width;
                    int p2 = i2.firstPixel + i2.fullLine * y2 + x2;
                    int v1 = (int) i1.pixels[p1] & 0xff;
                    int v2 = (int) i2.pixels[p2] & 0xff;
                    squareError += (v1 - v2) * (v1 - v2);
                    numPixels++;
                    p1++;
                }
                p1 += i1.lineSpan;
            }
        } else {
            // i2 is wider but i1 is taller
            for (int y2 = 0; y2 < i2.height; y2++) {
                for (int x1 = 0; x1 < i1.width; x1++) {
                    int x2 = x1 * i2.width / i1.width;
                    int y1 = y2 * i1.height / i2.height;
                    int p1 = i1.firstPixel + i1.fullLine * y1 + x1;
                    int p2 = i2.firstPixel + i2.fullLine * y2 + x2;
                    int v1 = (int) i1.pixels[p1] & 0xff;
                    int v2 = (int) i2.pixels[p2] & 0xff;
                    squareError += (v1 - v2) * (v1 - v2);
                    numPixels++;
                }
            }
        }
        return new MSEResult(squareError, numPixels);
    }

    /**
     * Private constructor prevents instantiation
     */
    private MSEUtil() {
    }

    public static class MSEResult {
        public final long squareError;
        public final int numPixels;

        public MSEResult(long squareError, int numPixels) {
            this.squareError = squareError;
            this.numPixels = numPixels;
        }
    }

}
