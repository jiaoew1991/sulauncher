package com.sulauncher;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;

public class Utils {
	/**
	 * Sudden show view.
	 * @param view
	 */
	public static void showView(View view) {
		showView(view, null);
	}
	/**
	 * Show view with animation
	 * @param view
	 * @param animation
	 */
	public static void showView(View view, Animation animation) {
		if (!view.isEnabled()) {
			view.setVisibility(View.VISIBLE);
			view.clearAnimation();
			view.setEnabled(true);
			if (animation != null) {
				view.startAnimation(animation);
			} else {
				view.startAnimation(Animations.getSuddenInAnimation());
			}
		}
	}
	/**
	 * Sudden hide view
	 * @param view
	 */
	public static void hideView(View view) {
		hideView(view, null);
	}
	/**
	 * Hide view with animation
	 * @param view
	 * @param animation
	 */
	public static void hideView(View view, Animation animation) {
		if (view.isEnabled()) {
			view.setVisibility(View.GONE);
			view.clearAnimation();
			view.setEnabled(false);
			if (animation != null) {
				view.startAnimation(animation);
			} else {
				view.startAnimation(Animations.getSuddenOutAnimation());
			}
		}
	}
	public static String makeStringComfortable(String label, int maxLength) {
		if (label.trim().getBytes().length <= maxLength)
			return label;
		StringBuffer sb = new StringBuffer();
		int length = 0;
		int index = 0;
		while (length < maxLength) {
			String tmp = label.substring(index, ++index);
			length += tmp.getBytes().length;
			sb.append(tmp);
		}
		sb.append("..");
//		if (sb.length() > 9) {
//			sb.delete(9, sb.length());
////			sb.substring(0, 9);
//			sb.append("...");
//		} else if (sb.toString().getBytes().length > 6 && sb.length() > 6) {
//			sb.delete(6, sb.length());
////			sb.substring(0, 9);
//			sb.append("...");
//		}
		return sb.toString();
	}
	public static List<String> makeMultiLineString(String label, int maxLength) {
		List<String> sl = new ArrayList<String>();
//		StringBuffer sb = new StringBuffer();
		int len = 0;
		while (len < label.length()) {
			int end = Math.min(len + maxLength, label.length());
			sl.add(label.substring(len, end));
//			sb.append(label.substring(len, end));
//			sb.append("\n");
			len += maxLength;
		}
		return sl;
	}
	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), 
				drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888 : Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	public static Drawable createTextDrawable(String text, int size) {
		size = size * 2;
		Bitmap bitmap = Bitmap.createBitmap(size, size, Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setTextSize(size);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setTextAlign(Align.CENTER);
		canvas.drawText(text.substring(0, 1).toUpperCase(), size / 2, size * 5 / 6, paint);
		return new BitmapDrawable(bitmap);
	}
}
