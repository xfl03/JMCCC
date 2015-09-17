package com.github.to2mbn.jmccc.option;

import java.util.Objects;

public class WindowSize {

    /**
     * Ture if game window is fullsize
     */
    private boolean fullSize;

    /**
     * Width of game window, default to 0
     */
    private int width;

    /**
     * Height of game window, default to 0
     */
    private int height;

    /**
     * Creates a fullsize WindowSize.
     */
    public WindowSize() {
        this.fullSize = true;
    }

    /**
     * Creates a WindowSize with given height and width.
     * 
     * @param width the width
     * @param height the height
     * @throws IllegalArgumentException if <code>width&lt;0||height&lt;0</code>
     */
    public WindowSize(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("width<0||height<0");
        }

        this.fullSize = false;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns ture if game window is fullsize.
     * 
     * @return ture if game window is fullsize
     */
    public boolean isFullSize() {
        return fullSize;
    }

    /**
     * Sets the fullSize.
     * 
     * @param fullSize true to set the game window to fullsize
     */
    public void setFullSize(boolean fullSize) {
        this.fullSize = fullSize;
    }

    /**
     * Gets the width
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
        return fullSize ? "Fullsize" : String.valueOf(width) + 'x' + String.valueOf(height);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof WindowSize) {
            WindowSize another = (WindowSize) obj;
            return (fullSize == another.fullSize) || (width == another.width && height == another.height);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fullSize ? 1 : Objects.hash(width, height);
    }

}
