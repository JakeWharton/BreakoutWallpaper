package com.jakewharton.breakoutwallpaper;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import com.jakewharton.utilities.WidgetLocationsPreference;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;

public class Game implements SharedPreferences.OnSharedPreferenceChangeListener {
	/**
	 * Single random number generate for this wallpaper.
	 */
	/*package*/static final Random RANDOM = new Random();
	
	/**
	 * Tag used for logging.
	 */
	private static final String TAG = "BreakoutWallpaper.Game";
	
	/**
	 * Cell value for a blank space.
	 */
	private static final int CELL_BLANK = 0;
	
	/**
	 * Brick cells between icon rows.
	 */
	private static final int CELLS_BETWEEN_ROW = 2;
	
	/**
	 * Brick cells between icon columns.
	 */
	private static final int CELLS_BETWEEN_COLUMN = 1;
	
	private static final int PAINT_STYLE_FILL = 0;
	
	private static final int PAINT_STYLE_STROKE = 1;
	

	
	/**
	 * Number of cells on the board horizontally.
	 */
	private int mCellsWide;
	
	/**
	 * Number of cells on the board vertically.
	 */
	private int mCellsTall;
	
	/**
	 * Number of cells horizontally between the columns.
	 */
	private int mCellColumnSpacing;
	
	/**
	 * Number of cells vertically between the rows.
	 */
	private int mCellRowSpacing;
	
	/**
	 * Width (in pixels) of a single cell.
	 */
	private float mCellWidth;
	
	/**
	 * Height (in pixels) of a single cell.
	 */
	private float mCellHeight;
	
	/**
	 * Height (in pixels) of the screen.
	 */
    private int mScreenHeight;
    
    /**
     * Width (in pixels) of the screen.
     */
    private int mScreenWidth;
    
    private int mGameWidth;
    
    private int mGameHeight;
    
    /**
     * Whether or not the screen is currently in landscape mode.
     */
    private boolean mIsLandscape;
    
    /**
     * Number of icon rows on the launcher.
     */
    private int mIconRows;
    
    /**
     * Number of icon columns on the launcher.
     */
    private int mIconCols;
    
    /**
     * 2-dimensional array of the board's cells.
     * 
     * zero == blank
     * non-zero == block and represents its color
     */
	private int[][] mBoard;
    
    /**
     * Color of the background.
     */
    private int mGameBackground;
    
    /**
     * Top padding (in pixels) of the grid from the screen top.
     */
    private int mDotGridPaddingTop;
    
    /**
     * Left padding (in pixels) of the grid from the screen left.
     */
    private int mDotGridPaddingLeft;
    
    /**
     * Bottom padding (in pixels) of the grid from the screen bottom.
     */
    private int mDotGridPaddingBottom;
    
    /**
     * Right padding (in pixels) of the grid from the screen right.
     */
    private int mDotGridPaddingRight;
    
    /**
     * Path to the user background image (if any).
     */
    private String mBackgroundPath;
    
    /**
     * The user background image (if any).
     */
    private Bitmap mBackground;
    
    /**
     * The size (in pixels) of a single cell.
     */
    private final RectF mCellSize;
    
    /**
     * The locations of widgets on the launcher.
     */
    private List<Rect> mWidgetLocations;
    
    /**
     * Paint to draw the background color.
     */
    private final Paint mBackgroundPaint;
    
    /**
     * Paint to draw the bricks.
     */
    private final Paint mBlockForeground;
    
    /**
     * Paint to draw the balls.
     */
    private final Paint mBallForeground;
    
    /**
     * Balls. Enough said.
     */
    private Ball[] mBalls;
    
    /**
     * Colors for bricks.
     */
    private final LinkedList<Integer> mBrickColors;
    
    /**
     * Number of bricks remaining in the game.
     */
    private int mBricksRemaining;
    
    
    
    /**
     * Create a new game.
     */
    public Game() {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> Game()");
    	}

        //Create Paints
        this.mBackgroundPaint = new Paint();
        this.mBlockForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mBallForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
        //TODO: make preference
        this.mBallForeground.setColor(0xffffffff);
        
        this.mCellSize = new RectF(0, 0, 0, 0);
        
        this.mBrickColors = new LinkedList<Integer>();
        this.mBrickColors.add(0xffff0000);
        this.mBrickColors.add(0xff00ff00);
        this.mBrickColors.add(0xff0000ff);
        
        //Load all preferences or their defaults
        Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
        this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);

    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< Game()");
    	}
    }

    
    
    /**
     * Handle the changing of a preference.
     */
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> onSharedPreferenceChanged()");
    	}
    	
		final boolean all = (key == null);
		final Resources resources = Wallpaper.CONTEXT.getResources();
		
		boolean hasLayoutChanged = false;
		boolean hasGraphicsChanged = false;
		boolean hasBallsChanged = false;
		
		
		// GENERAL //
        
        int balls = 0;
        final String ghostCount = resources.getString(R.string.settings_game_ballcount_key);
        if (all || key.equals(ghostCount)) {
        	balls = preferences.getInt(ghostCount, resources.getInteger(R.integer.game_ballcount_default));
        	hasBallsChanged = true;
        	
        	if (Wallpaper.LOG_DEBUG) {
        		Log.d(Game.TAG, "Ball Count: " + balls);
        	}
        	
	    	this.mBalls = new Ball[balls];
	    	for (int i = 0; i < balls; i++) {
	    		this.mBalls[i] = new Ball();
	    	}
        }
		
		
		// COLORS //
        
		final String gameBackground = resources.getString(R.string.settings_color_background_key);
		if (all || key.equals(gameBackground)) {
			this.mGameBackground = preferences.getInt(gameBackground, resources.getInteger(R.integer.color_background_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Background: #" + Integer.toHexString(this.mGameBackground));
			}
		}
		
		final String backgroundImage = resources.getString(R.string.settings_color_bgimage_key);
		if (all || key.equals(backgroundImage)) {
			this.mBackgroundPath = preferences.getString(backgroundImage, null);
			
			if (this.mBackgroundPath != null) {			
				if (Wallpaper.LOG_DEBUG) {
					Log.d(Game.TAG, "Background Image: " + this.mBackgroundPath);
				}
				
				//Trigger performResize
				hasGraphicsChanged = true;
			} else {
				this.mBackground = null;
			}
		}
		
		final String backgroundOpacity = resources.getString(R.string.settings_color_bgopacity_key);
		if (all || key.equals(backgroundOpacity)) {
			this.mBackgroundPaint.setAlpha(preferences.getInt(backgroundOpacity, resources.getInteger(R.integer.color_bgopacity_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Background Image Opacity: " + this.mBackgroundPaint.getAlpha());
			}
		}
		
		final String blockStyle = resources.getString(R.string.settings_color_blockstyle_key);
		if (all || key.equals(blockStyle)) {
			final int blockStyleValue = preferences.getInt(blockStyle, resources.getInteger(R.integer.color_blockstyle_default));
			switch (blockStyleValue) {
				case Game.PAINT_STYLE_FILL:
					this.mBlockForeground.setStyle(Paint.Style.FILL);
					
					if (Wallpaper.LOG_DEBUG) {
						Log.d(Game.TAG, "Block Style: FILL");
					}
					break;
					
				case Game.PAINT_STYLE_STROKE:
					this.mBlockForeground.setStyle(Paint.Style.STROKE);
					
					if (Wallpaper.LOG_DEBUG) {
						Log.d(Game.TAG, "Block Style: STROKE");
					}
					break;
					
				default:
					Log.e(Game.TAG, "Invalid block style value " + blockStyleValue);
			}
		}
		
		final String ballStyle = resources.getString(R.string.settings_color_ballstyle_key);
		if (all || key.equals(ballStyle)) {
			final int ballStyleValue = preferences.getInt(ballStyle, resources.getInteger(R.integer.color_ballstyle_default));
			switch (ballStyleValue) {
				case Game.PAINT_STYLE_FILL:
					this.mBallForeground.setStyle(Paint.Style.FILL);
					
					if (Wallpaper.LOG_DEBUG) {
						Log.d(Game.TAG, "Ball Style: FILL");
					}
					break;
					
				case Game.PAINT_STYLE_STROKE:
					this.mBallForeground.setStyle(Paint.Style.STROKE);
					
					if (Wallpaper.LOG_DEBUG) {
						Log.d(Game.TAG, "Ball Style: STROKE");
					}
					break;
					
				default:
					Log.e(Game.TAG, "Invalid ball style value " + ballStyleValue);
			}
		}
    	
        
		// GRID //
		
		final String dotGridPaddingLeft = resources.getString(R.string.settings_display_padding_left_key);
		if (all || key.equals(dotGridPaddingLeft)) {
			this.mDotGridPaddingLeft = preferences.getInt(dotGridPaddingLeft, resources.getInteger(R.integer.display_padding_left_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Left: " + this.mDotGridPaddingLeft);
			}
		}

		final String dotGridPaddingRight = resources.getString(R.string.settings_display_padding_right_key);
		if (all || key.equals(dotGridPaddingRight)) {
			this.mDotGridPaddingRight = preferences.getInt(dotGridPaddingRight, resources.getInteger(R.integer.display_padding_right_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Right: " + this.mDotGridPaddingRight);
			}
		}

		final String dotGridPaddingTop = resources.getString(R.string.settings_display_padding_top_key);
		if (all || key.equals(dotGridPaddingTop)) {
			this.mDotGridPaddingTop = preferences.getInt(dotGridPaddingTop, resources.getInteger(R.integer.display_padding_top_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Top: " + this.mDotGridPaddingTop);
			}
		}

		final String dotGridPaddingBottom = resources.getString(R.string.settings_display_padding_bottom_key);
		if (all || key.equals(dotGridPaddingBottom)) {
			this.mDotGridPaddingBottom = preferences.getInt(dotGridPaddingBottom, resources.getInteger(R.integer.display_padding_bottom_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Bottom: " + this.mDotGridPaddingBottom);
			}
		}
		
		final String widgetLocations = resources.getString(R.string.settings_display_widgetlocations_key);
		if (all || key.equals(widgetLocations)) {
			this.mWidgetLocations = WidgetLocationsPreference.convertStringToWidgetList(preferences.getString(widgetLocations, resources.getString(R.string.display_widgetlocations_default)));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Widget Locations: " + (this.mWidgetLocations.size() / 4));
			}
		}
		
		
		// CELLS //
		
		final String iconRows = resources.getString(R.string.settings_display_iconrows_key);
		if (all || key.equals(iconRows)) {
			this.mIconRows = preferences.getInt(iconRows, resources.getInteger(R.integer.display_iconrows_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Icon Rows: " + this.mIconRows);
			}
		}
		
		final String iconCols = resources.getString(R.string.settings_display_iconcols_key);
		if (all || key.equals(iconCols)) {
			this.mIconCols = preferences.getInt(iconCols, resources.getInteger(R.integer.display_iconcols_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Icon Cols: " + this.mIconCols);
			}
		}
		
		final String cellSpacingRow = resources.getString(R.string.settings_display_rowspacing_key);
		if (all || key.equals(cellSpacingRow)) {
			this.mCellRowSpacing = preferences.getInt(cellSpacingRow, resources.getInteger(R.integer.display_rowspacing_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
		    	Log.d(Game.TAG, "Cell Row Spacing: " + this.mCellRowSpacing);
			}
		}
		
		final String cellSpacingCol = resources.getString(R.string.settings_display_colspacing_key);
		if (all || key.equals(cellSpacingCol)) {
			this.mCellColumnSpacing = preferences.getInt(cellSpacingCol, resources.getInteger(R.integer.display_colspacing_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
		    	Log.d(Game.TAG, "Cell Column Spacing: " + this.mCellColumnSpacing);
			}
		}
		
		if (hasLayoutChanged) {
	    	this.mCellsWide = (this.mIconCols * (this.mCellColumnSpacing + Game.CELLS_BETWEEN_COLUMN)) + Game.CELLS_BETWEEN_COLUMN;
	    	this.mCellsTall = (this.mIconRows * (this.mCellRowSpacing + Game.CELLS_BETWEEN_ROW)) + Game.CELLS_BETWEEN_ROW;
	    	
	    	if (Wallpaper.LOG_DEBUG) {
	    		Log.d(Game.TAG, "Cells Wide: " + this.mCellsWide);
	    		Log.d(Game.TAG, "Cells Tall: " + this.mCellsTall);
	    	}
	    	
	    	//Create playing board
	        this.mBoard = new int[this.mCellsTall][this.mCellsWide];
		}
		if (hasLayoutChanged || hasGraphicsChanged || hasBallsChanged) {
			if ((this.mScreenWidth > 0) && (this.mScreenHeight > 0)) {
				//Resize everything to fit
				this.performResize(this.mScreenWidth, this.mScreenHeight);
			}

	    	this.newGame();
		}

    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< onSharedPreferenceChanged()");
    	}
	}
	
	/**
	 * Get the width of a cell.
	 * 
	 * @return Cell width.
	 */
	public float getCellWidth() {
		return this.mCellWidth;
	}
	
	/**
	 * Get the height of a cell.
	 * 
	 * @return Cell height.
	 */
	public float getCellHeight() {
		return this.mCellHeight;
	}
	
	/**
	 * Determine whether or not a position contains a block.
	 * 
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @return Boolean.
	 */
	private boolean isBlock(final int x, final int y) {
		return ((x >= 0) && (x < this.mCellsWide) && (y >= 0) && (y < this.mCellsTall) && (this.mBoard[y][x] != Game.CELL_BLANK));
	}
	
	/**
	 * Manipulate a ball direction based on a user touch.
	 * 
	 * @param x X coordinate of touch.
	 * @param y Y coordinate of touch.
	 */
	public void setTouch(final float x, final float y) {
		//XXX: semi-broken
		double closestDistance = Float.MAX_VALUE;
		Ball closestBall = null;
		for (final Ball ball : this.mBalls) {
			final double ballDistance = Math.sqrt(Math.pow(ball.getLocationX() - x, 2) + Math.pow(ball.getLocationY() - y, 2));
			if (ballDistance < closestDistance) {
				closestBall = ball;
				closestDistance = ballDistance;
			}
		}
		
		closestBall.setVector(Math.abs(closestBall.getLocationX() - x), Math.abs(closestBall.getLocationY() - y));
	}
    
    /**
     * Reset the game state to that of first initialization.
     */
    public void newGame() {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> newGame()");
    	}

    	//Initialize board
    	final int iconCellsWidth = this.mCellColumnSpacing + Game.CELLS_BETWEEN_COLUMN;
    	final int iconCellsHeight = this.mCellRowSpacing + Game.CELLS_BETWEEN_ROW;
    	final int colors = this.mBrickColors.size();
    	for (int y = 0; y < this.mCellsTall; y++) {
    		for (int x = 0; x < this.mCellsWide; x++) {
    			final int dx = x % iconCellsWidth;
    			final int dy = y % iconCellsHeight;
    			if ((dx < Game.CELLS_BETWEEN_COLUMN) || (dy < Game.CELLS_BETWEEN_ROW)) {
    				this.mBoard[y][x] = this.mBrickColors.get((x + y) % colors);
    			} else {
    				this.mBoard[y][x] = Game.CELL_BLANK;
    			}
    		}
    	}
    	
    	//Remove board under widgets
    	for (final Rect widget : this.mWidgetLocations) {
    		if (Wallpaper.LOG_DEBUG) {
    			Log.d(Game.TAG, "Widget: L=" + widget.left + ", T=" + widget.top + ", R=" + widget.right + ", B=" + widget.bottom);
    		}
    		
    		final int left = (widget.left * iconCellsWidth) + Game.CELLS_BETWEEN_COLUMN;
    		final int top = (widget.top * iconCellsHeight) + Game.CELLS_BETWEEN_ROW;
    		final int bottom = (widget.bottom * iconCellsHeight) + Game.CELLS_BETWEEN_ROW + this.mCellRowSpacing - 1;
    		final int right = (widget.right * iconCellsWidth) + Game.CELLS_BETWEEN_COLUMN + this.mCellColumnSpacing - 1;
    		for (int y = top; y <= bottom; y++) {
    			for (int x = left; x <= right; x++) {
    				this.mBoard[y][x] = Game.CELL_BLANK;
    			}
    		}
    	}
    	
    	//Count bricks
    	this.mBricksRemaining = 0;
    	for (int y = 0; y < this.mCellsTall; y++) {
    		for (int x = 0; x < this.mCellsWide; x++) {
    			if (this.mBoard[y][x] != Game.CELL_BLANK) {
    				this.mBricksRemaining += 1;
    			}
    		}
    	}
    	
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< newGame()");
    	}
    }
    
    /**
     * Convert an icon position to on-screen coordinates
     * 
     * @param x Icon column
     * @param y Icon row
     * @return Screen coordinates.
     */
    private PointF getBallLocationAtIcon(final int x, final int y) {
    	return new PointF(
    			((this.mCellColumnSpacing * x) + (Game.CELLS_BETWEEN_COLUMN * (x + 1)) + (this.mCellColumnSpacing / 2.0f)) * this.mCellWidth,
    			((this.mCellRowSpacing * y) + (Game.CELLS_BETWEEN_ROW * (y + 1)) + (this.mCellRowSpacing / 2.0f)) * this.mCellHeight
    	);
    }
    
    /**
     * Iterate all entities one step.
     */
    public void tick() {
    	for (final Ball ball : this.mBalls) {
    		ball.tick(this);

    		//Test screen edges
    		if (ball.getLocationX() <= 0) {
    			ball.setVector(Math.abs(ball.getVectorX()), ball.getVectorY());
    		} else if (ball.getLocationX() >= this.mGameWidth) {
    			ball.setVector(-Math.abs(ball.getVectorX()), ball.getVectorY());
    		}
    		if (ball.getLocationY() <= 0) {
    			ball.setVector(ball.getVectorX(), Math.abs(ball.getVectorY()));
    		} else if (ball.getLocationY() >= this.mGameHeight) {
    			ball.setVector(ball.getVectorX(), -Math.abs(ball.getVectorY()));
    		}
    		
    		//Test blocks
    		final int ballCheck1X = (int)((ball.getLocationX() - Ball.RADIUS) / this.mCellWidth);
    		final int ballCheck1Y = (int)((ball.getLocationY() + (Math.signum(ball.getVectorY()) * Ball.RADIUS)) / this.mCellHeight);
    		final int ballCheck2X = (int)((ball.getLocationX() + Ball.RADIUS) / this.mCellWidth);
    		final int ballCheck2Y = ballCheck1Y;
    		final int ballCheck3X = (int)((ball.getLocationX() + (Math.signum(ball.getVectorX()) * Ball.RADIUS)) / this.mCellWidth);
    		final int ballCheck3Y = (int)((ball.getLocationY() + (-Math.signum(ball.getVectorY()) * Ball.RADIUS)) / this.mCellHeight);
    		this.checkCollision(ball, ballCheck1X, ballCheck1Y);
    		this.checkCollision(ball, ballCheck2X, ballCheck2Y);
    		this.checkCollision(ball, ballCheck3X, ballCheck3Y);
    	}
    	
    	if (this.mBricksRemaining <= 0) {
    		this.newGame();
    	}
    }
    
    /**
     * Determine if a ball has collided with a brick in the specified coordinates.
     * 
     * @param ball Ball instance.
     * @param blockX X coordinate of potential brick.
     * @param blockY Y coordinate of potential brick.
     * @return Boolean indicating collision.
     */
	private boolean checkCollision(final Ball ball, final int blockX, final int blockY)
	{
		if (Wallpaper.LOG_DEBUG) {
			Log.d(Game.TAG, "Checking block (" + blockX + "," + blockY + ") against ball at (" + ball.getLocationX() + "," + ball.getLocationY() + ")");
		}
		
		if (!this.isBlock(blockX, blockY)) {
			return false;
		}
		
		if (Wallpaper.LOG_DEBUG) {
			Log.d(Game.TAG, "-- Is Collision");
			Log.d(Game.TAG, "-- Current Vector: (" + ball.getVectorX() + "," + ball.getVectorY() + ")");
		}
		
		final float cellWidthOverTwo = this.mCellWidth / 2;
		final float cellHeightOverTwo = this.mCellHeight / 2;
		final float blockCenterX = (blockX * this.mCellWidth) + cellWidthOverTwo;
		final float blockCenterY = (blockY * this.mCellHeight) + cellHeightOverTwo;
		
		//Calculate collision unit vector
		float collisionUnitVectorX = blockCenterX - ball.getLocationX();
		float collisionUnitVectorY = blockCenterY - ball.getLocationY();
		final float collisionVectorLength = (float)Math.sqrt(Math.pow(collisionUnitVectorX, 2) + Math.pow(collisionUnitVectorY, 2));
		collisionUnitVectorX /= collisionVectorLength;
		collisionUnitVectorY /= collisionVectorLength;
		
		//Calculate ball velocity unit vector
		final float ballVectorLength = (float)Math.sqrt(Math.pow(ball.getVectorX(), 2) + Math.pow(ball.getVectorY(), 2));
		final float ballUnitVectorX = ball.getVectorX() / ballVectorLength;
		final float ballUnitVectorY = ball.getVectorY() / ballVectorLength;
		
		final float dotProduct = (collisionUnitVectorX * ballUnitVectorX) + (collisionUnitVectorY * ballUnitVectorY);
		final float vectorDeltaX = -2 * collisionUnitVectorX * dotProduct * ballVectorLength;
		final float vectorDeltaY = -2 * collisionUnitVectorY * dotProduct * ballVectorLength;
		
		float newVectorX = ball.getVectorX() + vectorDeltaX;
		float newVectorY = ball.getVectorY() + vectorDeltaY;
		final float newVectorLength = (float)Math.sqrt(Math.pow(newVectorX, 2) + Math.pow(newVectorY, 2));
		newVectorX /= newVectorLength;
		newVectorY /= newVectorLength;
		
		ball.setVector(newVectorX, newVectorY);
		
		if (Wallpaper.LOG_DEBUG) {
			Log.d(Game.TAG, "-- New Vector: (" + ball.getVectorX() + "," + ball.getVectorY() + ")");
		}
		
		this.mBoard[blockY][blockX] = Game.CELL_BLANK;
		this.mBricksRemaining -= 1;
		
		return true;
	}

    /**
     * Resize the game board and all entities according to a new width and height.
     * 
     * @param screenWidth New width.
     * @param screenHeight New height.
     */
    public void performResize(int screenWidth, int screenHeight) {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> performResize(width = " + screenWidth + ", height = " + screenHeight + ")");
    	}
    	
    	//Background image
    	if (this.mBackgroundPath != null) {
			try {
				final Bitmap temp = BitmapFactory.decodeStream(Wallpaper.CONTEXT.getContentResolver().openInputStream(Uri.parse(this.mBackgroundPath)));
				final float pictureAR = temp.getWidth() / (temp.getHeight() * 1.0f);
				final float screenAR = screenWidth / (screenHeight * 1.0f);
				int newWidth;
				int newHeight;
				int x;
				int y;
				
				if (pictureAR > screenAR) {
					//wider than tall related to the screen AR
					newHeight = screenHeight;
					newWidth = (int)(temp.getWidth() * (screenHeight / (temp.getHeight() * 1.0f)));
					x = (newWidth - screenWidth) / 2;
					y = 0;
				} else {
					//taller than wide related to the screen AR
					newWidth = screenWidth;
					newHeight = (int)(temp.getHeight() * (screenWidth / (temp.getWidth() * 1.0f)));
					x = 0;
					y = (newHeight - screenHeight) / 2;
				}
				
	    		final Bitmap scaled = Bitmap.createScaledBitmap(temp, newWidth, newHeight, false);
	    		this.mBackground = Bitmap.createBitmap(scaled, x, y, screenWidth, screenHeight);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.w(Game.TAG, "Unable to load background bitmap.");
				this.mBackground = null;
			}
    	}
    	
    	if (screenWidth > screenHeight) {
    		this.mIsLandscape = true;
    		final int temp = screenHeight;
    		screenHeight = screenWidth;
    		screenWidth = temp;
    	} else {
    		this.mIsLandscape = false;
    	}
    	
    	this.mScreenWidth = screenWidth;
    	this.mScreenHeight = screenHeight;
    	
    	if (this.mIsLandscape) {
    		this.mGameWidth = (screenWidth - this.mDotGridPaddingTop);
    		this.mCellWidth = this.mGameWidth / (this.mCellsWide * 1.0f);
    		this.mGameHeight = (screenHeight - (this.mDotGridPaddingBottom + this.mDotGridPaddingLeft + this.mDotGridPaddingRight));
    		this.mCellHeight = this.mGameHeight / (this.mCellsTall * 1.0f);
    	} else {
    		this.mGameWidth = (screenWidth - (this.mDotGridPaddingLeft + this.mDotGridPaddingRight));
    		this.mCellWidth = this.mGameWidth / (this.mCellsWide * 1.0f);
    		this.mGameHeight = (screenHeight - (this.mDotGridPaddingTop + this.mDotGridPaddingBottom));
    		this.mCellHeight = this.mGameHeight / (this.mCellsTall * 1.0f);
    	}
    	
    	//Update cell size
    	this.mCellSize.right = this.mCellWidth;
    	this.mCellSize.bottom = this.mCellHeight;
    	
    	//Set ball radius
    	Ball.RADIUS = ((this.mCellWidth < this.mCellHeight) ? this.mCellWidth : this.mCellHeight) * Ball.SIZE_PERCENTAGE / 2;
    	
    	//Position balls
    	//TODO: this should be in newGame();
    	final PointF ball0Location = this.getBallLocationAtIcon(0, 0);
    	this.mBalls[0].setLocation(ball0Location.x, ball0Location.y);
    	this.mBalls[0].setVector(0, -1);
    	if (this.mBalls.length > 1) {
    		final PointF ball1Location = this.getBallLocationAtIcon(this.mIconCols - 1, this.mIconRows - 1);
    		this.mBalls[1].setLocation(ball1Location.x, ball1Location.y);
        	this.mBalls[1].setVector(0, 1);
    	}
    	if (this.mBalls.length > 2) {
    		final PointF ball2Location = this.getBallLocationAtIcon(this.mIconCols - 1, 0);
    		this.mBalls[2].setLocation(ball2Location.x, ball2Location.y);
        	this.mBalls[2].setVector(1, 0);
    	}
    	if (this.mBalls.length > 3) {
    		final PointF ball3Location = this.getBallLocationAtIcon(0, this.mIconRows - 1);
    		this.mBalls[3].setLocation(ball3Location.x, ball3Location.y);
        	this.mBalls[3].setVector(-1, 0);
    	}
    	
    	if (Wallpaper.LOG_DEBUG) {
    		Log.d(Game.TAG, "Is Landscape: " + this.mIsLandscape);
    		Log.d(Game.TAG, "Screen Width: " + screenWidth);
    		Log.d(Game.TAG, "Screen Height: " + screenHeight);
    		Log.d(Game.TAG, "Cell Width: " + this.mCellWidth);
    		Log.d(Game.TAG, "Cell Height: " + this.mCellHeight);
    		Log.d(Game.TAG, "Ball Radius: " + Ball.RADIUS);
    	}

    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< performResize()");
    	}
    }
    
    /**
     * Render the board and all entities on a Canvas.
     * 
     * @param c Canvas to draw on.
     */
    public void draw(final Canvas c) {
    	c.save();
    	
    	//Clear the screen in case of transparency in the image
		c.drawColor(this.mGameBackground);
    	if (this.mBackground != null) {
    		//Bitmap should already be sized to the screen so draw it at the origin
    		c.drawBitmap(this.mBackground, 0, 0, this.mBackgroundPaint);
    	}
        
        if (this.mIsLandscape) {
        	//Perform counter-clockwise rotation
        	c.rotate(-90, this.mScreenWidth / 2.0f, this.mScreenWidth / 2.0f);
        	c.translate(0, this.mDotGridPaddingLeft);
        } else {
        	c.translate(this.mDotGridPaddingLeft, this.mDotGridPaddingTop);
        }
        
        //Draw dots and walls
        this.drawGameBoard(c);

        if (this.mIsLandscape) {
        	//Perform clockwise rotation back to normal
        	c.rotate(90, this.mScreenWidth / 2.0f, this.mScreenWidth / 2.0f);
        }
        
        c.restore();
    }

    /**
     * Render the dots and walls.
     * 
     * @param c Canvas to draw on.
     */
    private void drawGameBoard(final Canvas c) {
    	//draw blocks
        for (int y = 0; y < this.mCellsTall; y++) {
        	for (int x = 0; x < this.mCellsWide; x++) {
        		final int cell = this.mBoard[y][x];
        		if (cell != Game.CELL_BLANK) {
        			this.mBlockForeground.setColor(cell);
        			
        			final float left = x * this.mCellWidth;
        			final float top = y * this.mCellHeight;
        			final float right = left + this.mCellWidth;
        			final float bottom = top + this.mCellHeight;
        			
        			c.drawRect(left, top, right, bottom, this.mBlockForeground);
        		}
        	}
        }
        
        //draw balls
        for (final Ball ball : this.mBalls) {
        	c.drawRect(ball.getLocationX() - Ball.RADIUS, ball.getLocationY() - Ball.RADIUS, ball.getLocationX() + Ball.RADIUS, ball.getLocationY() + Ball.RADIUS, this.mBallForeground);
        }
    }
}
