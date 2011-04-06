// ReceiptFinder.java
// Copyright (c) 2011 Dmitry V. Korotkov
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.package net.sourceforge.javaocr.ocrPlugins.imgShearer;
package net.sourceforge.javaocr.ocrPlugins.receiptFinder;

import net.sourceforge.javaocr.scanner.DocumentScanner;
import net.sourceforge.javaocr.scanner.PixelImage;

import java.awt.*;
import java.util.Queue;
import java.util.Random;

/**
 * Utility class to find receipt in the image. All image around the receipt is filled with white.
 * <p/>
 * Algorithm is based on finding white square near the center of the image and filling space around it with white.
 * If receipt is rotated, its top, bottom and sides may be cut a bit.
 *
 * @author Dmitry Korotkov
 */
public class ReceiptFinder {

    private static final int WHITE = 255;
    private static final int BLACK = 0;

    private double centralSquareDimension = 0.3;

    private int attempts = 50;
    private int threshold = 64;

    public double getCentralSquareDimension() {
        return centralSquareDimension;
    }

    public void setCentralSquareDimension(double centralSquareDimension) {
        this.centralSquareDimension = centralSquareDimension;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    private void tryAdd(Queue<Point> queue, PixelImage image, boolean mask[], int x, int y) {
        if (x < 0 || y < 0 || x >= image.width || y >= image.height) {
            return;
        }
        int index = y * image.width + x;
        if (mask[index]) {
            return;
        }
        mask[index] = true;
        if (image.pixels[index] < threshold) {
            return;
        }
        queue.add(new Point(x, y));
    }

    private void fillMask(PixelImage source, int mask[], boolean visitMask[], int x, int y) {
        Queue<Point> pointQueue = new ArrayQueue<Point>(10000, source.width * source.height);
        pointQueue.add(new Point(x, y));

        while (!pointQueue.isEmpty()) {
            Point p = pointQueue.remove();
            int index = p.y * source.width + p.x;
            if (index < 0 || index > source.pixels.length) {
                continue;
            }
            visitMask[index] = true;
            if (source.pixels[index] < threshold) {
                // point is black
                continue;
            }
            mask[index] = WHITE;
            //add neighbours to processing queue
            tryAdd(pointQueue, source, visitMask, p.x + 1, p.y);
            tryAdd(pointQueue, source, visitMask, p.x - 1, p.y);
            tryAdd(pointQueue, source, visitMask, p.x, p.y + 1);
            tryAdd(pointQueue, source, visitMask, p.x, p.y - 1);
            tryAdd(pointQueue, source, visitMask, p.x + 1, p.y + 1);
            tryAdd(pointQueue, source, visitMask, p.x - 1, p.y - 1);
            tryAdd(pointQueue, source, visitMask, p.x + 1, p.y - 1);
            tryAdd(pointQueue, source, visitMask, p.x - 1, p.y + 1);
        }
    }

    public void findReceipt(DocumentScanner documentScanner, PixelImage pixelImage) {
        int mask[] = new int[pixelImage.pixels.length];
        boolean visitMask[] = new boolean[pixelImage.pixels.length];
        int xMin = (int) (pixelImage.width * (1 - centralSquareDimension) / 2);
        int yMin = (int) (pixelImage.height * (1 - centralSquareDimension) / 2);
        int xMax = pixelImage.width - xMin;
        int yMax = pixelImage.height - yMin;
        threshold = documentScanner.getWhiteThreshold();

        final Random random = new Random();
        for (int i = 0; i < attempts; i++) {
            fillMask(pixelImage, mask, visitMask, xMin + random.nextInt(xMax - xMin), yMin + random.nextInt(yMax - yMin));
        }

        // copy mask

        // fill white from sides
        for (int y = 0; y < pixelImage.height; y++) {
            for (int x = 0; x < pixelImage.width && mask[y * pixelImage.width + x] == 0; x++) {
                pixelImage.pixels[y * pixelImage.width + x] = WHITE;
            }
            for (int x = pixelImage.width - 1; x >= 0 && mask[y * pixelImage.width + x] == 0; x--) {
                pixelImage.pixels[y * pixelImage.width + x] = WHITE;
            }
        }
        // fill white from top and bottom
        for (int x = 0; x < pixelImage.width; x++) {
            for (int y = 0; y < pixelImage.height && mask[y * pixelImage.width + x] == 0; y++) {
                pixelImage.pixels[y * pixelImage.width + x] = WHITE;
            }
            for (int y = pixelImage.height - 1; y >= 0 && mask[y * pixelImage.width + x] == 0; y--) {
                pixelImage.pixels[y * pixelImage.width + x] = WHITE;
            }
        }
    }
}
