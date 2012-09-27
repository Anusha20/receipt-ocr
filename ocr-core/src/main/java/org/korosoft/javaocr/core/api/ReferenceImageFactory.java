package org.korosoft.javaocr.core.api;

import org.korosoft.javaocr.core.MutableImage;

/**
 * ReferenceImageFactory defines reference image factory for {@link org.korosoft.javaocr.core.impl.OCRScanner} class.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public interface ReferenceImageFactory {
    ReferenceImage createReferenceImage(MutableImage image, char symbol);
}
