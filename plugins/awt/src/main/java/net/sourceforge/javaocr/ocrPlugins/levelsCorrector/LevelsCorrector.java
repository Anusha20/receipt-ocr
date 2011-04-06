// LevelsCorrector.java
// Copyright (c) 2011 Dmitry V. Korotkov
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.package net.sourceforge.javaocr.ocrPlugins.imgShearer;
package net.sourceforge.javaocr.ocrPlugins.levelsCorrector;

import net.sourceforge.javaocr.scanner.PixelImage;

/**
 * Utility class to correct level based on idea that at the center of the image should be high-contrast image to
 * be improved. It is suitable for correcting photo images of receipts.
 * <p/>
 * This class works only with grayscale images.
 *
 * @author Dmitry Korotkov
 */
public class LevelsCorrector {
    private double centralSquareDimension = 0.3;
    private double whiteThreshold = 0.5;
    private static final int WHITE = 255;
    private static final int BLACK = 0;

    public void adjustImageLevels(PixelImage pixelImage) {
        int xMin = (int) (pixelImage.width * (1 - centralSquareDimension) / 2);
        int yMin = (int) (pixelImage.height * (1 - centralSquareDimension) / 2);
        int xMax = pixelImage.width - xMin;
        int yMax = pixelImage.height - yMin;

        int levels[] = new int[256];

        // Calculate levels
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                levels[pixelImage.pixels[y * pixelImage.width + x] & 255]++;
            }
        }

        int topLevelIdx = 0;
        int lowerLevelIdx = -1;

        // Find 'white' level and 'black' level.
        // White is the most frequent, Black is the darkest
        for (int i = 0; i < levels.length; i++) {
            if (levels[topLevelIdx] < levels[i]) {
                topLevelIdx = i;
            }
            if (lowerLevelIdx == -1 && levels[i] > 0) {
                lowerLevelIdx = i;
            }

        }

        // calculate read white threshold
        int thresholdLevel = (int) (lowerLevelIdx + (topLevelIdx -  lowerLevelIdx) * whiteThreshold);

        // update levels
        for (int i = pixelImage.width * pixelImage.height - 1; i >= 0; i--) {
            pixelImage.pixels[i] = pixelImage.pixels[i] < thresholdLevel ? BLACK : WHITE;
        }
    }

    // Getters and setters


    public double getCentralSquareDimension() {
        return centralSquareDimension;
    }

    public void setCentralSquareDimension(double centralSquareDimension) {
        this.centralSquareDimension = centralSquareDimension;
    }

    public double getWhiteThreshold() {
        return whiteThreshold;
    }

    public void setWhiteThreshold(double whiteThreshold) {
        this.whiteThreshold = whiteThreshold;
    }
}
