package net.sourceforge.javaocr.scanner;

public class RecognizedChar {
    final char recognizedChar;
    final TrainingChar trainingChar;

    public RecognizedChar(char recognizedChar, TrainingChar trainingChar) {
        this.recognizedChar = recognizedChar;
        this.trainingChar = trainingChar;
    }

    public char getRecognizedChar() {
        return recognizedChar;
    }

    public TrainingChar getTrainingChar() {
        return trainingChar;
    }
}
