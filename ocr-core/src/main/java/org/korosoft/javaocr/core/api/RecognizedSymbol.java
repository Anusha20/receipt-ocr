package org.korosoft.javaocr.core.api;

import org.korosoft.javaocr.core.MutableImage;

/**
 * Recognized symbol
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public final class RecognizedSymbol {
    /**
     * Symbol image.
     */
    public final MutableImage image;
    /**
     * Symbol x coordinate on main image.
     */
    public final int x;
    /**
     * Symbol y coordinate on main image.
     */
    public final int y;
    /**
     * Symbol base line (measured from the top of symbol).
     */
    public final int baseLine;
    /**
     * Matched reference image (can be {@code null} if recognition has failed).
     */
    public final ReferenceImage referenceImage;
    /**
     * Recognition score (lower is better).
     */
    public final double score;

    /**
     * Creates new RecognizedSymbol instance.
     *
     * @param image          Symbol image.
     * @param x              Symbol x coordinate on main image.
     * @param y              Symbol y coordinate on main image.
     * @param baseLine       Symbol base line (measured from the top of symbol).
     * @param referenceImage Matched reference image (can be {@code null} if recognition has failed).
     * @param score          Recognition score (lower is better).
     */
    public RecognizedSymbol(MutableImage image, int x, int y, int baseLine, ReferenceImage referenceImage, double score) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.score = score;
        this.referenceImage = referenceImage;
        this.baseLine = baseLine;
    }
}
