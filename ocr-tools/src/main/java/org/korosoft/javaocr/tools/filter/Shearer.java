package org.korosoft.javaocr.tools.filter;

import org.korosoft.javaocr.core.MutableImage;
import org.korosoft.javaocr.core.api.ImageFilter;
import org.korosoft.javaocr.core.impl.SimpleImageScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Filter to shear initially roughly positioned document (such as receipt photo made with cellphone camera).
 * Shearing solves problem of inability to split in symbols slightly rotated images due to inability of resolving rows.
 * <p/>
 * Algorithm is based on splitting the image to several narrow columns, finding lines in them and then shearing the
 * image according difference between found gaps.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class Shearer implements ImageFilter {
    /**
     * Max allowed max row shift limit. Since array of integers covering all possible shift value is created,
     * shift bounds should be reasonable.
     */
    public static final int MAX_ALLOWED_ROW_SHIFT = 1000;
    public static final double DELTA = 0.01;
    /**
     * Number of the columns of the image considered to be narrow enough to have horizontal rows of white lines.
     */
    private int columnCount = 10;

    /**
     * Maximum row shift per piece in pixel.
     */
    //ToDo: make 100
    private int maxRowShift = 50;

    /**
     * Background color for new pixels appearing after shearing
     */
    private byte backgroundColor = -1;

    /**
     * Image scanner settings used to discover lines.
     */
    private SimpleImageScanner.Settings settings = new SimpleImageScanner.Settings();

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
     * Returns background color for new pixels appearing after shearing.
     *
     * @return Background color for new pixels appearing after shearing.
     */
    public byte getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets background color for new pixels appearing after shearing.
     *
     * @param backgroundColor Background color for new pixels appearing after shearing.
     */
    public void setBackgroundColor(byte backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Returns image scanner settings used to discover lines.
     *
     * @return Image scanner settings used to discover lines.
     * @see SimpleImageScanner.Settings
     */
    public SimpleImageScanner.Settings getSettings() {
        return settings;
    }

    /**
     * Sets image scanner settings used to discover lines.
     *
     * @param settings Image scanner settings used to discover lines.
     * @see SimpleImageScanner.Settings
     */
    public void setSettings(SimpleImageScanner.Settings settings) {
        this.settings = settings;
    }

    /**
     * Sets max allowed row shift between two adjacent columns. Greater shifts are ignored when parsing an image.
     * Values greater than {@link #MAX_ALLOWED_ROW_SHIFT} are not allowed.
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
     * Detects image rotation and shears the image to compensate the rotation.
     *
     * @param source Source image.
     * @return The same image.
     */
    public MutableImage doFilter(MutableImage source) {
        final int w = source.width;
        final int h = source.height;
        @SuppressWarnings({"unchecked"}) final List<Integer> rows[] = new List[columnCount];

        // Find rows in each column
        for (int i = 0; i < rows.length; i++) {
            final int x1 = w * i / columnCount;
            final int x2 = w * (i + 1) / columnCount;
            MutableImage columnImage = source.subImage(x1, 0, x2 - x1, h);
            rows[i] = findLines(columnImage);
        }

        // This array contains count of each found shift occurrence
        final int shifts[] = new int[maxRowShift * 2 + 1];

        // Walk through all columns (except the rightmost). For each column walk through all rows.
        // For each row find the nearest row in next column to the right. Calculate difference between
        // these two rows and increment corresponding value in shifts array.
        // Since DocumentScanner finds row top and bottom lines, process even and odd row lists elements
        // separately.
        for (int i = 0; i < columnCount - 1; i++) {
            // process even rows
            for (int j = 0, sj = rows[i].size(); j < sj; j++) {
                int shift = maxRowShift + 1;
                for (int k = 0, sk = rows[i + 1].size(); k < sk; k ++) {
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
        int mostFrequentShiftIdx = 0;
        for (int i = 1; i < shifts.length; i++) {
            if (shifts[i] > shifts[mostFrequentShiftIdx]) {
                mostFrequentShiftIdx = i;
            }
        }

        // Find all the shift indices at max frequency and build Gaussian distribution
        final double gaussian[] = new double[maxRowShift * 2 + 1];
        int mostFrequentShift = shifts[mostFrequentShiftIdx];
        for (int i = 0, shiftsLength = shifts.length; i < shiftsLength; i++) {
            if (shifts[i] == mostFrequentShift) {
                // Increment gaussian array with de-normalized Gaussian distribution curve
                for (int j = 0; j < shifts.length; j++) {
                    double delta = j - i;
                    gaussian[j] += Math.exp(-0.1 * delta * delta);
                }
            }
        }

        // Recalculate most frequent shift index by using gaussian curve. Pick closer to "0" if there are similar values
        mostFrequentShiftIdx = 0;
        for (int i = 1; i < gaussian.length; i++) {
            if (gaussian[i] > gaussian[mostFrequentShiftIdx]) {
                mostFrequentShiftIdx = i;
            }
            if (Math.abs(gaussian[i] - gaussian[mostFrequentShiftIdx]) < DELTA) {
                if (i <= maxRowShift) {
                    mostFrequentShiftIdx = i;
                }
            }
        }
//        mostFrequentShift = shifts[mostFrequentShiftIdx];

        // Calculate average shift between found adjacent shift values surrounding the most frequent shift.
        // Calculate numerator (sum) and denominator (cnt) separately
        int shiftSum = shifts[mostFrequentShiftIdx] * (mostFrequentShiftIdx - maxRowShift);
        int shiftCnt = shifts[mostFrequentShiftIdx];

        // Add lesser shifts until the gap in found shifts array
        for (int pos = mostFrequentShiftIdx - 1; pos > 0 && shifts[pos] > 0; pos--) {
            shiftSum += shifts[pos] * (pos - maxRowShift);
            shiftCnt += shifts[pos];
        }

        // Add greater shifts until the gap in found shifts array
        for (int pos = mostFrequentShiftIdx + 1; pos < shifts.length && shifts[pos] > 0; pos++) {
            shiftSum += shifts[pos] * (pos - maxRowShift);
            shiftCnt += shifts[pos];
        }

        // Calculate the average shift
        double shiftPerPiece = shiftSum * 1.0 / shiftCnt;

        shearImage(source, shiftPerPiece);

        return source;
    }

    private List<Integer> findLines(MutableImage image) {
        boolean isWhitespaceNow = true;
        boolean isFirstLine = true;
        if (image.height <= 0) {
            return Collections.emptyList();
        }
        int lineTop = -1;
        final List<Integer> result = new ArrayList<Integer>();
        for (int i = 0, p = image.firstPixel; i < image.height; i++, p += image.fullLine) {
            final int lineColorScore = getLineColorScore(image, p);
            if (lineColorScore == 0) {
                if (!isWhitespaceNow) {
                    if (!isFirstLine) {
                        result.add(lineTop);
                    }
                    isFirstLine = false;
                    isWhitespaceNow = true;
                }
            } else {
                if (isWhitespaceNow) {
                    isWhitespaceNow = false;
                    lineTop = i;
                }
            }
        }
        return result;
    }

    /**
     * Calculates line color score. Zero means 100% white line.
     *
     * @param image Image to scan
     * @param p     Position to start at
     * @return line color score
     */
    private int getLineColorScore(MutableImage image, int p) {
        int score = 0;
        final int whiteThreshold = settings.getWhiteThreshold();
        for (int rx = image.width; rx > 0; rx--) {
            final int v = (int) image.pixels[p++] & 0xff;
            if (v < whiteThreshold) {
                score += (255 - v);
            }
        }
        return score;
    }

    /**
     * Shears provided image my specified number of pixels per column.
     *
     * @param source         Image to shear.
     * @param shiftPerColumn Shift per column.
     */
    private void shearImage(MutableImage source, double shiftPerColumn) {
        if (shiftPerColumn < 0) { // Shifting down
            for (int x = 0; x < source.width; x++) {
                int dy = (int) (shiftPerColumn * ((double) x / (double) source.width * (double) columnCount));
                if (dy == 0) {
                    continue;
                }
                int pNew = source.firstPixel + source.fullLine * (source.height - 1) + x;
                int pOld = pNew + source.fullLine * dy;
                while (pOld >= source.firstPixel) {
                    source.pixels[pNew] = source.pixels[pOld];
                    pOld -= source.fullLine;
                    pNew -= source.fullLine;
                }
                while (pNew >= source.firstPixel) {
                    source.pixels[pNew] = backgroundColor;
                    pNew -= source.fullLine;
                }
            }
        } else { // Shifting up
            int lastPixel = source.firstPixel + source.fullLine * source.height - source.lineSpan;
            for (int x = 0; x < source.width; x++) {
                int dy = (int) (shiftPerColumn * ((double) x / (double) source.width * (double) columnCount));
                if (dy == 0) {
                    continue;
                }
                int pNew = source.firstPixel + x;
                int pOld = pNew + source.fullLine * dy;
                while (pOld < lastPixel) {
                    source.pixels[pNew] = source.pixels[pOld];
                    pOld += source.fullLine;
                    pNew += source.fullLine;
                }
                while (pNew < lastPixel) {
                    source.pixels[pNew] = backgroundColor;
                    pNew += source.fullLine;
                }
            }
        }
    }
}
