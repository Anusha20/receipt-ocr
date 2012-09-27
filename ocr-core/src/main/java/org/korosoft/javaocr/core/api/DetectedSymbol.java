package org.korosoft.javaocr.core.api;

import org.korosoft.javaocr.core.MutableImage;

/**
 * Found symbol coordinates
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public final class DetectedSymbol {
    public final MutableImage image;
    public final int x;
    public final int y;
    public final int baseLine;

    public DetectedSymbol(MutableImage image, int x, int y, int baseLine) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.baseLine = baseLine;
    }
}
