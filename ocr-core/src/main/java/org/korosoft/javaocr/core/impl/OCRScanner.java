package org.korosoft.javaocr.core.impl;

import org.korosoft.javaocr.core.MutableImage;
import org.korosoft.javaocr.core.api.*;

import java.util.ArrayList;
import java.util.List;

/**
 * OCR scanner
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class OCRScanner {
    private final ImageScanner imageScanner;
    private final ReferenceImageStorage referenceImageStorage;
    private final ImageComparator imageComparator;
    private final RecognitionSettings settings;

    public OCRScanner(ImageScanner imageScanner, ReferenceImageStorage referenceImageStorage, ImageComparator imageComparator, RecognitionSettings settings) {
        this.imageScanner = imageScanner;
        this.referenceImageStorage = referenceImageStorage;
        this.imageComparator = imageComparator;
        this.settings = settings;
    }

    public List<List<List<RecognizedSymbol>>> scan(MutableImage image) {
        final List<List<List<RecognizedSymbol>>> lines = new ArrayList<List<List<RecognizedSymbol>>>();
        final List<ReferenceImage> allReferenceImages = referenceImageStorage.getAllReferenceImages();
        imageScanner.scan(image, new ImageScanner.Callback() {
            List<List<RecognizedSymbol>> currentLine = new ArrayList<List<RecognizedSymbol>>();
            List<RecognizedSymbol> currentWord = new ArrayList<RecognizedSymbol>();

            public void onNewLine() {
                if (currentWord.size() > 0) {
                    currentLine.add(currentWord);
                    currentWord = new ArrayList<RecognizedSymbol>();
                }
                if (currentLine.size() > 0) {
                    lines.add(currentLine);
                    currentLine = new ArrayList<List<RecognizedSymbol>>();
                }
            }

            public void onNewSymbol(MutableImage symbol, int x, int y, int baseLine) {
                currentWord.add(processIncomingSymbol(symbol, x, y, baseLine));
            }

            private RecognizedSymbol processIncomingSymbol(MutableImage symbol, int x, int y, int baseLine) {
                double bestMatchScore = 0;
                ReferenceImage bestMatchImage = null;
                for (ReferenceImage referenceImage : allReferenceImages) {
                    double imageScore = imageComparator.compareImages(referenceImage.getImage(), referenceImage.getBaseLine(), symbol, baseLine);
                    if (imageScore < settings.symbolRecognitionThreshold) {
                        bestMatchScore = imageScore;
                        bestMatchImage = referenceImage;
                        if (bestMatchScore > settings.exactMatchThreshold) {
                            break;
                        }
                    }
                }
                return new RecognizedSymbol(symbol, x, y, baseLine, bestMatchImage, bestMatchScore);
            }

            public void onWhitespace(int lineHeight, int width) {
                if (currentWord.size() > 0 && (double) width / (double) lineHeight > settings.whiteSpaceThreshold) {
                    currentLine.add(currentWord);
                    currentWord = new ArrayList<RecognizedSymbol>();
                }
            }

            public void onFinished() {
                if (currentWord.size() > 0) {
                    currentLine.add(currentWord);
                }
                if (currentLine.size() > 0) {
                    lines.add(currentLine);
                }
            }
        });
        return lines;
    }

}
