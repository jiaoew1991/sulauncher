package com.sulauncher.supad3d;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.sulauncher.MainActivity;
import com.sulauncher.R;
import com.sulauncher.Utils;
import com.sulauncher.supad3d.ButtonNum.OnHideFinishListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.Paint;
//import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * Supad3DView
 * @author YuMS
 *
 */
public class Supad3DView extends GLSurfaceView implements Renderer {
//	private static final String TAG = "Supad3DView";
//	private static final int[][] NumTextureRes = {
//			{R.drawable.supad_2_middle, R.drawable.supad_2_middle, R.drawable.supad_2_middle},
//			{R.drawable.supad_2_middle, R.drawable.supad_2_middle, R.drawable.supad_2_middle,},
//			{R.drawable.supad_2_middle, R.drawable.supad_2_middle, R.drawable.supad_2_middle,},
//			{R.drawable.supad_2_middle, R.drawable.supad_2_middle, R.drawable.supad_2_middle,},
//	};
//	
//	private static final int[][] NumTextureRes = {
//			{R.drawable.supad_1_middle, R.drawable.supad_2_middle, R.drawable.supad_3_middle},
//			{R.drawable.supad_4_middle, R.drawable.supad_5_middle, R.drawable.supad_6_middle,},
//			{R.drawable.supad_7_middle, R.drawable.supad_8_middle, R.drawable.supad_9_middle,},
//			{R.drawable.supad_star_middle, R.drawable.supad_0_middle, R.drawable.supad_sharp_middle,},
//	};
	private static final int[][] NumTextureRes = {
			{R.drawable.supad_1, R.drawable.supad_2, R.drawable.supad_3},
			{R.drawable.supad_4, R.drawable.supad_5, R.drawable.supad_6,},
			{R.drawable.supad_7, R.drawable.supad_8, R.drawable.supad_9,},
			{R.drawable.supad_star, R.drawable.supad_0, R.drawable.supad_sharp,},
	};
	public static final float TOUCH_SCALE_FACTOR = 180.f / 320;
	public static final int SUPAD_WIDTH = 0x26000;
	public static final float SUPAD_WIDTH_EXPAND_RATIO = 1.3f;
	public static final int SUPAD_HEIGHT = 0x26000;
	public static final int SUPAD_THICK = 0x02000;
	public static final int SUPAD_BUTTON_WIDTH = 0x08000;
	public static final int SUPAD_BUTTON_HEIGHT = 0x08000;
	public static final int SUPAD_BUTTON_LINE_ELEVATION = 0x07000;
	public static final int SUPAD_BUTTON_NUM_ELEVATION = 0x06000;
	public static final int SUPAD_BUTTON_BACK_ELEVATION = 0x05500;
	public static final int SUPAD_BUTTON_BACK_PRESSED_ELEVATION_OFFSET = -0x03000;
	public static final int ANIMATION_FRAMES = 20;
	public static final int MAX_ROW = 4;
	public static final int MAX_COLUMN = 3;
	public static final float MarginRatio = 0.08f;
	public static final float SpaceRatio = 0.2f;
	public static final float FingerOffsetRatio = 0.05f;
	public static final float Vertical2Dto3DRatio = 0.9f;
	
	private static final boolean PROFILE_DRAWING = false;
	
	public static float viewW;
	public static float viewH;
	public static float middleViewW;
	public static float middleViewH;
	public static float marginW;
	public static float marginH;
	public static float spaceWTotal;
	public static float spaceW;
	public static float squareWTotal;
	public static float squareW;
	public static float squareH;
	public static float spaceHTotal;
	public static float spaceH;
	public static float fingerOffsetH;
	public static float GLPerPix;
	
	private ButtonNum[][] mButtonNums = new ButtonNum[MAX_ROW][MAX_COLUMN];
	private ButtonBack[][] mButtonBacks = new ButtonBack[MAX_ROW][MAX_COLUMN];
	public ButtonBack[][] getmButtonBacks() {
		return mButtonBacks;
	}
	private Plate mPlate;
	
	private Context mContext;
	public int count;
	public boolean isPressing;
	private boolean isAnimating = false;
	private boolean isShowing = true;
	private boolean canMove = true;
	private boolean thisMoveValidate = true;
	private boolean blockAccidentallyHideSupad3D = false;
	private int lastWidth = 0;
	private int lastHeight = 0;
	
	private int frontTexture;
	private int picTexture;
	private int greenTexture;
	private int redTexture;
	private int blueTexture;
	private int[][] numTextures = new int[4][3];

	private boolean mDrawingProfilingStarted = false;
	private OnPatternListener mOnPatternListener;
	private OnResetPatternListener mOnResetPatternListener;
	private OnClickListener mOnClickListener;
	private ArrayList<ButtonBack> mPattern = new ArrayList<ButtonBack>(12);

	/**
	 * Lookup table for the circles of the pattern we are currently drawing.
	 * This will be the cells of the complete pattern unless we are animating,
	 * in which case we use this to hold the cells we are drawing for the in
	 * progress animation.
	 */
	private boolean[][] mPatternDrawLookup = new boolean[4][3];

	/**
	 * the in progress point:
	 * - during interaction: where the user's finger is
	 * - during animation: the current tip of the animating line
	 */
	private float mInProgressX = -1;
	private float mInProgressY = -1;

	private DisplayMode mPatternDisplayMode = DisplayMode.Correct;
	private boolean mEnableHapticFeedback = true;
	private boolean mPatternInProgress = false;

	private float mDiameterFactor = 0.10f;
//	private final int mStrokeAlpha = 128;
	private float mXHitFactor = 0.9f;
	private float mYHitFactor = 0.8f;

	private float mSquareWidth;
	private float mSquareHeight;
	
	public Supad3DView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public Supad3DView(Context context) {
		super(context);
		init(context);
	}

	public Supad3DView(Context context, AttributeSet attrs, Handler handler) {
		super(context, attrs);
		init(context);
	}
	void init(Context context) {
		Log.d("gl", "init");
//		if (!isInEditMode()) {
			setZOrderOnTop(true);
//		}
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		setRenderer(this);
//		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		mPlate = new Plate();
		mContext = context;
		count = 0;
	}

	private final Rect mInvalidate = new Rect();

	private int mBitmapWidth;
//	private int mBitmapHeight;

	/**
	 * How to display the current pattern.
	 */
	public enum DisplayMode {

		/**
		 * The pattern drawn is correct (i.e draw it in a friendly color)
		 */
		Correct,

		/**
		 * The pattern is wrong (i.e draw a foreboding color)
		 */
		Wrong
	}
	public static interface OnClickListener {
		void onClick(int column, int row);
	}
	public static interface OnResetPatternListener {
		void onResetPattern();
	}
	/**
	 * The call back interface for detecting patterns entered by the user.
	 */
	public static interface OnPatternListener {

		/**
		 * A new pattern has begun.
		 */
		void onPatternStart();

		/**
		 * The pattern was cleared.
		 */
		void onPatternCleared();

		/**
		 * The user extended the pattern currently being drawn by one cell.
		 * @param pattern The pattern with newly added cell.
		 */
		void onPatternCellAdded(List<ButtonBack> pattern);

		/**
		 * A pattern was detected from the user.
		 * @param pattern The pattern.
		 */
		void onPatternDetected(List<ButtonBack> pattern);
	}

	/**
	 * @return Whether the view has tactile feedback enabled.
	 */
	public boolean isTactileFeedbackEnabled() {
		return mEnableHapticFeedback;
	}

	/**
	 * Set whether the view will use tactile feedback.  If true, there will be
	 * tactile feedback as the user enters the pattern.
	 *
	 * @param tactileFeedbackEnabled Whether tactile feedback is enabled
	 */
	public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
		mEnableHapticFeedback = tactileFeedbackEnabled;
	}

	/**
	 * Set the call back for pattern detection.
	 * @param onPatternListener The call back.
	 */
	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}
	public void setOnPatternListener(
			OnPatternListener onPatternListener) {
		mOnPatternListener = onPatternListener;
	}
	public void setOnResetPatternListener(OnResetPatternListener listener){
		mOnResetPatternListener = listener;
	}

	/**
	 * Set the pattern explicitely (rather than waiting for the user to input
	 * a pattern).
	 * @param displayMode How to display the pattern.
	 * @param pattern The pattern.
	 */
	public void setPattern(DisplayMode displayMode, List<ButtonBack> pattern) {
		mPattern.clear();
		mPattern.addAll(pattern);
		clearPatternDrawLookup();
		for (ButtonBack cell : pattern) {
			mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
		}
		setDisplayMode(displayMode);
	}

	/**
	 * Set the display mode of the current pattern.  This can be useful, for
	 * instance, after detecting a pattern to tell this view whether change the
	 * in progress result to correct or wrong.
	 * @param displayMode The display mode.
	 */
	public void setDisplayMode(DisplayMode displayMode) {
		mPatternDisplayMode = displayMode;
		invalidate();
	}
	public int getSquareWidth() {
		return (int) (Math.min(mSquareHeight, mSquareWidth));
	}
	public int getCenterPointX(int column) {
		return  (int) (getPaddingLeft() + column * mSquareWidth);
	}
	public int getCenterPointY(int row) {
//		return (int) (getPaddingTop() + row * mSquareHeight);
		return (int) (row * mSquareHeight);
	}
	
	private void notifyCellAdded() {
		if (mOnPatternListener != null) {
			mOnPatternListener.onPatternCellAdded(mPattern);
		}
		sendAccessEvent(R.string.lockscreen_access_pattern_cell_added);
	}

	private void notifyPatternStarted() {
		if (mOnPatternListener != null) {
			mOnPatternListener.onPatternStart();
		}
		sendAccessEvent(R.string.lockscreen_access_pattern_start);
	}

	private void notifyPatternDetected() {
		if (mOnPatternListener != null) {
			mOnPatternListener.onPatternDetected(mPattern);
		}
		sendAccessEvent(R.string.lockscreen_access_pattern_detected);
	}

	private void notifyPatternCleared() {
		if (mOnPatternListener != null) {
			mOnPatternListener.onPatternCleared();
		}
		sendAccessEvent(R.string.lockscreen_access_pattern_cleared);
	}

	/**
	 * Clear the pattern.
	 */
	public void clearPattern() {
		resetPattern();
	}

	/**
	 * Reset all pattern state.
	 */
	private void resetPattern() {
		mPattern.clear();
		clearPatternDrawLookup();
		mPatternDisplayMode = DisplayMode.Correct;
		invalidate();
		if (mOnResetPatternListener != null) {
			mOnResetPatternListener.onResetPattern();
		}
	}

	/**
	 * Clear the pattern lookup table.
	 */
	private void clearPatternDrawLookup() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				mPatternDrawLookup[i][j] = false;
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		final int height = h - getPaddingTop() - getPaddingBottom();
		mSquareHeight = height  / 4.0f;		
		final int width = w - getPaddingLeft() - getPaddingRight();
		mSquareWidth = width / 3.0f;

	}

	private int resolveMeasured(int measureSpec, int desired) {
		int result = 0;
		int specSize = MeasureSpec.getSize(measureSpec);
		switch (MeasureSpec.getMode(measureSpec)) {
			case MeasureSpec.UNSPECIFIED:
				result = desired;
				break;
			case MeasureSpec.AT_MOST:
				result = Math.min(specSize, desired);
				break;
			case MeasureSpec.EXACTLY:
			default:
				result = specSize;
		}
		return result;
	}

	@Override
	protected int getSuggestedMinimumWidth() {
		// View should be large enough to contain 4 side-by-side target bitmaps
		return 4 * mBitmapWidth;
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		// View should be large enough to contain 4 side-by-side target bitmaps
		return 4 * mBitmapWidth;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int minimumWidth = getSuggestedMinimumWidth();
		final int minimumHeight = getSuggestedMinimumHeight();
		int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
		int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

		viewWidth = viewHeight = Math.min(viewWidth, viewHeight);
//		Log.v(TAG, "LockPatternView dimensions: " + viewWidth + "x" + viewHeight);
		setMeasuredDimension(viewWidth, viewHeight);
	}

	/**
	 * Determines whether the point x, y will add a new point to the current
	 * pattern (in addition to finding the cell, also makes heuristic choices
	 * such as filling in gaps based on current pattern).
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */
	private ButtonBack detectAndAddHit(float x, float y) {
		final ButtonBack cell = checkForNewHit(x, y);
		if (cell != null) {

			// check for gaps in existing pattern
//			Cell fillInGapCell = null;
			final ArrayList<ButtonBack> pattern = mPattern;
			if (!pattern.isEmpty()) {
				final ButtonBack lastCell = pattern.get(pattern.size() - 1);
				int dRow = cell.getRow() - lastCell.getRow();
				int dColumn = cell.getColumn() - lastCell.getColumn();

				@SuppressWarnings("unused")
				int fillInRow = lastCell.getRow();
				@SuppressWarnings("unused")
				int fillInColumn = lastCell.getColumn();

				if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
					fillInRow = lastCell.getRow() + ((dRow > 0) ? 1 : -1);
				}

				if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
					fillInColumn = lastCell.getColumn() + ((dColumn > 0) ? 1 : -1);
				}

			}

			addCellToPattern(cell);
			if (mEnableHapticFeedback) {
				performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
						HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
						| HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
			}
			return cell;
		}
		return null;
	}

	private void addCellToPattern(ButtonBack newCell) {
		mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
		mPattern.add(newCell);
		notifyCellAdded();
	}

	// helper method to find which cell a point maps to
	private ButtonBack checkForNewHit(float x, float y) {

		final int rowHit = getRowHit(y);
		if (rowHit < 0) {
			return null;
		}
		final int columnHit = getColumnHit(x);
		if (columnHit < 0) {
			return null;
		}

		if (mPatternDrawLookup[rowHit][columnHit]) {
			return null;
		}
		return mButtonBacks[rowHit][columnHit];
	}

	/**
	 * Helper method to find the row that y falls into.
	 * @param y The y coordinate
	 * @return The row that y falls in, or -1 if it falls in no row.
	 */
	private int getRowHit(float y) {

		final float squareHeight = squareH;
		float hitSize = squareHeight * mYHitFactor;
//		float offset = getPaddingTop() + marginH + (squareHeight - hitSize) / 2f;
		float offset = marginH + fingerOffsetH + (squareHeight - hitSize) / 2f;
		for (int i = 0; i < MAX_ROW; i++) {
			final float hitTop = offset + (squareHeight + spaceH) * i * Vertical2Dto3DRatio;
			if (y >= hitTop && y <= hitTop + hitSize) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Helper method to find the column x fallis into.
	 * @param x The x coordinate.
	 * @return The column that x falls in, or -1 if it falls in no column.
	 */
	private int getColumnHit(float x) {
		final float squareWidth = squareW;
		float hitSize = squareWidth * mXHitFactor;

//		float offset = getPaddingLeft() + marginW + (squareWidth - hitSize) / 2f;
		float offset = marginW + (squareWidth - hitSize) / 2f;
		for (int i = 0; i < MAX_COLUMN; i++) {

			final float hitLeft = offset + (squareWidth + spaceW) * i;
			if (x >= hitLeft && x <= hitLeft + hitSize) {
				return i;
			}
		}
		return -1;
	}
	public void setMoveable(boolean move) {
		canMove = move;
	}
	public boolean isAnimating() {
		return isAnimating;
	}
	public boolean isShowing() {
		return isShowing;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (event.getY() < marginH) {
					return false;
				}
				handleActionDown(event);
				return true;
			case MotionEvent.ACTION_UP:
				handleActionUp(event);
				return true;
			case MotionEvent.ACTION_MOVE:
				handleActionMove(event);
				return true;
			case MotionEvent.ACTION_CANCEL:
				resetPattern();
				mPatternInProgress = false;
				notifyPatternCleared();
				if (PROFILE_DRAWING) {
					if (mDrawingProfilingStarted) {
						Debug.stopMethodTracing();
						mDrawingProfilingStarted = false;
					}
				}
				return true;
		}
		return false;
	}

	private void handleActionMove(MotionEvent event) {
		// Handle all recent motion events so we don't skip any cells even when the device
		// is busy...
		if (!thisMoveValidate) return;
		float x = event.getX();
		float y = event.getY();
		if (x > 0 && y > 0) {
//			Log.d("middle", middleX + " " + middleY);
			mPlate.setAngle((x - middleViewW) * 0.03f, (y - middleViewH) * 0.03f, 5);
		}
		final int historySize = event.getHistorySize();
		for (int i = 0; i < historySize + 1; i++) {
			x = i < historySize ? event.getHistoricalX(i) : event.getX();
			y = i < historySize ? event.getHistoricalY(i) : event.getY();
			final int patternSizePreHitDetect = mPattern.size();
			ButtonBack hitCell = detectAndAddHit(x, y);
			final int patternSize = mPattern.size();
			if (hitCell != null && patternSize == 1) {
				mPatternInProgress = true;
				notifyPatternStarted();
			}
			if (!canMove && hitCell != null & patternSize >= 2) {
				resetPattern();
				invalidate();
				thisMoveValidate = false;
				return;
			}
			// note current x and y for rubber banding of in progress patterns
			final float dx = Math.abs(x - mInProgressX);
			final float dy = Math.abs(y - mInProgressY);
			if (dx + dy > mSquareWidth * 0.01f) {
				float oldX = mInProgressX;
				float oldY = mInProgressY;

				mInProgressX = x;
				mInProgressY = y;

				if (mPatternInProgress && patternSize > 0) {
					final ArrayList<ButtonBack> pattern = mPattern;
					final float radius = mSquareWidth * mDiameterFactor * 0.5f;

					final ButtonBack lastCell = pattern.get(patternSize - 1);

					float startX = getCenterXForColumn(lastCell.getColumn());
					float startY = getCenterYForRow(lastCell.getRow());

					float left;
					float top;
					float right;
					float bottom;

					final Rect invalidateRect = mInvalidate;

					if (startX < x) {
						left = startX;
						right = x;
					} else {
						left = x;
						right = startX;
					}

					if (startY < y) {
						top = startY;
						bottom = y;
					} else {
						top = y;
						bottom = startY;
					}

					// Invalidate between the pattern's last cell and the current location
					invalidateRect.set((int) (left - radius), (int) (top - radius),
							(int) (right + radius), (int) (bottom + radius));

					if (startX < oldX) {
						left = startX;
						right = oldX;
					} else {
						left = oldX;
						right = startX;
					}

					if (startY < oldY) {
						top = startY;
						bottom = oldY;
					} else {
						top = oldY;
						bottom = startY;
					}

					// Invalidate between the pattern's last cell and the previous location
					invalidateRect.union((int) (left - radius), (int) (top - radius),
							(int) (right + radius), (int) (bottom + radius));

					// Invalidate between the pattern's new cell and the pattern's previous cell
					if (hitCell != null) {
						startX = getCenterXForColumn(hitCell.getColumn());
						startY = getCenterYForRow(hitCell.getRow());

						if (patternSize >= 2) {
							// (re-using hitcell for old cell)
							hitCell = pattern.get(patternSize - 1 - (patternSize - patternSizePreHitDetect));
							oldX = getCenterXForColumn(hitCell.getColumn());
							oldY = getCenterYForRow(hitCell.getRow());

							if (startX < oldX) {
								left = startX;
								right = oldX;
							} else {
								left = oldX;
								right = startX;
							}

							if (startY < oldY) {
								top = startY;
								bottom = oldY;
							} else {
								top = oldY;
								bottom = startY;
							}
						} else {
							left = right = startX;
							top = bottom = startY;
						}

						final float widthOffset = mSquareWidth / 2f;
						final float heightOffset = mSquareHeight / 2f;

						invalidateRect.set((int) (left - widthOffset),
								(int) (top - heightOffset), (int) (right + widthOffset),
								(int) (bottom + heightOffset));
					}

					invalidate(invalidateRect);
				} else {
					invalidate();
				}
			}
		}
	}

	private void sendAccessEvent(int resId) {
		setContentDescription(mContext.getString(resId));
		sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
		setContentDescription(null);
	}

	private void handleActionUp(MotionEvent event) {
		// report pattern detected
//		resetPattern();
		isPressing = false;
		if (!thisMoveValidate) {
			thisMoveValidate = true;
			return;
		}
//		Log.d(TAG, "handle up");
//		Log.d(TAG, "mpattern " + mPattern.size());
		if (!mPattern.isEmpty()) {
			mPatternInProgress = false;
			if (mPattern.size() == 1) {
				ButtonBack hitCell = mPattern.get(0);
				if (mOnClickListener != null) {
					mOnClickListener.onClick(hitCell.getColumn(), hitCell.getRow());
				}
			}
			notifyPatternDetected();
			invalidate();
		}
		if (PROFILE_DRAWING) {
			if (mDrawingProfilingStarted) {
				Debug.stopMethodTracing();
				mDrawingProfilingStarted = false;
			}
		}
	}

	private void handleActionDown(MotionEvent event) {
		resetPattern();
		isPressing = true;
		final float x = event.getX();
		final float y = event.getY();
		if (x > 0 && y > 0) {
			mPlate.setAngle((x - middleViewW) * 0.03f, (y - middleViewH) * 0.03f, 1);
		}
		final ButtonBack hitCell = detectAndAddHit(x, y);
		if (hitCell != null) {
			mPatternInProgress = true;
			mPatternDisplayMode = DisplayMode.Correct;
			notifyPatternStarted();
		} else {
			mPatternInProgress = false;
			notifyPatternCleared();
		}
		if (hitCell != null) {
			final float startX = getCenterXForColumn(hitCell.getColumn());
			final float startY = getCenterYForRow(hitCell.getRow());

			final float widthOffset = mSquareWidth / 2f;
			final float heightOffset = mSquareHeight / 2f;

			invalidate((int) (startX - widthOffset), (int) (startY - heightOffset),
					(int) (startX + widthOffset), (int) (startY + heightOffset));
		}
		mInProgressX = x;
		mInProgressY = y;
		if (PROFILE_DRAWING) {
			if (!mDrawingProfilingStarted) {
				Debug.startMethodTracing("LockPatternDrawing");
				mDrawingProfilingStarted = true;
			}
		}
	}

	private float getCenterXForColumn(int column) {
		return getPaddingLeft() + column * mSquareWidth + mSquareWidth / 2f;
	}

	private float getCenterYForRow(int row) {
		return getPaddingTop() + row * mSquareHeight + mSquareHeight / 2f;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		return new SavedState(superState,
				Supad3DUtils.patternToString(mPattern),
				mPatternDisplayMode.ordinal(), mEnableHapticFeedback);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		final SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		setPattern(
				DisplayMode.Correct,
				Supad3DUtils.stringToPattern(ss.getSerializedPattern(), mButtonBacks));
		mPatternDisplayMode = DisplayMode.values()[ss.getDisplayMode()];
		mEnableHapticFeedback = ss.isTactileFeedbackEnabled();
	}

	/**
	 * The parecelable for saving and restoring a lock pattern view.
	 */
	private static class SavedState extends BaseSavedState {

		private final String mSerializedPattern;
		private final int mDisplayMode;
		private final boolean mTactileFeedbackEnabled;

		/**
		 * Constructor called from {@link SuPadView#onSaveInstanceState()}
		 */
		private SavedState(Parcelable superState, String serializedPattern, int displayMode,
				 boolean tactileFeedbackEnabled) {
			super(superState);
			mSerializedPattern = serializedPattern;
			mDisplayMode = displayMode;
			mTactileFeedbackEnabled = tactileFeedbackEnabled;
		}

		/**
		 * Constructor called from {@link #CREATOR}
		 */
		private SavedState(Parcel in) {
			super(in);
			mSerializedPattern = in.readString();
			mDisplayMode = in.readInt();
			mTactileFeedbackEnabled = (Boolean) in.readValue(null);
		}

		public String getSerializedPattern() {
			return mSerializedPattern;
		}

		public int getDisplayMode() {
			return mDisplayMode;
		}

		public boolean isTactileFeedbackEnabled(){
			return mTactileFeedbackEnabled;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeString(mSerializedPattern);
			dest.writeInt(mDisplayMode);
			dest.writeValue(mTactileFeedbackEnabled);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}

	@Override
	public void onDrawFrame(GL10 gl) {
//		Log.d("gl", "draw");
		if (!isPressing) {
			mPlate.setAngle(0, 0, 1);
		}
		final boolean[][] drawLookup = mPatternDrawLookup;
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glTranslatef(0, 0, -3f);
//		gl.glBlendFunc(GL10.GL_SRC_COLOR, GL10.GL_ONE_MINUS_SRC_COLOR);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		mPlate.draw(gl);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				setButtonBacksTexture(i, j, drawLookup[i][j]);
			}
			
		}
		for (int i = 0; i < MAX_ROW; i++) {
			for (int j = 0; j < MAX_COLUMN; j++) {
				mButtonBacks[i][j].draw(gl);
			}
		}
//		gl.glBlendFunc(GL10.GL_ONE_MINUS_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
//		gl.glBlendFunc(GL10.GL_DST_COLOR, GL10.GL_DST_COLOR);
//		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
//		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ZERO);
		for (int i = 0; i < MAX_ROW; i++) {
			for (int j = 0; j < MAX_COLUMN; j++) {
				mButtonNums[i][j].draw(gl);
			}
		}
//		mPathLine.draw(mPattern, gl);
		Message updateMessage = new Message();
		updateMessage.arg1 = count;
		++count;
//		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ZERO);
	}

	private void setButtonBacksTexture(int i, int j, boolean b) {
		if (b) {
			mButtonBacks[i][j].setTexture(redTexture);
			mButtonBacks[i][j].setPressed(true);
		} else {
			mButtonBacks[i][j].setTexture(greenTexture);
			mButtonBacks[i][j].setPressed(false);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d("gl", "changed");
		Log.d("gl", width + ", " + height);
		if (lastWidth == width && lastHeight == height) {
			Log.d("gl", "same return");
			return;
		} else {
			lastWidth = width;
			lastHeight = height;
		}
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		float ratio = (float) width / height;
		// gl.glOrthof(-ratio, ratio, -1, 1, 1, 10);
		gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		
		viewW = getWidth();
		viewH = getHeight();
		middleViewW = viewW / 2;
		middleViewH = viewH / 2;
		marginW = viewW * MarginRatio;
		marginH = viewH * MarginRatio;
		spaceWTotal = viewW * SpaceRatio;
		spaceW = spaceWTotal / (MAX_COLUMN - 1);
		squareWTotal = viewW - (marginW * 2 + spaceWTotal);
		squareW = squareWTotal / MAX_COLUMN;
		squareH = squareW;
		spaceHTotal = viewH - marginH * 2 - squareH * MAX_ROW;
		spaceH = spaceHTotal / (MAX_ROW - 1);
		fingerOffsetH = viewH * FingerOffsetRatio;
		GLPerPix = Supad3DView.SUPAD_WIDTH * 2 / viewW;
		
		for (int i = 0; i < MAX_ROW; i++) {
			for (int j = 0; j < MAX_COLUMN; j++) {
				mButtonBacks[i][j] = new ButtonBack(i, j);
				mButtonNums[i][j] = new ButtonNum(i, j);
//				Log.d("gl", "" + this.getWidth() + " " + this.getHeight());
				mButtonNums[i][j].setTexture(numTextures[i][j]);
				mButtonBacks[i][j].setTexture(greenTexture);
			}
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d("gl", "create");
		gl.glDisable(GL10.GL_DITHER);
//		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		gl.glClearColor(0, 0, 0, 0);
		gl.glShadeModel(GL10.GL_SMOOTH);

		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		gl.glHint(GL10.GL_LINE_SMOOTH, GL10.GL_NICEST);
		gl.glEnable(GL10.GL_POINT_SMOOTH);
		gl.glHint(GL10.GL_POINT_SMOOTH, GL10.GL_NICEST);

		gl.glEnable(GL10.GL_BLEND);					// 启用混色	
//		gl.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_ONE);			// 设置混色函数取得半透明效果
//		gl.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_ZERO);			// 设置混色函数取得半透明效果
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
//		gl.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_DST_ALPHA);			// 设置混色函数取得半透明效果
//		gl.glBlendFunc(GL10.GL_ONE_MINUS_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
//		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_SRC_ALPHA);
//		gl.glBlendFunc(GL10.GL_SRC_COLOR, GL10.GL_ONE_MINUS_DST_COLOR);
//		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.3f);
		
		picTexture = loadTexture(gl, mContext, R.drawable.white_board);
		frontTexture = loadTexture(gl, mContext, R.drawable.frame_only);
		mPlate.setOtherTexture(picTexture);
		mPlate.setFrontTexture(picTexture);
		greenTexture = loadTexture(gl, mContext, R.drawable.blue_board);
		blueTexture = loadTexture(gl, mContext, R.drawable.frosted_glass);
		redTexture = loadTexture(gl, mContext, R.drawable.red_board);
		
//		mPlate.setFrontTexture(blueTexture);
		for (int i = 0; i < MAX_ROW; i++) {
			for (int j = 0; j < MAX_COLUMN; j++) {
				numTextures[i][j] = loadTexture(gl, mContext, NumTextureRes[i][j]);
			}
		}
		gl.glBindTexture(GL10.GL_TEXTURE_2D, picTexture);
		onSurfaceChanged(gl, 0, 0);
	}
	public int loadTexture(GL10 gl, Context context, int res) {
		int textTure[] = new int[1];
		int mTextures;
		gl.glGenTextures(1, textTure, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textTure[0]);
		
		mTextures = textTure[0];
		InputStream is = context.getResources().openRawResource(res);
		Bitmap bitmap = BitmapFactory.decodeStream(is);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_NEAREST);
		return mTextures;
	}
	public void startExpanding() {
		if (mPlate != null) {
			mPlate.startExpanding();
		}
	}
	public void startShrinking() {
		if (mPlate != null) {
			mPlate.startShrinking();
		}
	}
	public void startHiding() {
		mButtonNums[0][0].setOnHideFinishListener(new ButtonNum.OnHideFinishListener() {
			@Override
			public void onHideFinish() {
				Log.d("gl", "onHideFinish");
				isAnimating = false;
				if (!blockAccidentallyHideSupad3D) {
					Message msg = MainActivity.mainHandler.obtainMessage();
					msg.arg1 = MainActivity.Messages.HIDE_SUPAD3DVIEW;
					MainActivity.mainHandler.sendMessage(msg);
				} else {
					Log.d("gl", "accident prevented");
				}
			}
		});
		Log.d("gl", "startHiding!");
		blockAccidentallyHideSupad3D = false;
		if (mPlate != null) {
			mPlate.startExpanding();
		}
		for (int i = 0; i < MAX_ROW; i++) {
			for (int j = 0; j < MAX_COLUMN; j++) {
				if (mButtonNums[i][j] != null) {
					mButtonNums[i][j].startHiding();
				}
			}
		}
		for (int i = 0; i < MAX_ROW; i++) {
			for (int j = 0; j < MAX_COLUMN; j++) {
				if (mButtonBacks[i][j] != null) {
					mButtonBacks[i][j].startHiding();;
				}
			}
		}
		isAnimating = true;
		isShowing = false;
//		mButtonNums[0][0].setOnShowFinishListener(new ButtonNum.OnShowFinishListener() {
//			@Override
//			public void onShowFinish() {
//				Log.d("gl", "onShowFinish");
//				Message msg = MainActivity.mainHandler.obtainMessage();
//				msg.arg1 = MainActivity.Messages.SHOW_SUPAD3DVIEW;
//				MainActivity.mainHandler.sendMessage(msg);
//			}
//		});
	}
	public void startShowing(boolean withBackground) {
		mButtonNums[0][0].setOnShowFinishListener(new ButtonNum.OnShowFinishListener() {
			@Override
			public void onShowFinish() {
				Log.d("gl", "onShowFinish");
				isAnimating = false;
			}
		});
		Log.d("gl", "startShowing!");
		blockAccidentallyHideSupad3D = true;
		Utils.showView(this);
		if (withBackground && mPlate != null) {
			mPlate.startShrinking();
		}
		for (int i = 0; i < MAX_ROW; i++) {
			for (int j = 0; j < MAX_COLUMN; j++) {
				if (mButtonNums[i][j] != null) {
					mButtonNums[i][j].startShowing();
				} else {
					Log.d("gl", "buttonNumEmpty");
				}
			}
		}
		for (int i = 0; i < MAX_ROW; i++) {
			for (int j = 0; j < MAX_COLUMN; j++) {
				if (mButtonBacks[i][j] != null) {
					mButtonBacks[i][j].startShowing();;
				}
			}
		}
		isAnimating = true;
		isShowing = true;
	}
}