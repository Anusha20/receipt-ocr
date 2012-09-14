package org.korosoft.javascr.core.impl;

import junit.framework.Assert;
import org.junit.Test;
import org.korosoft.javaocr.core.ImgUtil;
import org.korosoft.javaocr.core.MutableImage;
import org.korosoft.javaocr.core.api.ImageScanner;
import org.korosoft.javaocr.core.impl.SimpleImageScanner;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleImageScannerTest {
    @Test
    public void scanTest() throws Exception {
        MutableImage image = ImgUtil.readMutableImageFromSupportedStream(getClass().getResourceAsStream("/test.png"));
        SimpleImageScanner scanner = new SimpleImageScanner();
        scanner.getSettings().setWhiteThreshold(160);
        final AtomicInteger lineCount = new AtomicInteger(1);
        final AtomicInteger symbolCount = new AtomicInteger();
        final AtomicInteger whitespaceCount = new AtomicInteger();
        scanner.scan(image, new ImageScanner.Callback() {
            public void onNewLine() {
                lineCount.incrementAndGet();
            }

            public void onNewSymbol(MutableImage symbol, int x, int y) {
                symbolCount.incrementAndGet();
            }

            public void onWhitespace(int lineHeight, int width) {
                if ((double) width / (double)lineHeight > 0.2) {
                    whitespaceCount.incrementAndGet();
                }
            }

            public void onFinished() {
            }
        });
        Assert.assertEquals("Line count", 4, lineCount.get());
        Assert.assertEquals("Symbol count", 68, symbolCount.get());
        Assert.assertEquals("Whitespace count", 8, whitespaceCount.get());
    }

}
