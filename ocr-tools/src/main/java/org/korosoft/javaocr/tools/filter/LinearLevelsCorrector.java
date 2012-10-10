package org.korosoft.javaocr.tools.filter;

import org.korosoft.javaocr.core.MutableImage;

public class LinearLevelsCorrector extends AbstractLevelsCorrector {
    @Override
    protected void correctLevels(MutableImage source, int whiteColor, int blackColor) {
        if (whiteColor == blackColor) {
            return;
        }

        final byte[] pixels = source.pixels;
        final int w = source.width;
        final int h = source.height;
        int p;// calculate real white threshold

        // update levels
        p = source.firstPixel;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int colorValue = (((int) pixels[p] & 255) - blackColor) * 255 / (whiteColor - blackColor);
                if (colorValue < 0) {
                    colorValue = 0;
                } else if (colorValue > 255) {
                    colorValue = 255;
                }
                pixels[p] = (byte) colorValue;
                p++;
            }
            p += source.lineSpan;
        }
    }
}
