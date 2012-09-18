package org.korosoft.javaocr.core.api;

import org.korosoft.javaocr.core.MutableImage;

/**
 * ImageComparator defines image comparator tool for {@link org.korosoft.javaocr.core.OCRScanner} class.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public interface ImageComparator {
    double compareImages(MutableImage image1, MutableImage image2);
}