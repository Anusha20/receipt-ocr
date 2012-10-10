package org.korosoft.javaocr.tools.filter;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.korosoft.javaocr.core.ImgUtil;
import org.korosoft.javaocr.core.MutableImage;
import org.korosoft.javaocr.core.api.ImageScanner;
import org.korosoft.javaocr.core.impl.SimpleImageScanner;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Image shearer test
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class ShearerTest {

    private static final int EXPECTED_CHARS_COUNT = 45;

    @Test
    public void testShearerLeft() throws Exception {
        final BWLevelsCorrector levelsCorrector = new BWLevelsCorrector();
        final Shearer shearer = new Shearer();
        shearer.setColumnCount(3);
        final MutableImage image = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/test-rotated-l.png"));
        levelsCorrector.doFilter(image);
        Assert.assertTrue(String.format("Symbols count before shearing is less than %d", EXPECTED_CHARS_COUNT), getCharacterCount(image) < EXPECTED_CHARS_COUNT);
        shearer.doFilter(image);
        Assert.assertTrue(String.format("Symbols count before shearing is more or equal to %d", EXPECTED_CHARS_COUNT), getCharacterCount(image) >= EXPECTED_CHARS_COUNT);
    }

    @Test
    public void testShearerRight() throws Exception {
        final BWLevelsCorrector levelsCorrector = new BWLevelsCorrector();
        final Shearer shearer = new Shearer();
        shearer.setColumnCount(4);
        final MutableImage image = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/test-rotated-r.png"));
        levelsCorrector.doFilter(image);
        Assert.assertTrue(String.format("Symbols count before shearing is less than %d", EXPECTED_CHARS_COUNT), getCharacterCount(image) < EXPECTED_CHARS_COUNT);
        shearer.doFilter(image);
        Assert.assertEquals("Symbols count after shearing", EXPECTED_CHARS_COUNT, getCharacterCount(image));
    }

    @Ignore
    private int getCharacterCount(MutableImage image) {
        final SimpleImageScanner scanner = new SimpleImageScanner();
        final AtomicInteger i = new AtomicInteger(0);
        scanner.scan(image, new ImageScanner.Callback() {
            public void onNewLine() {
            }

            public void onNewSymbol(MutableImage symbol, int x, int y, int baseLine) {
                i.incrementAndGet();
            }

            public void onWhitespace(int lineHeight, int width) {
            }

            public void onFinished() {
            }
        });
        return i.get();
    }
}
