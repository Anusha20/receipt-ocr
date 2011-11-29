package net.sourceforge.javaocr.ocr;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Utility class to load JPEG images with EXIF orientation field support.
 *
 * @author Dmitry Korotkov
 */
public abstract class ImageReader {

    public static BufferedImage read(File file) throws IOException {
        return read(file, Integer.MAX_VALUE);
    }

    public static BufferedImage read(File file, int maxDimension) throws IOException {
        BufferedImage bfImage = ImageIO.read(file);
        int orientation = 1;
        try {
            IImageMetadata metadata = Sanselan.getMetadata(file);
            ArrayList<ImageMetadata.Item> metadataItems = metadata.getItems();
            orientation = 1;
            for (ImageMetadata.Item item : metadataItems) {
                if (item.getKeyword().equals("Orientation")) {
                    orientation = Integer.parseInt(item.getText());
                }
            }
        } catch (ImageReadException ignored) {
        }

        //Zoom

        double maxDim = maxDimension;
        int dim = Math.max(bfImage.getWidth(), bfImage.getHeight());
        if (dim > maxDim) {
            double aspect = maxDim / dim;
            BufferedImage scaledImage = new BufferedImage((int) (bfImage.getWidth() * aspect), (int) (bfImage.getHeight() * aspect), BufferedImage.TYPE_INT_RGB);
            AffineTransform tx = new AffineTransform();
            tx.scale(aspect, aspect);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);
            Graphics2D g = scaledImage.createGraphics();
            g.drawImage(bfImage, tx, null);
            g.dispose();
            bfImage = scaledImage;
        }

        if (orientation == 1) {
        } else if (orientation == 3) { // Rotate 180
            BufferedImage rotatedImage = new BufferedImage(bfImage.getWidth(), bfImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            AffineTransform tx = new AffineTransform();
            tx.translate(bfImage.getWidth(), bfImage.getHeight());
            tx.quadrantRotate(2);
            Graphics2D g = rotatedImage.createGraphics();
            g.drawImage(bfImage, tx, null);
            g.dispose();
            bfImage = rotatedImage;
        } else if (orientation == 6) { // Rotate CW
            BufferedImage rotatedImage = new BufferedImage(bfImage.getHeight(), bfImage.getWidth(), BufferedImage.TYPE_INT_RGB);
            AffineTransform tx = new AffineTransform();
            tx.translate(bfImage.getHeight(), 0);
            tx.quadrantRotate(1);
            Graphics2D g = rotatedImage.createGraphics();
            g.drawImage(bfImage, tx, null);
            g.dispose();
            bfImage = rotatedImage;
        } else if (orientation == 8) { // Rotate CCW
            BufferedImage rotatedImage = new BufferedImage(bfImage.getHeight(), bfImage.getWidth(), BufferedImage.TYPE_INT_RGB);
            AffineTransform tx = new AffineTransform();
            tx.translate(0, bfImage.getWidth());
            tx.quadrantRotate(-1);
            Graphics2D g = rotatedImage.createGraphics();
            g.drawImage(bfImage, tx, null);
            g.dispose();
            bfImage = rotatedImage;
        } else {
            throw new RuntimeException("Flipped orientations are not supported");
        }
        return bfImage;
    }

    /**
     * Private constructor prevents instantiation
     */
    private ImageReader() {

    }
}
