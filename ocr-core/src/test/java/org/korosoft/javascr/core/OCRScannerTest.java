package org.korosoft.javascr.core;

import junit.framework.Assert;
import org.junit.Test;
import org.korosoft.javaocr.core.ImgUtil;
import org.korosoft.javaocr.core.MSEUtil;
import org.korosoft.javaocr.core.MutableImage;
import org.korosoft.javaocr.core.OCRScanner;
import org.korosoft.javaocr.core.api.ImageComparator;
import org.korosoft.javaocr.core.api.ImageScanner;
import org.korosoft.javaocr.core.api.ReferenceImageStorage;
import org.korosoft.javaocr.core.impl.SimpleImageScanner;
import org.korosoft.javaocr.core.impl.SimpleReferenceImageStorage;

import java.util.List;

/**
 * OCR scanner test
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class OCRScannerTest {
    @Test
    public void testScan() throws Exception {
        ImageScanner imageScanner = new SimpleImageScanner();
        ReferenceImageStorage referenceImageStorage = new SimpleReferenceImageStorage();
        ImageComparator imageComparator = new ImageComparator() {
            public double compareImages(MutableImage image1, int baseLine1, MutableImage image2, int baseLine2) {
                MSEUtil.MSEResult result = MSEUtil.compareSlow(image1, image2, baseLine1, baseLine2, 255);
                return Math.sqrt(result.squareError / result.numPixels);
            }
        };
        OCRScanner.Settings settings = new OCRScanner.Settings(0.5, 0.2, 0.9);

        OCRScanner scanner = new OCRScanner(imageScanner, referenceImageStorage, imageComparator, settings);

        MutableImage image = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/test.png"));

        List<List<List<OCRScanner.RecognizedSymbol>>> scan = scanner.scan(image);
        Assert.assertEquals("Number of lines", 4, scan.size());
        Assert.assertEquals("Number of words in 1st line", 5, scan.get(0).size());
        Assert.assertEquals("Number of symbols in 3rd word in 1st line", 4, scan.get(0).get(2).size());

        OCRScanner.RecognizedSymbol letter_t = scan.get(0).get(2).get(3);
        referenceImageStorage.addReferenceImage(letter_t.image, 't', letter_t.baseLine);
        List<List<List<OCRScanner.RecognizedSymbol>>> scan2 = scanner.scan(image);
        int t_count = 0;
        for (List<List<OCRScanner.RecognizedSymbol>> line : scan2) {
            for (List<OCRScanner.RecognizedSymbol> word : line) {
                for (OCRScanner.RecognizedSymbol symbol : word) {
                    if (symbol.symbol != null && symbol.symbol.equals('t')) {
                        t_count++;
                    }
                }
            }
        }
        Assert.assertEquals("Number of 't' symbols on the image", 6, t_count);
    }

    @Test
    public void testBaseLineScan() throws Exception {
        ImageScanner imageScanner = new SimpleImageScanner();
        ReferenceImageStorage referenceImageStorage = new SimpleReferenceImageStorage();
        ImageComparator imageComparator = new ImageComparator() {
            public double compareImages(MutableImage image1, int baseLine1, MutableImage image2, int baseLine2) {
                MSEUtil.MSEResult result = MSEUtil.compareSlow(image1, image2, baseLine1, baseLine2, 255);
                return Math.sqrt(result.squareError / result.numPixels);
            }
        };
        OCRScanner.Settings settings = new OCRScanner.Settings(0.5, 0.2, 0.9);

        OCRScanner scanner = new OCRScanner(imageScanner, referenceImageStorage, imageComparator, settings);

        MutableImage image = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/baseline-test.png"));

        List<List<List<OCRScanner.RecognizedSymbol>>> scan = scanner.scan(image);
        Assert.assertEquals("Number of lines", 4, scan.size());
        Assert.assertEquals("Number of words in 1st line", 5, scan.get(0).size());
        Assert.assertEquals("Number of symbols in 3rd word in 1st line", 4, scan.get(0).get(2).size());

        OCRScanner.RecognizedSymbol letter_t = scan.get(0).get(2).get(3);
        referenceImageStorage.addReferenceImage(letter_t.image, 't', letter_t.baseLine);
        List<List<List<OCRScanner.RecognizedSymbol>>> scan2 = scanner.scan(image);
        int t_count = 0;
        for (List<List<OCRScanner.RecognizedSymbol>> line : scan2) {
            for (List<OCRScanner.RecognizedSymbol> word : line) {
                for (OCRScanner.RecognizedSymbol symbol : word) {
                    if (symbol.symbol != null && symbol.symbol.equals('t')) {
                        t_count++;
                    }
                }
            }
        }
        Assert.assertEquals("Number of 't' symbols on the image", 6, t_count);
    }
}
