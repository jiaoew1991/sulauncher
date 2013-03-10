/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sulauncher.supad;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.sulauncher.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays and detects the user's unlock attempt, which is a drag of a finger
 * across 9 regions of the screen.
 *
 * Is also capable of displaying a static pattern in "in progress", "wrong" or
 * "correct" states.
 */
public class SuPadView extends View {
    private static final String TAG = "SuPadView";
    // Aspect to use when rendering this view
    private static final int ASPECT_SQUARE = 0; // View will be the minimum of width/height
    private static final int ASPECT_LOCK_WIDTH = 1; // Fixed width; height will be minimum of (w,h)
    private static final int ASPECT_LOCK_HEIGHT = 2; // Fixed height; width will be minimum of (w,h)

    private static final boolean PROFILE_DRAWING = false;
    private boolean mDrawingProfilingStarted = false;

    private Paint mPaint = new Paint();
    private Paint mPathPaint = new Paint();

    static final int STATUS_BAR_HEIGHT = 25;

    /**
     * How many milliseconds we spend animating each circle of a lock pattern
     * if the animating mode is set.  The entire animation should take this
     * constant * the length of the pattern to complete.
     */
    private OnPatternListener mOnPatternListener;
    private OnResetPatternListener mOnResetPatternListener;
    private OnClickListener mOnClickListener;
    private ArrayList<Cell> mPattern = new ArrayList<Cell>(12);

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
    private final int mStrokeAlpha = 128;
    private float mHitFactor = 0.7f;

    private float mSquareWidth;
    private float mSquareHeight;

    private Bitmap[][] mBitmapSuPadNumber = new Bitmap[4][3];
    private Bitmap mBitmapCircleDefault;
    private Bitmap mBitmapBackgroundBlue;
    private Bitmap mBitmapBackgroundGreen;
    private Bitmap mBitmapBackgroundRed;
    private Bitmap mBitmapAppTray;
    

    private Bitmap mBitmapArrowGreenUp;
    private Bitmap mBitmapArrowRedUp;

    private final Path mCurrentPath = new Path();
    private final Rect mInvalidate = new Rect();

    private int mBitmapWidth;
    private int mBitmapHeight;

    private int mAspect;
    private final Matrix mArrowMatrix = new Matrix();
    private Context mContext = null;
//    private int getPaddingLeft() = 0;
//    private int getPaddingTop() = 0;
//    private int getPaddingRight() = 0;
//    private int getPaddingBottom() = 0;
	private final Matrix mCircleMatrix = new Matrix();


    public SuPadView(Context context) {
	    this(context, null);
	    mContext = context;
	}

	/**
     * Represents a cell in the 4 X 4 matrix of the unlock pattern view.
     */
    public static class Cell {
        int row;
        int column;

        // keep # objects limited to 9
        static Cell[][] sCells = new Cell[4][3];
        static {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 3; j++) {
                    sCells[i][j] = new Cell(i, j);
                }
            }
        }

        /**
         * @param row The row of the cell.
         * @param column The column of the cell.
         */
        private Cell(int row, int column) {
            checkRange(row, column);
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        /**
         * @param row The row of the cell.
         * @param column The column of the cell.
         */
        public static synchronized Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }

        private static void checkRange(int row, int column) {
            if (row < 0 || row > 4) {
                throw new IllegalArgumentException("row must be in range 0-3");
            }
            if (column < 0 || column > 3) {
                throw new IllegalArgumentException("column must be in range 0-2");
            }
        }

        public String toString() {
            return "(row=" + row + ",clmn=" + column + ")";
        }
    }

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
    public static  interface OnClickListener {
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
        void onPatternCellAdded(List<Cell> pattern);

        /**
         * A pattern was detected from the user.
         * @param pattern The pattern.
         */
        void onPatternDetected(List<Cell> pattern);
    }

    public SuPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LockPatternView);

        final String aspect = a.getString(R.styleable.LockPatternView_aspect);

        if ("square".equals(aspect)) {
            mAspect = ASPECT_SQUARE;
        } else if ("lock_width".equals(aspect)) {
            mAspect = ASPECT_LOCK_WIDTH;
        } else if ("lock_height".equals(aspect)) {
            mAspect = ASPECT_LOCK_HEIGHT;
        } else {
            mAspect = ASPECT_SQUARE;
        }

        setClickable(true);

        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);
        mPathPaint.setColor(Color.WHITE);
        mPathPaint.setAlpha(mStrokeAlpha);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);

        // lot's of bitmaps!
        mBitmapBackgroundBlue = getBitmapFor(R.drawable.supad_background_blue);
        mBitmapBackgroundGreen = getBitmapFor(R.drawable.supad_background_green);
        mBitmapBackgroundRed = getBitmapFor(R.drawable.supad_background_red);
        mBitmapAppTray = getBitmapFor(R.drawable.supad_app_tray);
        mBitmapCircleDefault = getBitmapFor(R.drawable.indicator_code_lock_point_area_default_holo);
        int[][] mBitmapNumbersID = {
        		{R.drawable.supad_1, R.drawable.supad_2, R.drawable.supad_3},
        		{R.drawable.supad_4, R.drawable.supad_5, R.drawable.supad_6,},
        		{R.drawable.supad_7, R.drawable.supad_8, R.drawable.supad_9,},
        		{R.drawable.supad_star, R.drawable.supad_0, R.drawable.supad_sharp,},
        };
        for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				mBitmapSuPadNumber[i][j] = getBitmapFor(mBitmapNumbersID[i][j]);
			}
		}
        mBitmapArrowGreenUp = getBitmapFor(R.drawable.indicator_code_lock_drag_direction_green_up_holo);
        mBitmapArrowRedUp = getBitmapFor(R.drawable.indicator_code_lock_drag_direction_red_up);

        // bitmaps have the size of the largest bitmap in this group
        final Bitmap bitmaps[] = { mBitmapSuPadNumber[0][0], mBitmapCircleDefault,
                 mBitmapBackgroundBlue, mBitmapBackgroundGreen, mBitmapBackgroundRed, mBitmapAppTray};

        for (Bitmap bitmap : bitmaps) {
        	Log.d("bitmap", "width: " + bitmap.getWidth());
            mBitmapWidth = Math.max(mBitmapWidth, bitmap.getWidth());
            mBitmapHeight = Math.max(mBitmapHeight, bitmap.getHeight());
        }

    }

    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
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
    public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
        mPattern.clear();
        mPattern.addAll(pattern);
        clearPatternDrawLookup();
        for (Cell cell : pattern) {
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
    	return (int) (getPaddingTop() + row * mSquareHeight);
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
        mSquareHeight = height / 4.0f;        
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

        switch (mAspect) {
            case ASPECT_SQUARE:
                viewWidth = viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_WIDTH:
                viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_HEIGHT:
//                viewWidth = Math.min(viewWidth, viewHeight);
                viewWidth = Math.min(viewWidth, (int) (viewHeight * 0.75f));
                break;
        }
         Log.v(TAG, "LockPatternView dimensions: " + viewWidth + "x" + viewHeight);
        setMeasuredDimension(viewWidth, viewHeight);
    }

    /**
     * Determines whether the point x, y will add a new point to the current
     * pattern (in addition to finding the cell, also makes heuristic choices
     * such as filling in gaps based on current pattern).
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    private Cell detectAndAddHit(float x, float y) {
        final Cell cell = checkForNewHit(x, y);
        if (cell != null) {

            // check for gaps in existing pattern
//            Cell fillInGapCell = null;
            final ArrayList<Cell> pattern = mPattern;
            if (!pattern.isEmpty()) {
                final Cell lastCell = pattern.get(pattern.size() - 1);
                int dRow = cell.row - lastCell.row;
                int dColumn = cell.column - lastCell.column;

                @SuppressWarnings("unused")
				int fillInRow = lastCell.row;
                @SuppressWarnings("unused")
				int fillInColumn = lastCell.column;

                if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                    fillInRow = lastCell.row + ((dRow > 0) ? 1 : -1);
                }

                if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                    fillInColumn = lastCell.column + ((dColumn > 0) ? 1 : -1);
                }

//                fillInGapCell = Cell.of(fillInRow, fillInColumn);
            }

//            if (fillInGapCell != null &&
//                    !mPatternDrawLookup[fillInGapCell.row][fillInGapCell.column]) {
//                addCellToPattern(fillInGapCell);
//            }
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

    private void addCellToPattern(Cell newCell) {
        mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
        mPattern.add(newCell);
        notifyCellAdded();
    }

    // helper method to find which cell a point maps to
    private Cell checkForNewHit(float x, float y) {

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
        return Cell.of(rowHit, columnHit);
    }

    /**
     * Helper method to find the row that y falls into.
     * @param y The y coordinate
     * @return The row that y falls in, or -1 if it falls in no row.
     */
    private int getRowHit(float y) {

        final float squareHeight = mSquareHeight;
        float hitSize = squareHeight * mHitFactor;

        float offset = getPaddingTop() + (squareHeight - hitSize) / 2f;
        for (int i = 0; i < 4; i++) {

            final float hitTop = offset + squareHeight * i;
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
        final float squareWidth = mSquareWidth;
        float hitSize = squareWidth * mHitFactor;

        float offset = getPaddingLeft() + (squareWidth - hitSize) / 2f;
        for (int i = 0; i < 3; i++) {

            final float hitLeft = offset + squareWidth * i;
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }

    private boolean canMove = true;
    private boolean thisMoveValidate = true;
    public void setMoveable(boolean move) {
    	canMove = move;
    }
    private boolean showBackGround = true;
    public void setShowBackGround(boolean showBackGround) {
    	this.showBackGround = showBackGround;
    }
//    @Override
//    public boolean onHoverEvent(MotionEvent event) {
////        if (AccessibilityManager.getInstance(mContext).isTouchExplorationEnabled()) {
//    	AccessibilityManager am = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
//        if (am.isTouchExplorationEnabled()) {
//            final int action = event.getAction();
//            switch (action) {
//                case MotionEvent.ACTION_HOVER_ENTER:
//                    event.setAction(MotionEvent.ACTION_DOWN);
//                    break;
//                case MotionEvent.ACTION_HOVER_MOVE:
//                    event.setAction(MotionEvent.ACTION_MOVE);
//                    break;
//                case MotionEvent.ACTION_HOVER_EXIT:
//                    event.setAction(MotionEvent.ACTION_UP);
//                    break;
//            }
//            onTouchEvent(event);
//            event.setAction(action);
//        }
//        return super.onHoverEvent(event);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
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
        final int historySize = event.getHistorySize();
        for (int i = 0; i < historySize + 1; i++) {
            final float x = i < historySize ? event.getHistoricalX(i) : event.getX();
            final float y = i < historySize ? event.getHistoricalY(i) : event.getY();
            final int patternSizePreHitDetect = mPattern.size();
            Cell hitCell = detectAndAddHit(x, y);
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
                    final ArrayList<Cell> pattern = mPattern;
                    final float radius = mSquareWidth * mDiameterFactor * 0.5f;

                    final Cell lastCell = pattern.get(patternSize - 1);

                    float startX = getCenterXForColumn(lastCell.column);
                    float startY = getCenterYForRow(lastCell.row);

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
                        startX = getCenterXForColumn(hitCell.column);
                        startY = getCenterYForRow(hitCell.row);

                        if (patternSize >= 2) {
                            // (re-using hitcell for old cell)
                            hitCell = pattern.get(patternSize - 1 - (patternSize - patternSizePreHitDetect));
                            oldX = getCenterXForColumn(hitCell.column);
                            oldY = getCenterYForRow(hitCell.row);

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
//    	resetPattern();
    	if (!thisMoveValidate) {
    		thisMoveValidate = true;
    		return;
    	}
    	Log.d(TAG, "handle up");
    	Log.d(TAG, "mpattern " + mPattern.size());
        if (!mPattern.isEmpty()) {
            mPatternInProgress = false;
	        if (mPattern.size() == 1) {
	        	Cell hitCell = mPattern.get(0);
	        	if (mOnClickListener != null) {
					mOnClickListener.onClick(hitCell.column, hitCell.row);
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
        final float x = event.getX();
        final float y = event.getY();
        final Cell hitCell = detectAndAddHit(x, y);
        if (hitCell != null) {
            mPatternInProgress = true;
            mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else {
            mPatternInProgress = false;
            notifyPatternCleared();
        }
        if (hitCell != null) {
            final float startX = getCenterXForColumn(hitCell.column);
            final float startY = getCenterYForRow(hitCell.row);

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
    protected void onDraw(Canvas canvas) {
        final ArrayList<Cell> pattern = mPattern;
        final int count = pattern.size();
        final boolean[][] drawLookup = mPatternDrawLookup;

        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;

        float radius = (squareWidth * mDiameterFactor * 0.5f);
        mPathPaint.setStrokeWidth(radius);

        final Path currentPath = mCurrentPath;
        currentPath.rewind();

        // draw the circles
        final int paddingTop = getPaddingTop();
        final int paddingLeft = getPaddingLeft();

        for (int i = 0; i < 4; i++) {
            float topY = paddingTop + i * squareHeight;
            //float centerY = getPaddingTop() + i * mSquareHeight + (mSquareHeight / 2);
            for (int j = 0; j < 3; j++) {
                float leftX = paddingLeft + j * squareWidth;
                drawCircle(canvas, (int) leftX, (int) topY, drawLookup[i][j], i, j);
            }
        }

        // only the last segment of the path should be computed here
        // draw the path of the pattern (unless the user is in progress, and
        // we are in stealth mode)
        final boolean drawPath = (mPatternDisplayMode == DisplayMode.Wrong);

        // draw the arrows associated with the path (unless the user is in progress, and
        // we are in stealth mode)
        boolean oldFlag = (mPaint.getFlags() & Paint.FILTER_BITMAP_FLAG) != 0;
        mPaint.setFilterBitmap(true); // draw with higher quality since we render with transforms
        if (drawPath) {
            for (int i = 0; i < count - 1; i++) {
                Cell cell = pattern.get(i);
                Cell next = pattern.get(i + 1);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if (!drawLookup[next.row][next.column]) {
                    break;
                }

                float leftX = paddingLeft + cell.column * squareWidth;
                float topY = paddingTop + cell.row * squareHeight;

                drawArrow(canvas, leftX, topY, cell, next);
            }
        }

        if (canMove) {
            boolean anyCircles = false;
            for (int i = 0; i < count; i++) {
                Cell cell = pattern.get(i);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if (!drawLookup[cell.row][cell.column]) {
                    break;
                }
                anyCircles = true;

                float centerX = getCenterXForColumn(cell.column);
                float centerY = getCenterYForRow(cell.row);
                if (i == 0) {
                    currentPath.moveTo(centerX, centerY);
                } else {
                    currentPath.lineTo(centerX, centerY);
                }
            }

            // add last in progress section
            if (mPatternInProgress && anyCircles) {
                currentPath.lineTo(mInProgressX, mInProgressY);
            }
            canvas.drawPath(currentPath, mPathPaint);
        }

        mPaint.setFilterBitmap(oldFlag); // restore default flag
    }

	private void drawArrow(Canvas canvas, float leftX, float topY, Cell start, Cell end) {
		if (!canMove) {
			return;
		}
        boolean green = mPatternDisplayMode != DisplayMode.Wrong;

        final int endRow = end.row;
        final int startRow = start.row;
        final int endColumn = end.column;
        final int startColumn = start.column;
        Bitmap arrow = green ? mBitmapArrowGreenUp : mBitmapArrowRedUp;
        final int cellWidth = mBitmapCircleDefault.getWidth();
        final int cellHeight = mBitmapCircleDefault.getHeight();
        // offsets for centering the bitmap in the cell
        final int offsetX = ((int) mSquareWidth - cellWidth) / 2;
        final int offsetY = ((int) mSquareHeight - cellHeight) / 2;

        // compute transform to place arrow bitmaps at correct angle inside circle.
        // This assumes that the arrow image is drawn at 12:00 with it's top edge
        // coincident with the circle bitmap's top edge.


        // the up arrow bitmap is at 12:00, so find the rotation from x axis and add 90 degrees.
        final float theta = (float) Math.atan2(
                (double) (endRow - startRow), (double) (endColumn - startColumn));
        final float angle = (float) Math.toDegrees(theta) + 90.0f;

        // compose matrix
        float sx = Math.min(mSquareWidth / cellWidth, 1.0f);
        float sy = Math.min(mSquareHeight / cellHeight, 1.0f);
        mArrowMatrix.setTranslate(leftX + offsetX, topY + offsetY); // transform to cell position
        mArrowMatrix.preTranslate(cellWidth/2, cellHeight/2);
        sy = Math.min(sx, sy);
        mArrowMatrix.preScale(sy, sy);
        mArrowMatrix.preTranslate(-cellWidth/2, -cellHeight/2);
        mArrowMatrix.preRotate(angle, cellWidth / 2.0f, cellHeight / 2.0f);  // rotate about cell center
        mArrowMatrix.preTranslate((cellWidth - arrow.getWidth()) / 2.0f, 0.0f); // translate to 12:00 pos
        canvas.drawBitmap(arrow, mArrowMatrix, mPaint);
    }

    /**
     * @param canvas
     * @param leftX
     * @param topY
     * @param partOfPattern Whether this circle is part of the pattern.
     */
    private void drawCircle(Canvas canvas, int leftX, int topY, boolean partOfPattern, int x, int y) {
        Bitmap suPadNumber = mBitmapSuPadNumber[x][y];
        Bitmap background = null;

        if (!partOfPattern && mPatternDisplayMode != DisplayMode.Wrong) {
            // unselected circle
            if (showBackGround) {
                background = mBitmapBackgroundBlue;
            }
        } else if (mPatternInProgress) {
            // user is in middle of drawing a pattern
            background = mBitmapBackgroundGreen;
        } else if (mPatternDisplayMode == DisplayMode.Wrong) {
            // the pattern is wrong
            background = mBitmapBackgroundRed;
        } else if (mPatternDisplayMode == DisplayMode.Correct) {
            background = mBitmapBackgroundGreen;
        } else {
            throw new IllegalStateException("unknown display mode " + mPatternDisplayMode);
        }
        final int width = suPadNumber.getWidth();
        final int height = suPadNumber.getHeight();

        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;

        int offsetX = (int) ((squareWidth - width) / 2f);
        int offsetY = (int) ((squareHeight - height) / 2f);

        // Allow circles to shrink if the view is too small to hold them.
        int delta = (int) (squareWidth - squareHeight) / 2;
        int adjust = (int) (squareWidth * .07);
        Rect outerRect = new Rect(leftX + delta + adjust, topY + adjust, 
        		leftX + (int) (squareWidth)- delta - adjust, topY + (int)(squareHeight) - adjust);
        Log.d("circle", "x: " + x + ", y: " + y +" " +  outerRect.toString());
        float sx = Math.min(mSquareWidth / width, 1.0f);
        float sy = Math.min(mSquareHeight / height, 1.0f);
        mCircleMatrix.setTranslate(leftX + offsetX, topY + offsetY);
        mCircleMatrix.preTranslate(width /2, height /2);
        sy = Math.min(sx, sy);
        mCircleMatrix.preScale(sy, sy);
        mCircleMatrix.preTranslate(-width /2, -height /2);
        canvas.drawBitmap(background, mCircleMatrix, mPaint);
        canvas.drawBitmap(suPadNumber, mCircleMatrix, mPaint);
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState,
                SuPadUtils.patternToString(mPattern),
                mPatternDisplayMode.ordinal(), mEnableHapticFeedback);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setPattern(
                DisplayMode.Correct,
                SuPadUtils.stringToPattern(ss.getSerializedPattern()));
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
}
