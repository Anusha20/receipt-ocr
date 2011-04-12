package net.sourceforge.javaocr.scanner;

import java.io.Serializable;

public class TrainingChar implements Serializable {
    final int width;
    final int height;
    final int rowTop;
    final int rowBottom;
    final byte[] pixels;
    final char value;

    int positiveScore;
    int negativeScore;

    public TrainingChar(TrainingChar source, char newValue) {
        this.width = source.width;
        this.height = source.height;
        this.rowTop = source.rowTop;
        this.rowBottom = source.rowBottom;
        this.pixels = source.pixels;
        this.value = newValue;
    }

    public TrainingChar(PixelImage pixelImage, int x1, int y1, int x2, int y2, int rowY1, int rowY2, char value) {
        this.value = value;

        width = x2 - x1 + 1;
        height = y2 - y1 + 1;
        rowTop = rowY1 - y1;
        rowBottom = rowY2 - y2;

        int ourIndex = 0;
        pixels = new byte[width * height];
        for (int y = y1; y <= y2; y++) {
            int theirIndex = y * pixelImage.width + x1;
            for (int x = x1; x <= x2; x++) {
                pixels[ourIndex] = (byte) ((pixelImage.pixels[theirIndex] & 255) - 128);
                ourIndex++;
                theirIndex++;
            }
        }
    }

    public int increasePositiveScore() {
        return ++positiveScore;
    }

    public int increaseNegativeScore() {
        return ++negativeScore;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRowTop() {
        return rowTop;
    }

    public int getRowBottom() {
        return rowBottom;
    }

    public byte[] getPixels() {
        return pixels;
    }

    public int getPositiveScore() {
        return positiveScore;
    }

    public int getNegativeScore() {
        return negativeScore;
    }
}
