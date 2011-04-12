package net.sourceforge.javaocr.scanner;

public class FoundChar {
    private final PixelImage image;
    final int x1;
    final int y1;
    final int x2;
    final int y2;
    final int rowY1;
    final int rowY2;

    public FoundChar(PixelImage image, int x1, int y1, int x2, int y2, int rowY1, int rowY2) {
        this.image = image;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.rowY1 = rowY1;
        this.rowY2 = rowY2;
    }

    public PixelImage getImage() {
        return image;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public int getRowY1() {
        return rowY1;
    }

    public int getRowY2() {
        return rowY2;
    }
}