package com.windy.breakpadexample.view;

/**
 * Cone of flashlight that the user moves around the view.
 */

public class FlashlightCone {

    private int mX;
    private int mY;
    private int mRadius;

    public FlashlightCone(int viewWidth, int viewHeight) {
        mX = viewWidth / 2;
        mY = viewHeight / 2;
        // Adjust the radius for the narrowest view dimension.
        mRadius = ((viewWidth <= viewHeight) ? mX / 3 : mY / 3);
    }

    /**
     * Update the coordinates of the flashlight cone.
     *
     * @param newX Changed value for x coordinate.
     * @param newY Changed value for y coordinate.
     */
    public void update(int newX, int newY) {
        mX = newX;
        mY = newY;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public int getRadius() {
        return mRadius;
    }
}
