// ImageShearer.java
// Copyright (c) 2011 Dmitry V. Korotkov
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.package net.sourceforge.javaocr.ocrPlugins.imgShearer;
package net.sourceforge.javaocr.ocrPlugins.imgShearer;

import net.sourceforge.javaocr.scanner.DocumentScanner;
import net.sourceforge.javaocr.scanner.PixelImage;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class to shear initially roughly positioned document (such as receipt photo made with cellphone camera).
 * Shearing solves problem of inability to split in symbols slightly rotated images due to inability of resolving rows.
 * <p/>
 * Algorithm is based on splitting the image to several narrow columns, finding lines in them and then shearing the
 * image according difference between found gaps.
 *
 * @author Dmitry Korotkov
 */
public class ImageShearer {
    /**
     * Max allowed max row shift limit. Since array of integers covering all possible shift value is created,
     * shift bounds should be reasonable.
     */
    public static final int MAX_ALLOWED_ROW_SHIFT = 1000;
    /**
     * Number of the columns of the image considered to be narrow enough to have horizontal rows of white lines.
     */
    private int columnCount = 10;

    /**
     * Maximum row shift per piece in pixel.
     */
    private int maxRowShift = 100;

    /**
     * Returns the number of columns image will be internally split into.
     *
     * @return the number of columns image will be internally split into.
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * Set the number of columns to split image into to find lines.
     *
     * @param columnCount the number of columns to split image into to find lines.
     */
    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * Returns max allowed row shift between two adjacent columns.
     *
     * @return max allowed row shift between two adjacent columns.
     */
    public int getMaxRowShift() {
        return maxRowShift;
    }

    /**
     * Sets max allowed row shift between two adjacent columns. Greater shifts are ignored when parsing an image.
     * Values greater than {@link ImageShearer#MAX_ALLOWED_ROW_SHIFT} are not allowed.
     *
     * @param maxRowShift max allowed row shift between two adjacent columns.
     */
    public void setMaxRowShift(int maxRowShift) {
        if (maxRowShift > 1000) {
            throw new IllegalArgumentException("Max row shift greater than " + MAX_ALLOWED_ROW_SHIFT + " is not allowed");
        }
        this.maxRowShift = maxRowShift;
    }

    /**
     * Shortcut to {@link ImageShearer#shearImage(net.sourceforge.javaocr.scanner.DocumentScanner, net.sourceforge.javaocr.scanner.PixelImage, int, int, int, int)}.
     * Passes 0, 0, imageWidth, imageHeight as dimension arguments.
     *
     * @param scanner {@link DocumentScanner} to find lines.
     * @param source  Source image.
     * @return New updated {@link PixelImage} instance.
     */
    public PixelImage shearImage(DocumentScanner scanner, PixelImage source) {
        return shearImage(scanner, source, 0, 0, source.width, source.height);
    }

    /**
     * Detects image rotation and shears the image to compensate the rotation.
     *
     * @param scanner Document Scanner to use for finding lines.
     * @param source  Source image.
     * @param blockX1 Block to scan lines in (left).
     * @param blockY1 Block to scan lines in (top).
     * @param blockX2 Block to scan lines in (right).
     * @param blockY2 Block to scan lines in (bottom).
     * @return New sheared instance of source image.
     */
    public PixelImage shearImage(DocumentScanner scanner, PixelImage source, int blockX1, int blockY1, int blockX2, int blockY2) {
        final int[] pixels = source.pixels;
        final int w = source.width;
        final int h = source.height;
        @SuppressWarnings({"unchecked"}) final List<Integer> rows[] = new List[columnCount];

        // Find rows in each column
        for (int i = 0; i < rows.length; i++) {
            int x1 = blockX1 + (blockX2 - blockX1) * i / columnCount;
            int x2 = blockX1 + (blockX2 - blockX1) * (i + 1) / columnCount;
            rows[i] = scanner.extractRows(source, x1, blockY1, x2, blockY2);
        }

        // This array contains count of each found shift occurrence
        int shifts[] = new int[maxRowShift * 2 + 1];

        // Walk through all columns (except the rightmost). For each column walk through all rows.
        // For each row find the nearest row in next column to the right. Calculate difference between
        // these two rows and increment corresponding value in shifts array.
        // Since DocumentScanner finds row top and bottom lines, process even and odd row lists elements
        // separately.
        for (int i = 0; i < columnCount - 1; i++) {
            // process even rows
            for (int j = 0, sj = rows[i].size(); j < sj; j += 2) {
                int shift = maxRowShift + 1;
                for (int k = 0, sk = rows[i + 1].size(); k < sk; k += 2) {
                    final int x = rows[i + 1].get(k) - rows[i].get(j);
                    if (Math.abs(x) < Math.abs(shift)) {
                        shift = x;
                    }
                }
                if (Math.abs(shift) <= maxRowShift) {
                    shifts[maxRowShift + shift]++;
                }
            }
            // process odd rows
            for (int j = 1, s = rows[i].size(); j < s; j += 2) {
                int shift = maxRowShift + 1;
                for (int k = 1, sk = rows[i + 1].size(); k < sk; k += 2) {
                    final int x = rows[i + 1].get(k) - rows[i].get(j);
                    if (Math.abs(x) < Math.abs(shift)) {
                        shift = x;
                    }
                }
                if (Math.abs(shift) <= maxRowShift) {
                    shifts[maxRowShift + shift]++;
                }
            }
        }

        // Find the most frequent shift index (maps to shift value as index - maxRowShift)
        int maxShiftIdx = 0;
        for (int i = 0; i < shifts.length; i++) {
            if (shifts[i] > shifts[maxShiftIdx]) {
                maxShiftIdx = i;
            }
        }

        // Calculate average shift between found adjacent shift values surrounding the most frequent shift.
        // Calculate numerator (sum) and denominator (cnt) separately
        int shiftSum = shifts[maxShiftIdx] * (maxShiftIdx - maxRowShift);
        int shiftCnt = shifts[maxShiftIdx];

        // Add lesser shifts until the gap in found shifts array
        for (int pos = maxShiftIdx - 1; pos > 0 && shifts[pos] > 0; pos--) {
            shiftSum += shifts[pos] * (pos - maxRowShift);
            shiftCnt += shifts[pos];
        }

        // Add greater shifts until the gap in found shifts array
        for (int pos = maxShiftIdx - 1; pos < shifts.length && shifts[pos] > 0; pos++) {
            shiftSum += shifts[pos] * (pos - maxRowShift);
            shiftCnt += shifts[pos];
        }

        // Calculate the average shift
        double shiftPerPiece = shiftSum * 1.0 / shiftCnt;
        //System.out.println("Row shift per piece is: " + shiftPerPiece + " px");

        return shearImage(source, shiftPerPiece);
    }

    /**
     * Shears provided image my specified number of pixels per column.
     *
     * @param source         Image to shear.
     * @param shiftPerColumn Shift per column.
     * @return Sheared image.
     */
    private PixelImage shearImage(PixelImage source, double shiftPerColumn) {
        int[] pixels = source.pixels;
        int w = source.width;
        int h = source.height;
        int newPixels[] = new int[pixels.length];
        Arrays.fill(newPixels, 255);
        PixelImage result = new PixelImage(newPixels, w, h);

        for (int x = 0; x < w; x++) {
            int yShift = (int) (x * shiftPerColumn / w * columnCount);
            for (int y = 0; y < h; y++) {
                int yOrig = y + yShift;
                if (yOrig < 0) {
                    yOrig = 0;
                }
                if (yOrig >= h) {
                    yOrig = h - 1;
                }
                newPixels[y * w + x] = pixels[yOrig * w + x];
            }
        }

        return result;
    }
}

