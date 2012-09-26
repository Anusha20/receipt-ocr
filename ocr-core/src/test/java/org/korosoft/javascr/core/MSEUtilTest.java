package org.korosoft.javascr.core;

import org.junit.Assert;
import org.junit.Test;
import org.korosoft.javaocr.core.ImgUtil;
import org.korosoft.javaocr.core.MSEUtil;
import org.korosoft.javaocr.core.MutableImage;

public class MSEUtilTest {
    @Test
    public void testSelfCompare() throws Exception {
        byte[] pixels = new byte[10000];
        for (int i = 0; i < 100; i++) {
            pixels[i * 100 + i] = (byte) i;
        }
        MutableImage image = new MutableImage(pixels, 0, 0, 100, 100);
        Assert.assertEquals("Square error for fast compare", 0, MSEUtil.compareFast(image, image).squareError);
        Assert.assertEquals("Square error for slow compare", 0, MSEUtil.compareSlow(image, image).squareError);

        MutableImage subImage = image.subImage(10, 10, 80, 80);
        Assert.assertEquals("Square error for fast sub-image compare", 0, MSEUtil.compareFast(subImage, subImage).squareError);
        Assert.assertEquals("Square error for slow sub-image compare", 0, MSEUtil.compareSlow(subImage, subImage).squareError);
    }

    @Test
    public void testSubImageCompare() throws Exception {
        byte[] pixels = new byte[10000];
        for (int i = 0; i < 100; i++) {
            pixels[i * 100 + i] = 100;
        }
        MutableImage master = new MutableImage(pixels, 0, 0, 100, 100);

        MutableImage subImage1 = master.subImage(0, 0, 80, 80);
        MutableImage subImage2 = master.subImage(10, 10, 80, 80);

        // sub-images should contain the same pixels

        Assert.assertEquals("Square error for fast compare", 0, MSEUtil.compareFast(subImage1, subImage2).squareError);
        Assert.assertEquals("Square error for slow compare", 0, MSEUtil.compareSlow(subImage1, subImage2).squareError);
    }

    @Test
    public void testBetterAndWorse() throws Exception {
        byte[] pixels = new byte[10000];
        for (int i = 0; i < 100; i++) {
            pixels[i * 100 + i] = (byte)i;
        }
        MutableImage master = new MutableImage(pixels, 0, 0, 100, 100);

        MutableImage subImage1 = master.subImage(0, 0, 80, 80);
        MutableImage subImage2 = master.subImage(10, 10, 80, 80);
        MutableImage subImage3 = master.subImage(20, 20, 80, 80);

        // sub-images should contain the same pixels

        Assert.assertTrue("Square error for fast compare of different images should correspond the difference", MSEUtil.compareFast(subImage1, subImage2).squareError < MSEUtil.compareFast(subImage1, subImage3).squareError);
        Assert.assertTrue("Square error for slow compare of different images should correspond the difference", MSEUtil.compareSlow(subImage1, subImage2).squareError < MSEUtil.compareSlow(subImage1, subImage3).squareError);
    }

    @Test
    public void testBaseLineSelfCompare() throws Exception {
        MutableImage i1 = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/a_33.png"));
        MutableImage i2 = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/a_33.png"));
        Assert.assertEquals("Square error for self same baseline compare", 0, MSEUtil.compareSlow(i1, i2, 33, 33, 0xff).squareError);
        Assert.assertTrue("Square error for self shifted baseline compare", MSEUtil.compareSlow(i1, i2, 33, 34, 0xff).squareError != 0);
    }

    @Test
    public void testBaseLineEqualCompare() throws Exception {
        MutableImage i1 = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/a_33.png"));
        MutableImage i2 = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/a_22.png"));
        Assert.assertEquals("Square error for baseline compare of matching images", 0, MSEUtil.compareSlow(i1, i2, 33, 22, 0xff).squareError);
    }

    @Test
    public void testBaseLineZoomCompare() throws Exception {
        MutableImage i1 = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/a_33.png"));
        MutableImage i2 = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/a_45_scaled.png"));
        Assert.assertEquals("Square error for baseline compare of matching scaled images", 0, MSEUtil.compareSlow(i1, i2, 33, 45, 0xff).squareError);
    }
}
