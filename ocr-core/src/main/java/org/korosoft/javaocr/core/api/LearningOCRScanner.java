package org.korosoft.javaocr.core.api;

import org.korosoft.javaocr.core.MutableImage;

import java.util.List;

/**
 * Learning OCR lifecycle definition. Main entry point if {@link #scan(org.korosoft.javaocr.core.MutableImage, org.korosoft.javaocr.core.api.LearningOCRScanner.Callback)} method.
 * All further interactions are callback-based.
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public interface LearningOCRScanner {
    /**
     * Scanner lifecycle entry point. Call this method to start image OCR recognition.
     * <p/>
     * This method may block until recognition is complete or be executes asynchronously depending on scanner implementation.
     *
     * @param image    Image to recognize.
     * @param callback Your scanner callback implementation.
     * @see org.korosoft.javaocr.core.api.LearningOCRScanner.Callback
     */
    void scan(MutableImage image, Callback callback);

    /**
     * Implement this interface to be able to invoke {@link LearningOCRScanner#scan(org.korosoft.javaocr.core.MutableImage, org.korosoft.javaocr.core.api.LearningOCRScanner.Callback)} method.
     * <p/>
     * Structure of this interface and its methods enforces learning OCR scanner
     *
     * @author Dmitry Korotkov
     * @see LearningOCRScanner#scan(org.korosoft.javaocr.core.MutableImage, org.korosoft.javaocr.core.api.LearningOCRScanner.Callback)
     * @since 1.0
     */
    public interface Callback {
        /**
         * This method is invoked when user/your system is required to pick which symbols should be recognized.
         *
         * @param detectedWords contains all symbols found in the image. First level of lists is lines, second level is words in line and the third level is particular symbols.
         * @param callback      Call this callback to select symbols to be recognized. The format of callback argument is the same as for {@code detectedWords} parameter.
         *                      Callback may be called asynchronously, but it should be invoked exactly once. Pass {@code null} to cancel the scan.
         *                      <p/>
         *                      You are free to reuse {@code detectedWords} lists as callback arguments.
         */
        void pickWordsToRecognize(List<List<List<DetectedSymbol>>> detectedWords, SimpleCallback<List<List<List<DetectedSymbol>>>> callback);

        /**
         * This method is invokes when user/your system is required to confirm recognition result.
         *
         * @param recognizedWords contains all recognized symbols as the were passed to {@link #pickWordsToRecognize(java.util.List, SimpleCallback)} method.
         *                        If a symbol is not recognized, {@link RecognizedSymbol#referenceImage} will be {@code null}.
         * @param callback        Call this callback to provide 'correct answer' to the system which is learning recognition at each scan.
         *                        Provided strings should exactly match {@code recognizedWords} structure. Pass {@code null} to cancel the scan.
         */
        void confirmRecognitionResult(List<List<List<RecognizedSymbol>>> recognizedWords, SimpleCallback<List<List<String>>> callback);

        /**
         * This method is invoked when the recognition is finally complete.
         *
         * @param recognizedWords Recognition result.
         */
        void onRecognitionComplete(List<List<String>> recognizedWords);
    }
}
