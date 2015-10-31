package com.github.to2mbn.jmccc.option;

import java.io.Serializable;
import java.util.Objects;

public class WindowSize implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * True if the window is fullscreen
     */
    private boolean fullscreen;

    /**
     * Width of the window, default to 0
     */
    private int width;

    /**
     * Height of the window, default to 0
     */
    private int height;

    /**
     * Creates a full-screen WindowSize.
     */
    public WindowSize() {
        this.fullscreen = true;
    }

    /**
     * Creates a WindowSize with the given height and width.
     * 
     * @param width the width
     * @param height the height
     * @throws IllegalArgumentException if <code>width&lt;0||height&lt;0</code>
     */
    public WindowSize(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("width<0||height<0");
        }

        this.fullscreen = false;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns true if the window is fullscreen.
     * 
     * @return true if the window is fullscreen
     */
    public boolean isFullScreen() {
        return fullscreen;
    }

    /**
     * Sets the window size to fullscreen or not.
     * 
     * @param fullscreen true to set the window to fullscreen
     */
    public void setFullScreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    /**
     * Gets the width.
     * 
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width.
     * 
     * @param width the width to set
     * @throws IllegalArgumentException if <code>width&lt;0</code>
     */
    public void setWidth(int width) {
        if (width < 0) {
            throw new IllegalArgumentException("width<0");
        }

        this.width = width;
    }

    /**
     * Gets the height.
     * 
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height.
     * 
     * @param height the height to set
     * @throws IllegalArgumentException if <code>height&lt;0</code>
     */
    public void setHeight(int height) {
        if (height < 0) {
            throw new IllegalArgumentException("height<0");
        }

        this.height = height;
    }

    @Override
    public String toString() {
        return fullscreen ? "Fullscreen" : String.valueOf(width) + 'x' + String.valueOf(height);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof WindowSize) {
            WindowSize another = (WindowSize) obj;
            return (fullscreen == another.fullscreen) || (width == another.width && height == another.height);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fullscreen ? 1 : Objects.hash(width, height);
    }

}
