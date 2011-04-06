package net.sourceforge.javaocr.gui;

import javax.swing.*;

public final class JavaOCRUtil {

    private static JFileChooser fileChooser;

    public static JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        return fileChooser;
    }

    private JavaOCRUtil() {
    }
}
