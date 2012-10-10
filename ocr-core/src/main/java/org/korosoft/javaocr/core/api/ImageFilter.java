package org.korosoft.javaocr.core.api;

import org.korosoft.javaocr.core.MutableImage;

/**
 * Common interface for mutable image filter.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public interface ImageFilter {
    /**
     * Filters the image.
     *
     * @param source Image to filter.
     * @return Result image. Returned value may be the same as {@code source} argument or a new {@link MutableImage} object.
     *         In-place image modification is preferred.
     */
    MutableImage doFilter(MutableImage source);
}
