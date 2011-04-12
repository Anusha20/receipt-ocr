package net.sourceforge.javaocr.scanner;

import java.util.ArrayList;
import java.util.List;

public class FoundWord {
    private final List<RecognizedChar> recognizedChars;

    private String recognizedString = null;

    public FoundWord() {
        recognizedChars = new ArrayList<RecognizedChar>();
    }

    public void addRecognizedChar(RecognizedChar recognizedChar) {
        recognizedChars.add(recognizedChar);
        recognizedString = null;
    }

    public int getSize() {
        return recognizedChars.size();
    }

    public RecognizedChar getRecognizedChar(int index) {
        return recognizedChars.get(index);
    }

    public String getRecognizedString() {
        if (recognizedString == null) {
            StringBuilder builder = new StringBuilder(recognizedChars.size());
            for (RecognizedChar c : recognizedChars) {
                if (c != null) {
                    builder.append(c.getRecognizedChar());
                }
            }
            recognizedString = builder.toString();
        }
        return recognizedString;
    }

    public String toString() {
        return getRecognizedString();
    }
}
