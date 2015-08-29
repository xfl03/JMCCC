package com.darkyoooooo.jmccc.launch;

public class WindowSize {
    private boolean isFullSize;
    private int height, width;

    public WindowSize(boolean isFullSize) {
        this.isFullSize = isFullSize;
        this.height = this.width = 0;
    }

    public WindowSize(int height, int width) {
        this.isFullSize = false;
        this.height = height;
        this.width = width;
    }

    public boolean isFullSize() {
        return this.isFullSize;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }
}
