package net.sourceforge.javaocr.scanner;

import java.util.ArrayList;
import java.util.List;

public class FoundWord {
    private final List<FoundChar> chars;
    private final List<RecognizedChar> recognizedChars;

    private String recognizedString = null;

    public FoundWord() {
        chars = new ArrayList<FoundChar>();
        recognizedChars = new ArrayList<RecognizedChar>();
    }

    public void addUnrecognizedChar(FoundChar foundChar) {
        chars.add(foundChar);
        recognizedChars.add(null);
        recognizedString = null;
    }

    public void recognizeChar(int index, RecognizedChar recognizedChar) {
        recognizedChars.set(index, recognizedChar);
        recognizedString = null;
    }

    public int getSize() {
        return recognizedChars.size();
    }

    public FoundChar getFoundChar(int index) {
        return chars.get(index);
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
}
