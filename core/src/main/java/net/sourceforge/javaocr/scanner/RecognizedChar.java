package net.sourceforge.javaocr.scanner;

public class RecognizedChar {
    final char recognizedChar;
    final TrainingImage trainingImage;

    public RecognizedChar(char recognizedChar, TrainingImage trainingImage) {
        this.recognizedChar = recognizedChar;
        this.trainingImage = trainingImage;
    }

    public char getRecognizedChar() {
        return recognizedChar;
    }

    public TrainingImage getTrainingImage() {
        return trainingImage;
    }
}
