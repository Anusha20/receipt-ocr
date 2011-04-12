package net.sourceforge.javaocr.scanner;

public class FoundChar {
    private final int width;
    private final int height;
    private final int rowTop;
    private final int rowBottom;
    private final byte pixels[];

    public FoundChar(PixelImage pixelImage, int x1, int y1, int x2, int y2, int rowY1, int rowY2) {
        this.width = x2 - x1 + 1;
        this.height = y2 - y1 + 1;
        this.rowTop = rowY1 - y1;
        this.rowBottom = rowY2 - y1;

        pixels = new byte[width * height];
        int ourIndex = 0;
        for (int y = y1; y <= y2; y++) {
            int rowIndex = y * pixelImage.width + x1;
            for (int x = x1; x <= x2; x++) {
                pixels[ourIndex] = (byte) ((pixelImage.pixels[rowIndex] & 255) - 128);
                ourIndex++;
                rowIndex++;
            }
        }
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
}
