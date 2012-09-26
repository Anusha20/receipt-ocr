package org.korosoft.javaocr.core.api;

import org.korosoft.javaocr.core.MutableImage;

/**
 * ReferenceImage defines reference image for {@link org.korosoft.javaocr.core.OCRScanner} class.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public interface ReferenceImage {
    /**
     * Returns symbol shown on the reference image.
     *
     * @return symbol shown on the reference image.
     */
    String getSymbol();

    /**
     * Returns reference image.
     *
     * @return Reference image.
     */
    MutableImage getImage();

    /**
     * Returns symbol base line.
     *
     * @return symbol base line.
     */
    int getBaseLine();

    /**
     * Returns number of correct symbol detections made.
     *
     * @return number of correct symbol detections made.
     */
    int getCorrectDetections();

    /**
     * Returns number of all symbol detection attempts made.
     *
     * @return number of all symbol detection attempts made.
     */
    int getAllDetections();

    /**
     * Registers one more symbol detection.
     *
     * @param correct {@code true} when the detection was correct (normally user should be asked whether the detection was correct)
     */
    void registerDetection(boolean correct);
}
