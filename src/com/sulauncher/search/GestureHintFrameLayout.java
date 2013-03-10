package com.sulauncher.search;

import java.util.ArrayList;

import com.sulauncher.Animations;
import com.sulauncher.R;
import com.sulauncher.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class GestureHintFrameLayout extends FrameLayout {
	private static float SQUARE_SIZE_FACTOR = 0.6f;
	private static float SPACING_FACTOR = 0.30f;
	private static int ROWS = 4;
	private static int COLUMNS = 5;
	
	private static int MATCH_BLANK_DRAWABLE = R.drawable.blank_yellow;
	private static int SQUARE_BLANK_MATCH = R.drawable.blank_green;
	private static int SQUARE_BLANK_PASS = R.drawable.blank_red;
	private static int SQUARE_BLANK_DEFAULT = R.drawable.blank_blue;

	private ImageView[][] gestureHintSquare = new ImageView[4][3];
	private ImageView gestureHintMatchSquare = null;
	private LineView lineView = null;
	
	private Context mContext = null;
	private int[][] gestureHintSquareBlankResources = new int[4][3];
	private boolean lastMatch = false;
	private boolean firstPress = true;
//	private ArrayList<Point> path = new ArrayList<Point>();
	private Path path = new Path();
	protected int squareSize, spacing, marginHorizontal, marginVertical;
	
	public GestureHintFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init(int width, int height, Context context) {
		int x, y;
		int squareWidth = (int) (width * SQUARE_SIZE_FACTOR / COLUMNS);
		int squareHeight = (int) (height * SQUARE_SIZE_FACTOR / ROWS);
		squareSize = Math.min(squareWidth, squareHeight);
		int spacingHorizontal = (int) (width * SPACING_FACTOR / (COLUMNS - 1));
		int spacingVertical = (int) (height * SPACING_FACTOR / (ROWS - 1));
		spacing = Math.min(spacingHorizontal, spacingVertical);
		marginHorizontal = width - squareSize * COLUMNS - spacing * (COLUMNS - 1);
		marginHorizontal /= 2;
		marginVertical = height - squareSize * ROWS - spacing * (ROWS - 1);
		marginVertical /= 2;
		mContext = context;
		for (int i = 0; i < 4; i++) {
			y = marginVertical + (squareSize + spacing) * i;
			for (int j = 0; j < 3; j++) {
				x = marginHorizontal + (squareSize + spacing) * j;
				gestureHintSquare[i][j] = new ImageView(context);
				gestureHintSquareBlankResources[i][j] = SQUARE_BLANK_DEFAULT;
				gestureHintSquare[i][j].setImageResource(gestureHintSquareBlankResources[i][j]);
				Log.d("image", "width: " + gestureHintSquare[i][j].getWidth()+ ", height: " + gestureHintSquare[i][j].getHeight());
				addView(gestureHintSquare[i][j]);
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(squareSize, squareSize);
				params.leftMargin = x;
				params.topMargin = y;
				gestureHintSquare[i][j].setLayoutParams(params);
				gestureHintSquare[i][j].setAlpha(200);
			}
		}
		gestureHintMatchSquare = new ImageView(context);
		gestureHintMatchSquare.setImageResource(R.drawable.blank_yellow);
		addView(gestureHintMatchSquare);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(squareSize, squareSize);
		params.leftMargin = marginHorizontal + (squareSize + spacing) * (COLUMNS - 1);
		params.topMargin = marginVertical + (squareSize + spacing) * ROWS / 2 - squareSize / 2;
		gestureHintMatchSquare.setLayoutParams(params);
		lineView = new LineView(context);
		params = new FrameLayout.LayoutParams(width, height);
		params.leftMargin = 0;
		params.topMargin = 0;
		params.rightMargin = 0;
		params.bottomMargin = 0;
		lineView.setLayoutParams(params);
		addView(lineView);
		Log.d("gesture", lineView.getWidth() + ", " + lineView.getHeight());
	}

	public void resetBlankDrawables() {
		for (int i = 0 ; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				gestureHintSquareBlankResources[i][j] = SQUARE_BLANK_DEFAULT;
			}
		}
		firstPress = true;
		path.reset();
	}

	public void setBlankDrawable(int x, int y) {
		gestureHintSquareBlankResources[x][y] = SQUARE_BLANK_PASS;
		if (firstPress) {
			path.moveTo(getYAt(y), getXAt(x));
			firstPress = false;
		} else {
			path.lineTo(getYAt(y), getXAt(x));
		}
		lineView.invalidate();
	}

	public void renewSquare(int x, int y, int resource) {
		Utils.hideView(gestureHintSquare[x][y]);
		gestureHintSquare[x][y].setImageResource(resource);
		Utils.showView(gestureHintSquare[x][y], Animations.getZoomOutAppearAnimation());
	}
	
	public void renewSquareToMatch(int x, int y) {
		Utils.hideView(gestureHintSquare[x][y]);
		gestureHintSquare[x][y].setImageResource(SQUARE_BLANK_MATCH);
		Utils.showView(gestureHintSquare[x][y], Animations.getZoomOutAppearAnimation());
	}
	
	public void renewSquare(int i, int j, Drawable icon) {
		Utils.hideView(gestureHintSquare[i][j]);
		gestureHintSquare[i][j].setImageDrawable(icon);
		Utils.showView(gestureHintSquare[i][j], Animations.getZoomOutAppearAnimation());
	}

	public void renewSquareToBlank(int i, int j) {
		Utils.hideView(gestureHintSquare[i][j]);
		gestureHintSquare[i][j].setImageResource(gestureHintSquareBlankResources[i][j]);
		Utils.showView(gestureHintSquare[i][j], Animations.getZoomOutAppearAnimation());
	}
	
	public void renewSquareToBlankWithoutAnim(int i, int j) {
		gestureHintSquare[i][j].setImageResource(gestureHintSquareBlankResources[i][j]);
	}
	
	public void renewMatchSquare(Drawable icon) {
		Utils.hideView(gestureHintMatchSquare);
		gestureHintMatchSquare.setImageDrawable(icon);
		Utils.showView(gestureHintMatchSquare, Animations.getZoomOutAppearAnimation());
		lastMatch = true;
	}

	public void renewMatchToBlank() {
		if (lastMatch) {
			Utils.hideView(gestureHintMatchSquare);
			gestureHintMatchSquare.setImageResource(MATCH_BLANK_DRAWABLE);
			Utils.showView(gestureHintMatchSquare, Animations.getZoomOutAppearAnimation());
			lastMatch = false;
		}
	}
	
	private class LineView extends View {
		private Paint paint = null;
//		private PathEffect dashPathEffect = new DashPathEffect(new float[]{20, 10, 5, 10}, 10);
		private PathEffect cornerPathEffect = new CornerPathEffect(mContext.getResources().getDisplayMetrics().density * 20);
		private PathEffect PathEffect = new DashPathEffect(new float[]{20, 10, 5, 10}, 10);

		public LineView(Context context) {
			super(context);
			paint = new Paint();
			paint.setColor(mContext.getResources().getColor(R.color.divider_half_trans));
			paint.setStrokeWidth(3);
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.STROKE);
			paint.setPathEffect(cornerPathEffect);
		}
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawPath(path, paint);
//			Point lastP = null;
//			for (Point p : path) {
//				if (lastP == null) {
//					lastP = p;
//				} else {
//					canvas.drawLine(getYAt(lastP.y), getXAt(lastP.x), getYAt(p.y), getXAt(p.x), paint);
//					lastP = p;
//				}
//			}
		}
	}
	private float getXAt(int x) {
		return marginVertical + (spacing + squareSize) * x + squareSize / 2;
	}

	private float getYAt(int y) {
		return marginHorizontal + (spacing + squareSize) * y + squareSize / 2;
	}
}
