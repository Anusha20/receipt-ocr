package org.korosoft.javascr.core.impl;

import junit.framework.Assert;
import org.junit.Test;
import org.korosoft.javaocr.core.ImgUtil;
import org.korosoft.javaocr.core.MSEUtil;
import org.korosoft.javaocr.core.MutableImage;
import org.korosoft.javaocr.core.api.*;
import org.korosoft.javaocr.core.impl.SimpleImageScanner;
import org.korosoft.javaocr.core.impl.SimpleLearningOCRScanner;
import org.korosoft.javaocr.core.impl.SimpleReferenceImageStorage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LearningOCRScannerTest {
    @Test
    public void testFirstPass() throws Exception {
        MutableImage image = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/test.png"));
        ImageScanner imageScanner = new SimpleImageScanner();
        ReferenceImageStorage referenceImageStorage = new SimpleReferenceImageStorage();
        ImageComparator imageComparator = new ImageComparator() {
            public double compareImages(MutableImage image1, int baseLine1, MutableImage image2, int baseLine2) {
                MSEUtil.MSEResult result = MSEUtil.compareSlow(image1, image2, baseLine1, baseLine2, 255);
                return Math.sqrt(result.squareError / result.numPixels);
            }
        };
        RecognitionSettings settings = new RecognitionSettings(40, 0.2, 10);

        LearningOCRScanner scanner = new SimpleLearningOCRScanner(imageScanner, referenceImageStorage, imageComparator, settings);

        final AtomicBoolean pickWordsToRecognizeExecuted = new AtomicBoolean();
        final AtomicBoolean confirmRecognitionResultExecuted = new AtomicBoolean();
        final AtomicBoolean onRecognitionCompleteExecuted = new AtomicBoolean();
        scanner.scan(image, new LearningOCRScanner.Callback() {
            public void pickWordsToRecognize(List<List<List<DetectedSymbol>>> detectedWords, SimpleCallback<List<List<List<DetectedSymbol>>>> callback) {
                Assert.assertEquals("Number of lines", 4, detectedWords.size());
                Assert.assertEquals("Number of words in 1st line", 5, detectedWords.get(0).size());
                Assert.assertEquals("Number of symbols in 2rd word in 1st line", 4, detectedWords.get(0).get(2).size());
                Assert.assertEquals("Number of symbols in 3rd word in 1st line", 4, detectedWords.get(0).get(3).size());

                //noinspection unchecked
                pickWordsToRecognizeExecuted.set(true);
                //noinspection unchecked
                callback.call(Collections.singletonList(Arrays.asList(detectedWords.get(0).get(2), detectedWords.get(0).get(3))));
            }

            public void confirmRecognitionResult(List<List<List<RecognizedSymbol>>> recognizedWords, SimpleCallback<List<List<String>>> callback) {
                confirmRecognitionResultExecuted.set(true);
                callback.call(Collections.singletonList(Arrays.asList("unit", "test")));
            }

            public void onRecognitionComplete(List<List<String>> recognizedWords) {
                onRecognitionCompleteExecuted.set(true);
            }
        });

        Assert.assertTrue("pickWordsToRecognize executed", pickWordsToRecognizeExecuted.get());
        Assert.assertTrue("confirmRecognitionResult executed", confirmRecognitionResultExecuted.get());
        Assert.assertTrue("onRecognitionComplete executed", onRecognitionCompleteExecuted.get());
    }

    @Test
    public void testSecondPass() throws Exception {
        MutableImage image = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/test.png"));
        ImageScanner imageScanner = new SimpleImageScanner();
        ReferenceImageStorage referenceImageStorage = new SimpleReferenceImageStorage();
        ImageComparator imageComparator = new ImageComparator() {
            public double compareImages(MutableImage image1, int baseLine1, MutableImage image2, int baseLine2) {
                MSEUtil.MSEResult result = MSEUtil.compareSlow(image1, image2, baseLine1, baseLine2, 255);
                return Math.sqrt(result.squareError / result.numPixels);
            }
        };
        RecognitionSettings settings = new RecognitionSettings(40, 0.2, 10);

        LearningOCRScanner scanner = new SimpleLearningOCRScanner(imageScanner, referenceImageStorage, imageComparator, settings);

        final AtomicBoolean pickWordsToRecognizeExecuted = new AtomicBoolean();
        final AtomicBoolean confirmRecognitionResultExecuted = new AtomicBoolean();
        final AtomicBoolean onRecognitionCompleteExecuted = new AtomicBoolean();
        scanner.scan(image, new LearningOCRScanner.Callback() {
            public void pickWordsToRecognize(List<List<List<DetectedSymbol>>> detectedWords, SimpleCallback<List<List<List<DetectedSymbol>>>> callback) {
                Assert.assertEquals("Number of lines", 4, detectedWords.size());
                Assert.assertEquals("Number of words in 1st line", 5, detectedWords.get(0).size());
                Assert.assertEquals("Number of symbols in 2rd word in 1st line", 4, detectedWords.get(0).get(2).size());
                Assert.assertEquals("Number of symbols in 3rd word in 1st line", 4, detectedWords.get(0).get(3).size());

                //noinspection unchecked
                pickWordsToRecognizeExecuted.set(true);
                //noinspection unchecked
                callback.call(Collections.singletonList(Arrays.asList(detectedWords.get(0).get(2), detectedWords.get(0).get(3))));
            }

            public void confirmRecognitionResult(List<List<List<RecognizedSymbol>>> recognizedWords, SimpleCallback<List<List<String>>> callback) {
                confirmRecognitionResultExecuted.set(true);
                callback.call(Collections.singletonList(Arrays.asList("unit", "test")));
            }

            public void onRecognitionComplete(List<List<String>> recognizedWords) {
                onRecognitionCompleteExecuted.set(true);
            }
        });

        Assert.assertTrue("pickWordsToRecognize executed", pickWordsToRecognizeExecuted.get());
        Assert.assertTrue("confirmRecognitionResult executed", confirmRecognitionResultExecuted.get());
        Assert.assertTrue("onRecognitionComplete executed", onRecognitionCompleteExecuted.get());

        final AtomicReference<String> recognizedText = new AtomicReference<String>();
        // Scan second pass
        scanner.scan(image, new LearningOCRScanner.Callback() {
            public void pickWordsToRecognize(List<List<List<DetectedSymbol>>> detectedWords, SimpleCallback<List<List<List<DetectedSymbol>>>> callback) {
                Assert.assertEquals("Number of lines", 4, detectedWords.size());
                Assert.assertEquals("Number of words in 1st line", 5, detectedWords.get(0).size());
                Assert.assertEquals("Number of symbols in 2rd word in 1st line", 4, detectedWords.get(0).get(2).size());
                Assert.assertEquals("Number of symbols in 3rd word in 1st line", 4, detectedWords.get(0).get(3).size());

                //noinspection unchecked
                pickWordsToRecognizeExecuted.set(true);
                //noinspection unchecked
                callback.call(detectedWords);
            }

            public void confirmRecognitionResult(List<List<List<RecognizedSymbol>>> recognizedWords, SimpleCallback<List<List<String>>> callback) {
                StringBuilder builder = new StringBuilder();
                boolean firstLine = true;
                for (List<List<RecognizedSymbol>> line : recognizedWords) {
                    if (firstLine) {
                        firstLine = false;
                    } else {
                        builder.append("\n");
                    }
                    boolean firstWord = true;
                    for (List<RecognizedSymbol> word : line) {
                        if (firstWord) {
                            firstWord = false;
                        } else {
                            builder.append(" ");
                        }
                        for (RecognizedSymbol symbol : word) {
                            builder.append(symbol.referenceImage == null ? " " : symbol.referenceImage.getSymbol());
                        }
                    }
                }
                recognizedText.set(builder.toString());
                confirmRecognitionResultExecuted.set(true);
                callback.call(null);
            }

            public void onRecognitionComplete(List<List<String>> recognizedWords) {
                onRecognitionCompleteExecuted.set(true);
            }
        });

        Assert.assertEquals("Recognized text", "  is is unit test i   e\n     e ei t     si   e\n   u ent s  nne   i      \n  it  ", recognizedText.get());
    }
}
