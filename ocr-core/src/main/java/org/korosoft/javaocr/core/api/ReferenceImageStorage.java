package org.korosoft.javaocr.core.api;

import org.korosoft.javaocr.core.MutableImage;

import java.util.List;

/**
 * ReferenceImageStorage defines abstract reference image storage for {@link org.korosoft.javaocr.core.OCRScanner} class.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public interface ReferenceImageStorage {
    /**
     * Returns all the reference images ordered by {@link ReferenceImage#getCorrectDetections()} / {@link ReferenceImage#getAllDetections()}.
     *
     * @return all the reference images ordered by {@link ReferenceImage#getCorrectDetections()} / {@link ReferenceImage#getAllDetections()}.
     */
    List<ReferenceImage> getAllReferenceImages();

    /**
     * Registers new reference image. New reference image should have correct detections count and all detections count equal to 1.
     *
     * @param image Image to register.
     */
    ReferenceImage addReferenceImage(MutableImage image, char symbol);
}
