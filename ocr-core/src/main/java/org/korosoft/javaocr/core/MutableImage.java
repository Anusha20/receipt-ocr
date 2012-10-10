package org.korosoft.javaocr.core;

/**
 * MutableImage is core implementation for mutable image container. Image data array is exposed to public for
 * performance reason.
 * <p/>
 * This class supports zero-copy creating of nested images which works pretty much like {@link String#substring(int, int)}
 * method. Since the array is mutable, all nested images are updated along with master image.
 * <p/>
 * 8 bits per grayscale pixel are used. Image is stored in a linear byte array. Index of the first pixel of the image is
 * stored in {@link MutableImage#firstPixel} field. Number of pixels to skip after the end of line to get to the beginning
 * of next line is stored in {@link MutableImage#lineSpan} field.
 * <p/>
 * Consider the image.
 * <pre>
 * + + + + +
 * + # # # +
 * + # # # +
 * + # # # +
 * + + + + +
 * </pre>
 * Its array representation is:
 * <pre>
 * 0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f  10 11 12 13 14 15 16 17 18
 * +  +  +  +  +  +  #  #  #  +  +  #  #  #  +  +  #  #  #  +  +  +  +  +  +
 * </pre>
 * Full image MutableImage object will have the following properties:
 * <p/>
 * - {@link MutableImage#pixels}: [+ + + + + + # # # + + # # # + + # # # + + + + + +]<br/>
 * - {@link MutableImage#firstPixel}: 0<br/>
 * - {@link MutableImage#lineSpan}: 0
 * <p/>
 * The region marked with '#' signs can be extracted as nested image (new MutableImage instance).
 * That image will have the following properties:
 * <p/>
 * - {@link MutableImage#pixels}: [+ + + + + + # # # + + # # # + + # # # + + + + + +] (the same object as previous image pixels array)<br/>
 * - {@link MutableImage#firstPixel}: 6<br/>
 * - {@link MutableImage#lineSpan}: 2
 * <p/>
 * Please be aware that this class is not thread safe.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public final class MutableImage {
    /**
     * Container for grayscale pixels of the image. Different images can share the same pixel container. Please read
     * documentation for {@link MutableImage#firstPixel} and {@link MutableImage#lineSpan} fields before you use this field.
     */
    public final byte[] pixels;
    /**
     * Index of the first pixel of the image in {@link MutableImage#pixels} array. First pixel is top-left corner of the image.
     * Data before the first pixels likely belongs to other images.
     */
    public final int firstPixel;
    /**
     * Number of bytes between end of previous pixel line and next pixel line in the {@link MutableImage#pixels} array.
     */
    public final int lineSpan;

    /**
     * Width of the image in pixels
     */
    public final int width;

    /**
     * Height of the image in pixels
     */
    public final int height;

    /**
     * Number of bytes between beginnings of two adjacent lines. Always equals to {@link #lineSpan} + {@link #width}
     */
    public final int fullLine;

    /**
     * Creates new MutableImage instance.
     *
     * @param pixels     Grayscale pixels array (see {@link MutableImage#pixels})
     * @param firstPixel First image pixel index (see {@link MutableImage#firstPixel}).
     * @param lineSpan   Number of pixels between lines (see {@link MutableImage#lineSpan}).
     * @param width      Image width in pixels.
     * @param height     Image height in pixels.
     * @see MutableImage
     */
    public MutableImage(byte[] pixels, int firstPixel, int lineSpan, int width, int height) {
        if (firstPixel + (width + lineSpan) * height - lineSpan > pixels.length) {
            throw new IllegalArgumentException("Image dimensions exceed data array size");
        }
        this.pixels = pixels;
        this.firstPixel = firstPixel;
        this.lineSpan = lineSpan;
        this.width = width;
        this.height = height;
        this.fullLine = lineSpan + width;
    }

    /**
     * Creates MutableImage object containing specified rectangle and sharing {@link MutableImage#pixels} array with
     * this image.
     *
     * @param left   left bound of the rectangle within current image.
     * @param top    top bound of the rectangle within current image.
     * @param width  rectangle width.
     * @param height rectangle height.
     * @return new MutableImage object.
     */
    public MutableImage subImage(int left, int top, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Requested image size is less than 0");
        }
        if (left < 0 || left + width > this.width || top < 0 || top + height > this.height) {
            throw new IllegalArgumentException("Requested image exceeds image bounds");
        }
        if (top == 0) {
            return new MutableImage(pixels, firstPixel + left, lineSpan + (this.width - width), width, height);
        } else {
            return new MutableImage(pixels, firstPixel + left + top * (this.width + lineSpan), lineSpan + (this.width - width), width, height);
        }
    }

    /**
     * Returns MutableImage object with defragmented pixels array (without unused bytes). Consider example: when initial image is:
     * <p/>
     * - {@link MutableImage#pixels}: [+ + + + + + # # # + + # # # + + # # # + + + + + +]<br/>
     * - {@link MutableImage#firstPixel}: 6<br/>
     * - {@link MutableImage#lineSpan}: 2<br/>
     * - {@link MutableImage#width}: 3<br/>
     * - {@link MutableImage#height}: 3
     * <p/>
     * Result value will be:
     * <p/>
     * - {@link MutableImage#pixels}: [# # # # # # # # #]<br/>
     * - {@link MutableImage#firstPixel}: 0<br/>
     * - {@link MutableImage#lineSpan}: 0<br/>
     * - {@link MutableImage#width}: 3<br/>
     * - {@link MutableImage#height}: 3
     *
     * @param duplicate pass {@code true} value to have a guarantee that image data is copied to a separate array
     *                  (i.e. unbound from master image and other nested images if any).
     *                  <p/>
     *                  When image {@link MutableImage#firstPixel} and {@link MutableImage#lineSpan} both equal to zero,
     *                  {@link MutableImage#pixels} array length equals to {@link MutableImage#width} * {@link MutableImage#height} and
     *                  {@code duplicate} argument is {@code false}, this method returns the same image without doing
     *                  anything.
     * @return MutableImage object containing the same image with defragmented pixels array.
     */
    public MutableImage intern(boolean duplicate) {
        if (firstPixel == 0 && lineSpan == 0 && pixels.length == width * height) {
            if (duplicate) {
                final byte[] newPixels = new byte[pixels.length];
                System.arraycopy(pixels, 0, newPixels, 0, pixels.length);
                return new MutableImage(newPixels, 0, 0, width, height);
            } else {
                return this;
            }
        }
        final byte[] newPixels = new byte[width * height];
        int newPos = 0;
        int pos = firstPixel;
        for (int y = 0; y < height; y++) {
            System.arraycopy(pixels, pos, newPixels, newPos, width);
            pos += fullLine;
            newPos += width;
        }
        return new MutableImage(newPixels, 0, 0, width, height);
    }

    /**
     * Calculates pixel index in {@link #pixels} array by coordinates. Do not use this method when iterating through pixels. Increment the index
     * as described in {@link MutableImage} class JavaDoc.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return Pixel index.
     */
    @Deprecated
    public int getPixelIndex(int x, int y) {
        return firstPixel + y * fullLine + x;
    }
}
