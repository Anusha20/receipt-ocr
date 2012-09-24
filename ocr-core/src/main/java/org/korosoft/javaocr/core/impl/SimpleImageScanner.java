package org.korosoft.javaocr.core.impl;

import org.korosoft.javaocr.core.MutableImage;
import org.korosoft.javaocr.core.api.ImageScanner;

/**
 * Simple {@link ImageScanner} implementation
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class SimpleImageScanner implements ImageScanner {
    private Settings settings = new Settings();

    public Settings getSettings() {
        return settings;
    }

    public void scan(MutableImage image, Callback callback) {
        boolean isWhitespaceNow = true;
        boolean isFirstLine = true;
        if (image.height <= 0) {
            callback.onFinished();
            return;
        }
        final int scores[] = new int[image.height];
        int lineTop = -1;
        int scoreY = 0;
        for (int i = 0, p = image.firstPixel; i < image.height; i++, p += image.fullLine) {
            final int lineColorScore = getLineColorScore(image, p);
            if (lineColorScore == 0) {
                if (!isWhitespaceNow) {
                    if (!isFirstLine) {
                        callback.onNewLine();
                    }
                    // Find a base line
                    int avgLineColorScore = scores[0];
                    for (int j = 1; j < scoreY; j++) {
                        avgLineColorScore += scores[j];
                    }
                    avgLineColorScore /= scoreY;
                    int baseLine = 0;
                    for (int j = scoreY - 1; j >= 0; j--) {
                        if (scores[j] * settings.baseLineFactor > avgLineColorScore) {
                            baseLine = j;
                            break;
                        }
                    }

                    parseLine(image.subImage(0, lineTop, image.width, i - lineTop), lineTop, baseLine, callback);
                    isFirstLine = false;
                    isWhitespaceNow = true;
                }
            } else {
                if (isWhitespaceNow) {
                    isWhitespaceNow = false;
                    lineTop = i;
                    scores[0] = lineColorScore;
                    scoreY = 1;
                } else {
                    scores[scoreY] = lineColorScore;
                    scoreY++;
                }
            }
        }
        callback.onFinished();
    }

    private void parseLine(MutableImage image, int lineTop, int baseLine, Callback callback) {
        boolean isWhitespaceNow = true;
        boolean hadSymbol = false;
        int symbolLeft = -1;
        for (int x = 0, topPos = image.firstPixel; x < image.width; x++, topPos++) {
            if (isWhitespaceColumn(image, topPos)) {
                if (!isWhitespaceNow) {
                    callback.onNewSymbol(image.subImage(symbolLeft, 0, x - symbolLeft, image.height), symbolLeft, lineTop, baseLine);
                    hadSymbol = true;
                    symbolLeft = x;
                    isWhitespaceNow = true;
                }
            } else {
                if (isWhitespaceNow) {
                    if (hadSymbol) {
                        callback.onWhitespace(image.height, x - symbolLeft);
                    }
                    isWhitespaceNow = false;
                    symbolLeft = x;
                }
            }
        }
    }

    private boolean isWhitespaceColumn(MutableImage image, int p) {
        for (int ry = image.height; ry > 0; ry--) {
            int v = (int) image.pixels[p] & 0xff;
            if (v < settings.whiteThreshold) {
                return false;
            }
            p += image.fullLine;
        }
        return true;
    }

    /**
     * Calculates line color score. Zero means 100% white line.
     *
     * @param image Image to scan
     * @param p     Position to start at
     * @return line color score
     */
    private int getLineColorScore(MutableImage image, int p) {
        int score = 0;
        for (int rx = image.width; rx > 0; rx--) {
            final int v = (int) image.pixels[p++] & 0xff;
            if (v < settings.whiteThreshold) {
                score += (255 - v);
            }
        }
        return score;
    }

    public class Settings {
        /**
         * White color lower threshold. All colors with color greater than {@code whiteThreshold} are considered
         * to be 100% white (equal to 255)
         */
        private int whiteThreshold = 192;

        /**
         * Base line detection factor. Lower line of pixels with total intensity multiplied by {@code baseLineFactor}
         * higher than average line intensity is considered to be a base line
         */
        private int baseLineFactor = 4;

        public int getWhiteThreshold() {
            return whiteThreshold;
        }

        public void setWhiteThreshold(int whiteThreshold) {
            this.whiteThreshold = whiteThreshold;
        }

        public int getBaseLineFactor() {
            return baseLineFactor;
        }

        public void setBaseLineFactor(int baseLineFactor) {
            this.baseLineFactor = baseLineFactor;
        }
    }
}
