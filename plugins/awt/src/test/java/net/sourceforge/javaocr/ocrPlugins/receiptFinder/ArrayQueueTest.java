package net.sourceforge.javaocr.ocrPlugins.receiptFinder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Queue;

public class ArrayQueueTest {
    private Queue<Integer> queue;

    @Before
    public void setUp() throws Exception {
        queue = new ArrayQueue<Integer>();
    }

    @Test
    public void testEmpty() throws Exception {
        Assert.assertTrue("Should be empty", queue.isEmpty());
    }

    @Test
    public void testAddMuch() throws Exception {
        int count = 10000;
        for (int i = 0; i < count; i++) {
            queue.add(i);
        }
        Assert.assertEquals("Count after add " + count, count, queue.size());
    }

    @Test
    public void testAddMuchRemoveMuch() throws Exception {
        int count = 10000;
        for (int i = 0; i < count; i++) {
            queue.add(i);
        }
        for (int i = 0; i < count; i++) {
            Assert.assertEquals("Retrieved value", i, (int)queue.remove());
        }
        Assert.assertTrue("Should be empty", queue.isEmpty());
    }

    @Test
    public void testAddRemoveMuch() throws Exception {
        int count = 1000000;
        for (int i = 0; i < count; i++) {
            queue.add(i);
            Assert.assertEquals("Retrieved value", i, (int)queue.remove());
        }
        Assert.assertTrue("Should be empty", queue.isEmpty());
    }

    @Test
    public void testAdd50AddRemoveMuch() throws Exception {
        int count = 1000000;
        for (int i = 0; i < 50; i++) {
            queue.add(i);
        }
        for (int i = 0; i < count; i++) {
            queue.add(i);
            Assert.assertEquals("Retrieved value", i < 50 ? i : (i - 50), (int)queue.remove());
        }
        Assert.assertEquals("Size after add/remove", 50, queue.size());
    }
}
