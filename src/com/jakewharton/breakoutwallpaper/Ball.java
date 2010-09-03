package com.jakewharton.breakoutwallpaper;

import android.util.Log;

/**
 * Represents a single ball on the game board.
 * 
 * @author Jake Wharton
 */
public class Ball {
	/**
	 * Tag used for logging.
	 */
	private static final String TAG = "BreakoutWallpaper.Ball";
	
	/**
	 * Size relative to the smaller of cell width and cell height.
	 */
	/*package*/static final float SIZE_PERCENTAGE = 0.75f;
	
	/**
	 * Ball radius in a static context.
	 */
	/*package*/static float RADIUS = 0;
	
	/**
	 * Arbitrary ball speed.
	 */
	private static final int SPEED = 10;
	
	
	
	/**
	 * X coordinate of on-screen location.
	 */
	private float mLocationX;
	
	/**
	 * Y coordinate of on-screen location.
	 */
	private float mLocationY;
	
	/**
	 * X coordinate of per-tick movement vector.
	 */
	private float mVectorX;
	
	/**
	 * Y coordinate of per-tick movement vector.
	 */
	private float mVectorY;
	
	
	
	/**
	 * Create a ball.
	 */
	public Ball() {
		if (Wallpaper.LOG_VERBOSE) {
			Log.v(Ball.TAG, "> Ball()");
		}
		
		this.mLocationX = 0;
		this.mLocationY = 0;
		this.mVectorX = 0;
		this.mVectorY = 0;

		if (Wallpaper.LOG_VERBOSE) {
			Log.v(Ball.TAG, "< Ball()");
		}
	}
	
	
	
	/**
	 * Get X coordinate of on-screen location.
	 * 
	 * @return Float.
	 */
	public float getLocationX() {
		return this.mLocationX;
	}
	
	/**
	 * Get Y coordinate of on-screen location.
	 * 
	 * @return Float.
	 */
	public float getLocationY() {
		return this.mLocationY;
	}
	
	/**
	 * Set on-screen location.
	 * 
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 */
	public void setLocation(final float x, final float y) {
		this.mLocationX = x;
		this.mLocationY = y;
		
		if (Wallpaper.LOG_DEBUG) {
			Log.d(Ball.TAG, "Location: (" + x + ", " + y + ")");
		}
	}
	
	/**
	 * Get X coordinate of per-tick movement vector.
	 * 
	 * @return Float.
	 */
	public float getVectorX() {
		return this.mVectorY;
	}
	
	/**
	 * Get Y coordinate of per-tick movement vector.
	 * 
	 * @return Float.
	 */
	public float getVectorY() {
		return this.mVectorX;
	}
	
	/**
	 * Set per-tick movement vector. This will be normalized and then scaled to speed.
	 * 
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 */
	public void setVector(float x, float y) {
		final float length = (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		x /= length;
		y /= length;
		x *= Ball.SPEED;
		y *= Ball.SPEED;
		
		this.mVectorX = x;
		this.mVectorY = y;
	}
	
	/**
	 * Iterate the ball one step.
	 */
	public void tick() {
		this.mLocationX += this.mVectorX;
		this.mLocationY += this.mVectorY;
	}
}
