package org.to2mbn.jmccc.test;

import org.junit.Test;
import org.to2mbn.jmccc.option.WindowSize;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WindowSizeTest {

    @Test
    public void testEqualsFullscreenSameSize() {
        assertTrue(WindowSize.fullscreen().equals(WindowSize.fullscreen()));
    }

    @Test
    public void testEqualsWindowSameSize() {
        assertTrue(new WindowSize(0, 100).equals(new WindowSize(0, 100)));
    }

    @Test
    public void testEqualsWindowDifferentSize() {
        assertFalse(new WindowSize(0, 100).equals(new WindowSize(100, 0)));
    }

    @Test
    public void testEqualsWindowDifferentSize2() {
        assertFalse(new WindowSize(0, 100).equals(new WindowSize(100, 100)));
    }

    @Test
    public void testEqualsFullscreenAndWindow() {
        assertFalse(new WindowSize(0, 0).equals(WindowSize.fullscreen()));
    }

}
