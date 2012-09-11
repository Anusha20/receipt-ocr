package org.korosoft.javaocr.core;

/**
 * Mean square error comparator for {@link MutableImage} instances.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public final class MSEUtil {
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
            if (i1.height >= i2.height) {
                // i1 is wider and taller
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
                // i1 is wider but i2 is taller
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
        } else {
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
            if (i1.height >= i2.height) {
                // i1 is wider and taller
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
                // i1 is wider but i2 is taller
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
        } else {
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
