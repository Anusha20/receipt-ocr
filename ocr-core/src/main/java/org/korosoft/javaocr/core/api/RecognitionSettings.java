package org.korosoft.javaocr.core.api;

/**
 * Recognition settings
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public final class RecognitionSettings {
    /**
     * Comparisons with MSE score grater than symbolRecognitionThreshold are considered to be totally different.
     */
    public final double symbolRecognitionThreshold;
    /**
     * Space aspect ratio threshold. Spaces narrower than w/h=whiteSpaceThreshold are not considered to be space symbols.
     */
    public final double whiteSpaceThreshold;
    /**
     * Comparisons with MSE score less than exactMatchThreshold are considered to be equal and no further symbol search is performed.
     */
    public final double exactMatchThreshold;

    /**
     * Creates RecognitionSettings instance.
     *
     * @param symbolRecognitionThreshold Comparisons with MSE score grater than symbolRecognitionThreshold are considered to be totally different.
     * @param whiteSpaceThreshold        Space aspect ratio threshold. Spaces narrower than w/h=whiteSpaceThreshold are not considered to be space symbols.
     * @param exactMatchThreshold        Comparisons with MSE score less than exactMatchThreshold are considered to be equal and no further symbol search is performed.
     */
    public RecognitionSettings(double symbolRecognitionThreshold, double whiteSpaceThreshold, double exactMatchThreshold) {
        this.symbolRecognitionThreshold = symbolRecognitionThreshold;
        this.whiteSpaceThreshold = whiteSpaceThreshold;
        this.exactMatchThreshold = exactMatchThreshold;
    }
}
