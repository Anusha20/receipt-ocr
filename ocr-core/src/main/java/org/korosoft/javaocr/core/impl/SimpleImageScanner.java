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
        int lineTop = -1;
        for (int i = 0, p = image.firstPixel; i < image.height; i++, p += image.fullLine) {
            if (isWhitespaceLine(image, p)) {
                if (!isWhitespaceNow) {
                    if (!isFirstLine) {
                        callback.onNewLine();
                    }
                    parseLine(image.subImage(0, lineTop, image.width, i - lineTop), lineTop, callback);
                    isFirstLine = false;
                    isWhitespaceNow = true;
                }
            } else {
                if (isWhitespaceNow) {
                    isWhitespaceNow = false;
                    lineTop = i;
                }
            }
        }
        callback.onFinished();
    }

    private void parseLine(MutableImage image, int lineTop, Callback callback) {
        boolean isWhitespaceNow = true;
        boolean hadSymbol = false;
        int symbolLeft = -1;
        for (int x = 0, topPos = image.firstPixel; x < image.width; x++, topPos++) {
            if (isWhitespaceColumn(image, topPos)) {
                if (!isWhitespaceNow) {
                    callback.onNewSymbol(image.subImage(symbolLeft, 0, x - symbolLeft, image.height), symbolLeft, lineTop);
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

    private boolean isWhitespaceLine(MutableImage image, int p) {
        for (int rx = image.width; rx > 0; rx--) {
            int v = (int) image.pixels[p++] & 0xff;
            if (v < settings.whiteThreshold) {
                return false;
            }
        }
        return true;
    }

    public class Settings {
        private int whiteThreshold = 192;

        public int getWhiteThreshold() {
            return whiteThreshold;
        }

        public void setWhiteThreshold(int whiteThreshold) {
            this.whiteThreshold = whiteThreshold;
        }
    }
}
