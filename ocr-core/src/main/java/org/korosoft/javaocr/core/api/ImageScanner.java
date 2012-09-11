package org.korosoft.javaocr.core.api;

import org.korosoft.javaocr.core.MutableImage;

/**
 * Image scanner finds lines and individual characters in the document. Found sequences of characters are reported by
 * {@link ImageScanner.Callback} methods invocations.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public interface ImageScanner {
    /**
     * Scans provided image in order to find sequences of characters. Implement this interface to provide more
     * sophisticated symbols finder. This method should be always executed synchronously.
     *
     * @param image    Image to scan for symbols.
     * @param callback Scanner callback.
     */
    void scan(MutableImage image, Callback callback);

    /**
     * Implement this interface to obtain output from {@link ImageScanner#scan(org.korosoft.javaocr.core.MutableImage, org.korosoft.javaocr.core.api.ImageScanner.Callback)} method.
     *
     * @author Dmitry Korotkov
     * @since 1.0
     */
    public interface Callback {
        /**
         * This method is invoked when logical line of symbols is ended on the scanned image.
         */
        void onNewLine();

        /**
         * This method is invoked when a symbol is detected in the image. Note that the image is not internalized, so
         * if you need to store it or to keep it in memory, you should call {@link MutableImage#intern(boolean)} method.
         *
         * @param symbol Found symbol image.
         */
        void onNewSymbol(MutableImage symbol, int x, int y);

        /**
         * This method is invoked when a whitespace is detected.
         *
         * @param lineHeight height of the line whitespace is detected in.
         * @param width      width of the whitespace in pixels.
         */
        void onWhitespace(int lineHeight, int width);
    }
}
