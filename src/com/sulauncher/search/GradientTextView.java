package com.sulauncher.search;

import java.util.Timer;
import java.util.TimerTask;

import com.sulauncher.R;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * A gradient Textview mainly used as selector
 * @author YuMS
 */
public class GradientTextView extends TextView{
	public GradientDrawable gradientDrawable;
	private int alpha;
	private int minAlpha = 0;
	private boolean alphaInc;
	private boolean alphaing;
	private Timer timer;
	private GradientTextView self;
	private int originLeft;
	private int originHeight = 0;
	private int originTop = 0;
	public GradientTextView(Context context) {
		super(context);
		init(context);
	}
	
	public GradientTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public GradientTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public void init(Context context) {
		gradientDrawable = (GradientDrawable) context.getResources().getDrawable(R.drawable.list_selector);
		gradientDrawable.setAlpha(minAlpha);
		setBackgroundDrawable(gradientDrawable);
		alphaInc = true;
		timer = new Timer();
		alphaing = false;
		originLeft = this.getPaddingLeft();
	}

	public void setMinAlpha(int alpha) {
		minAlpha = alpha;
		gradientDrawable.setAlpha(alpha);
		this.alpha = alpha;
	}
	
	public void setTouching(boolean touching, GradientTextView self) {
		if (this.self == null) {
			this.self = self;
		}
		if (touching) {
			alphaInc = true;
			if (!alphaing) {
				alphaing = true;
				timer.schedule(new GradientTimer(), 0, 20);
			}
		} else {
			alphaInc = false;
			if (!alphaing) {
				alphaing = true;
				timer.schedule(new GradientTimer(), 100, 20);
			}
		}
	}
	class GradientTimer extends TimerTask {
		@Override
		public void run() {
			try {
			if (alphaInc && (alpha < 255)) {
				alpha += 25;
				if (alpha > 255) {
					alpha = 255;
				}
				self.post(new Runnable() {
					@Override
					public void run() {
						self.gradientDrawable.setAlpha(alpha);
						setBackgroundDrawable(gradientDrawable);
//						Log.d("haha", self.getLeft() + "");
						self.setLeft((alpha - minAlpha) / 5 + originLeft);
						self.setRotationY((alpha - minAlpha) / 5);
//						Log.d("hahainc", alpha + "");
					}
				});
			} else if ((!alphaInc) && alpha > minAlpha) {
				alpha -= 5;
				if (alpha < minAlpha) {
					alpha = minAlpha;
				}
				self.post(new Runnable() {
					
					@Override
					public void run() {
						self.gradientDrawable.setAlpha(alpha);
						setBackgroundDrawable(gradientDrawable);
//						Log.d("haha", self.getLeft() + "");
						self.setLeft((alpha - minAlpha) / 5 + originLeft);
						self.setRotationY((alpha - minAlpha) / 5);
//						Log.d("hahadec", alpha + "");
					}
				});
			} else {
				alphaing = false;
				cancel();
			}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
