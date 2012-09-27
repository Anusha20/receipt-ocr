package org.korosoft.javaocr.core.impl;

import org.korosoft.javaocr.core.MutableImage;
import org.korosoft.javaocr.core.api.ReferenceImage;
import org.korosoft.javaocr.core.api.ReferenceImageStorage;

import java.util.*;

/**
 * Trivial implementation of {@link ReferenceImageStorage}
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class SimpleReferenceImageStorage implements ReferenceImageStorage {
    List<ReferenceImage> images = new ArrayList<ReferenceImage>();
//    Set<ReferenceImage> images = new TreeSet<ReferenceImage>(new Comparator<ReferenceImage>() {
//        public int compare(ReferenceImage o1, ReferenceImage o2) {
//            double x1 = (double) o1.getCorrectDetections() / (double) o1.getAllDetections();
//            double x2 = (double) o2.getCorrectDetections() / (double) o2.getAllDetections();
//            return x1 < x2 ? 1 : x1 == x2 ? 0 : -1;
//        }
//    });

    public List<ReferenceImage> getAllReferenceImages() {
        return new ArrayList<ReferenceImage>(images);
    }

    public ReferenceImage addReferenceImage(MutableImage image, String symbol, int baseLine) {
        ReferenceImage r = new ReferenceImageImpl(image, symbol, baseLine, 1, 1);
        images.add(r);
        return r;
    }

    private class ReferenceImageImpl implements ReferenceImage {
        private final MutableImage image;
        private final String symbol;
        private final int baseLine;
        private int correctDetections;
        private int allDetections;

        private ReferenceImageImpl(MutableImage image, String symbol, int baseLine, int correctDetections, int allDetections) {
            this.image = image;
            this.symbol = symbol;
            this.correctDetections = correctDetections;
            this.allDetections = allDetections;
            this.baseLine = baseLine;
        }

        public String getSymbol() {
            return symbol;
        }

        public MutableImage getImage() {
            return image;
        }

        public int getBaseLine() {
            return baseLine;
        }

        public int getCorrectDetections() {
            return correctDetections;
        }

        public int getAllDetections() {
            return allDetections;
        }

        public void registerDetection(boolean correct) {
            if (correct) {
                correctDetections++;
            }
            allDetections++;
        }
    }
}
