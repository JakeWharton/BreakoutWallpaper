package com.jakewharton.breakoutwallpaper;

import android.graphics.PointF;
import android.util.Log;

/**
 * Represents a single ball on the game board.
 * 
 * @author Jake Wharton
 */
public class Ball {
	private static final String TAG = "BreakoutWallpaper.Ball";
	private static final float SIZE_PERCENTAGE = 0.75f;
	
	
	
	private final PointF mLocation;
	private final PointF mVector;
	private float mRadius;
	
	
	
	public Ball() {
		if (Wallpaper.LOG_VERBOSE) {
			Log.v(Ball.TAG, "> Ball()");
		}
		
		this.mLocation = new PointF(0, 0);
		this.mVector = new PointF(0, 0);

		if (Wallpaper.LOG_VERBOSE) {
			Log.v(Ball.TAG, "< Ball()");
		}
	}
	
	
	
	public PointF getLocation() {
		return this.mLocation;
	}
	
	public void setLocation(final float x, final float y) {
		this.mLocation.set(x, y);
		
		if (Wallpaper.LOG_DEBUG) {
			Log.d(Ball.TAG, "Location: (" + x + ", " + y + ")");
		}
	}
	
	public float getRadius() {
		return this.mRadius;
	}
	
	public PointF getVector() {
		return this.mVector;
	}
	
	public void setVector(final float x, final float y) {
		this.mVector.set(x, y);
	}
	
	public void toggleVectorX() {
		this.mVector.x = 0 - this.mVector.x;
	}
	
	public void toggleVectorY() {
		this.mVector.y = 0 - this.mVector.y;
	}
	
	public void tick(final Game game) {
		this.mLocation.x += this.mVector.x;
		this.mLocation.y += this.mVector.y;
	}
	
	public void performResize(final Game game) {
		this.mRadius = ((game.getCellWidth() < game.getCellHeight()) ? game.getCellWidth() : game.getCellHeight()) * Ball.SIZE_PERCENTAGE;
		
		if (Wallpaper.LOG_DEBUG) {
			Log.d(Ball.TAG, "Radius: " + this.mRadius);
		}
	}
}
