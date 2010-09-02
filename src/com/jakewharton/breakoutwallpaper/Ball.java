package com.jakewharton.breakoutwallpaper;

import android.util.Log;

/**
 * Represents a single ball on the game board.
 * 
 * @author Jake Wharton
 */
public class Ball {
	private static final String TAG = "BreakoutWallpaper.Ball";
	/*package*/static final float SIZE_PERCENTAGE = 0.75f;
	/*package*/static float RADIUS = 0;
	
	
	
	private float mLocationX;
	private float mLocationY;
	private float mVectorX;
	private float mVectorY;
	
	
	
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
	
	
	
	public float getLocationX() {
		return this.mLocationX;
	}
	
	public float getLocationY() {
		return this.mLocationY;
	}
	
	public void setLocation(final float x, final float y) {
		this.mLocationX = x;
		this.mLocationY = y;
		
		if (Wallpaper.LOG_DEBUG) {
			Log.d(Ball.TAG, "Location: (" + x + ", " + y + ")");
		}
	}
	
	public float getVectorY() {
		return this.mVectorX;
	}
	
	public float getVectorX() {
		return this.mVectorY;
	}
	
	public void setVectorX(final float x) {
		this.mVectorX = x;
	}
	
	public void setVectorY(final float y) {
		this.mVectorY = y;
	}
	
	public void tick(final Game game) {
		this.mLocationX += this.mVectorX;
		this.mLocationY += this.mVectorY;
	}
}
