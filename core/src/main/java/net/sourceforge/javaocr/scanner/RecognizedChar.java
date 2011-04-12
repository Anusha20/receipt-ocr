package net.sourceforge.javaocr.scanner;

public class RecognizedChar {
    final char recognizedChar;
    final TrainingImage trainingImage;
    final FoundChar foundChar;

    public RecognizedChar(char recognizedChar, TrainingImage trainingImage, FoundChar foundChar) {
        this.recognizedChar = recognizedChar;
        this.trainingImage = trainingImage;
        this.foundChar = foundChar;
    }

    public char getRecognizedChar() {
        return recognizedChar;
    }

    public TrainingImage getTrainingImage() {
        return trainingImage;
    }

    public FoundChar getFoundChar() {
        return foundChar;
    }
}
