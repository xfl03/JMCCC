package org.to2mbn.jmccc.option;

import java.io.Serializable;

public class WindowSize implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * True for fullscreen
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
     *
     * @deprecated This may cause ambiguity. Use {@link #fullscreen()}.
     */
    @Deprecated
    public WindowSize() {
        this.fullscreen = true;
    }

    /**
     * Creates a WindowSize with the given height and width.
     *
     * @param width  the width
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
     * A factory method for creating fullscreen WindowSize. Returns a fullscreen WindowSize.
     *
     * @return a fullscreen WindowSize
     */
    public static WindowSize fullscreen() {
        return new WindowSize();
    }

    /**
     * A factory method for creating non-fullscreen WindowSize. Returns a
     * WindowSize with the given width and height.
     *
     * @param width  the width
     * @param height the height
     * @return a WindowSize with the given height and width
     * @throws IllegalArgumentException if <code>width&lt;0||height&lt;0</code>
     */
    public static WindowSize window(int width, int height) {
        return new WindowSize(width, height);
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
     * Gets the width.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
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
            return fullscreen == another.fullscreen
                    && (fullscreen ||
                    (width == another.width && height == another.height));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fullscreen
                ? 1
                : 31 * width + height;
    }

}
