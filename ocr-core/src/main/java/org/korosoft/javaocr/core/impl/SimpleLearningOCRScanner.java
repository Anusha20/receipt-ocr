package org.korosoft.javaocr.core.impl;

import org.korosoft.javaocr.core.MutableImage;
import org.korosoft.javaocr.core.api.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleLearningOCRScanner implements LearningOCRScanner {
    final ImageScanner imageScanner;
    final ReferenceImageStorage referenceImageStorage;
    final ImageComparator imageComparator;
    final RecognitionSettings recognitionSettings;

    public SimpleLearningOCRScanner(ImageScanner imageScanner, ReferenceImageStorage referenceImageStorage, ImageComparator imageComparator, RecognitionSettings recognitionSettings) {
        this.imageScanner = imageScanner;
        this.referenceImageStorage = referenceImageStorage;
        this.imageComparator = imageComparator;
        this.recognitionSettings = recognitionSettings;
    }

    public void scan(MutableImage image, final Callback callback) {
        final List<List<List<DetectedSymbol>>> detectedLines = new ArrayList<List<List<DetectedSymbol>>>();

        imageScanner.scan(image, new ImageScanner.Callback() {
            List<List<DetectedSymbol>> currentLine = null;
            List<DetectedSymbol> currentWord = null;

            public void onNewLine() {
                processEndOfWord();
                processEndOfLine();
            }

            private void processEndOfLine() {
                if (currentLine != null && !currentLine.isEmpty()) {
                    detectedLines.add(currentLine);
                    currentLine = null;
                }
            }

            public void onNewSymbol(MutableImage symbol, int x, int y, int baseLine) {
                if (currentWord == null) {
                    currentWord = new ArrayList<DetectedSymbol>();
                }
                currentWord.add(new DetectedSymbol(symbol, x, y, baseLine));
            }

            public void onWhitespace(int lineHeight, int width) {
                if (currentWord.size() > 0 && (double) width / (double) lineHeight > recognitionSettings.whiteSpaceThreshold) {
                    processEndOfWord();
                }
            }

            public void onFinished() {
                processEndOfWord();
                processEndOfLine();
            }

            private void processEndOfWord() {
                if (currentWord != null && !currentWord.isEmpty()) {
                    if (currentLine == null) {
                        currentLine = new ArrayList<List<DetectedSymbol>>();
                    }
                    currentLine.add(currentWord);
                    currentWord = null;
                }
            }
        });

        final AtomicBoolean callbackCalled = new AtomicBoolean(false);
        callback.pickWordsToRecognize(detectedLines, new SimpleCallback<List<List<List<DetectedSymbol>>>>() {
            public void call(List<List<List<DetectedSymbol>>> linesToRecognize) {
                if (callbackCalled.getAndSet(true)) {
                    throw new IllegalStateException("Callback of pickWordsToRecognize was already called");
                }
                SimpleLearningOCRScanner.this.recognize(linesToRecognize, callback);
            }
        });
    }


    private void recognize(List<List<List<DetectedSymbol>>> linesToRecognize, final Callback callback) {
        if (linesToRecognize == null) {
            return;
        }

        final List<List<List<RecognizedSymbol>>> recognizedLines = new ArrayList<List<List<RecognizedSymbol>>>();
        List<ReferenceImage> allReferenceImages = referenceImageStorage.getAllReferenceImages();

        for (List<List<DetectedSymbol>> line : linesToRecognize) {
            List<List<RecognizedSymbol>> recognizedLine = new ArrayList<List<RecognizedSymbol>>();
            for (List<DetectedSymbol> word : line) {
                List<RecognizedSymbol> recognizedWord = new ArrayList<RecognizedSymbol>();
                for (DetectedSymbol detectedSymbol : word) {
                    double bestMatchScore = 0;
                    ReferenceImage bestMatchImage = null;
                    for (ReferenceImage referenceImage : allReferenceImages) {
                        double imageScore = imageComparator.compareImages(referenceImage.getImage(), referenceImage.getBaseLine(), detectedSymbol.image, detectedSymbol.baseLine);
                        if (imageScore < recognitionSettings.symbolRecognitionThreshold) {
                            bestMatchScore = imageScore;
                            bestMatchImage = referenceImage;
                            if (bestMatchScore > recognitionSettings.exactMatchThreshold) {
                                break;
                            }
                        }
                    }
                    recognizedWord.add(new RecognizedSymbol(detectedSymbol.image, detectedSymbol.x, detectedSymbol.y, detectedSymbol.baseLine, bestMatchImage, bestMatchScore));
                }
                recognizedLine.add(recognizedWord);
            }
            recognizedLines.add(recognizedLine);
        }
        final AtomicBoolean callbackCalled = new AtomicBoolean(false);
        callback.confirmRecognitionResult(recognizedLines, new SimpleCallback<List<List<String>>>() {
            public void call(List<List<String>> correctAnswer) {
                if (callbackCalled.getAndSet(true)) {
                    throw new IllegalStateException("Callback of pickWordsToRecognize was already called");
                }
                SimpleLearningOCRScanner.this.handleCorrectAnswer(recognizedLines, correctAnswer, callback);
            }
        });
    }

    private void handleCorrectAnswer(List<List<List<RecognizedSymbol>>> recognizedLines, List<List<String>> correctAnswer, Callback callback) {
        if (correctAnswer == null) {
            return;
        }

        // Validate first
        if (recognizedLines.size() != correctAnswer.size()) {
            throw new IllegalArgumentException(String.format("Number of recognized lines (%d) differs from correct answer lines (%d)", recognizedLines.size(), correctAnswer.size()));
        }

        {
            final Iterator<List<List<RecognizedSymbol>>> recognizedLineI = recognizedLines.iterator();
            final Iterator<List<String>> correctLineI = correctAnswer.iterator();
            int l = 1;
            while (recognizedLineI.hasNext()) {
                List<List<RecognizedSymbol>> line = recognizedLineI.next();
                List<String> correctLine = correctLineI.next();
                if (line.size() != correctLine.size()) {
                    throw new IllegalArgumentException(String.format("Number of recognized words in line %d (%d) differs from correct answer words for the same line (%d)", l, line.size(), correctLine.size()));
                }
                l++;
            }
        }

        {
            final Iterator<List<List<RecognizedSymbol>>> recognizedLineI = recognizedLines.iterator();
            final Iterator<List<String>> correctLineI = correctAnswer.iterator();
            while (recognizedLineI.hasNext()) {
                List<List<RecognizedSymbol>> line = recognizedLineI.next();
                List<String> correctLine = correctLineI.next();
                final Iterator<List<RecognizedSymbol>> recognizedLineIterator = line.iterator();
                final Iterator<String> correctLineIterator = correctLine.iterator();
                while (recognizedLineIterator.hasNext()) {
                    List<RecognizedSymbol> word = recognizedLineIterator.next();
                    String correctWord = correctLineIterator.next();

                    if (correctWord == null) {
                        continue;
                    }

                    if (word.size() == correctWord.length()) {
                        int i = 0;
                        char chars[] = correctWord.toCharArray();
                        for (RecognizedSymbol recognizedSymbol : word) {
                            provideCorrectAnswer(recognizedSymbol, String.valueOf(chars[i]));
                            i++;
                        }
                    }
                    // ToDo: support having more that one character per recognized symbol
                }
            }
        }

        callback.onRecognitionComplete(correctAnswer);
    }

    private void provideCorrectAnswer(RecognizedSymbol recognizedSymbol, String correctAnswer) {
        if (recognizedSymbol.referenceImage == null) {
            referenceImageStorage.addReferenceImage(recognizedSymbol.image, correctAnswer, recognizedSymbol.baseLine);
            return;
        }
        recognizedSymbol.referenceImage.registerDetection(correctAnswer.equals(recognizedSymbol.referenceImage.getSymbol()));
    }
}
