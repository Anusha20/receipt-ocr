package org.korosoft.javaocr.core;

import org.korosoft.javaocr.core.api.ImageComparator;
import org.korosoft.javaocr.core.api.ImageScanner;
import org.korosoft.javaocr.core.api.ReferenceImage;
import org.korosoft.javaocr.core.api.ReferenceImageStorage;

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
    private final Settings settings;

    public OCRScanner(ImageScanner imageScanner, ReferenceImageStorage referenceImageStorage, ImageComparator imageComparator, Settings settings) {
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

            public void onNewSymbol(MutableImage symbol, int x, int y) {
                currentWord.add(processIncomingSymbol(symbol, x, y));
            }

            private RecognizedSymbol processIncomingSymbol(MutableImage symbol, int x, int y) {
                double bestMatchScore = 0;
                ReferenceImage bestMatchImage = null;
                for (ReferenceImage referenceImage : allReferenceImages) {
                    double imageScore = imageComparator.compareImages(referenceImage.getImage(), symbol);
                    if (imageScore < settings.symbolRecognitionThreshold) {
                        bestMatchScore = imageScore;
                        bestMatchImage = referenceImage;
                        if (bestMatchScore > settings.exactMatchThreshold) {
                            break;
                        }
                    }
                }
                return new RecognizedSymbol(symbol, x, y, bestMatchImage == null ? null : bestMatchImage.getSymbol(), bestMatchScore);
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

    public static class RecognizedSymbol {
        public final MutableImage image;
        public final int x;
        public final int y;
        public final Character symbol;
        public final double score;

        private RecognizedSymbol(MutableImage image, int x, int y, Character symbol, double score) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.symbol = symbol;
            this.score = score;
        }
    }

    public static class Settings {
        public final double symbolRecognitionThreshold;
        public final double whiteSpaceThreshold;
        public final double exactMatchThreshold;

        public Settings(double symbolRecognitionThreshold, double whiteSpaceThreshold, double exactMatchThreshold) {
            this.symbolRecognitionThreshold = symbolRecognitionThreshold;
            this.whiteSpaceThreshold = whiteSpaceThreshold;
            this.exactMatchThreshold = exactMatchThreshold;
        }
    }
}
