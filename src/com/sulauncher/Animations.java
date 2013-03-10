package com.sulauncher;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Animations' creator
 * @author YuMS
 */
public class Animations {
	public static LayoutAnimationController getLeftSearchListLayoutAnimationController() {
		AnimationSet set = new AnimationSet(true);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);
        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
		return controller;
	}
	public static LayoutAnimationController getRightSearchListLayoutAnimationController() {
		AnimationSet set = new AnimationSet(true);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);
        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 1.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
		return controller;
	}
	public static Animation getAlphaInAnimation() {
		Animation animation = new AlphaAnimation(0, 1);
		animation.setDuration(300);
		animation.setFillAfter(true);
		return animation;
}
	public static Animation getAlphaOutAnimation() {
		Animation animation = new AlphaAnimation(1, 0);
		animation.setDuration(300);
		animation.setFillAfter(true);
//		animation.setFillBefore(false);
		return animation;
	}
	public static Animation getAlphaLeftInAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, -1.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(getAlphaInAnimation());
		animation.addAnimation(tAnimation);
		return animation;
	}
//	public static Animation getRightParentInAnimation() {
//		TranslateAnimation tAnimation = new TranslateAnimation(
//				TranslateAnimation.RELATIVE_TO_PARENT, 1.0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
//				TranslateAnimation.RELATIVE_TO_PARENT, 0.0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.0f);
//		tAnimation.setDuration(200);
//		tAnimation.setFillAfter(true);
//		return tAnimation;
//	}
	public static Animation getAlphaLeftOutAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, -1.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(getAlphaOutAnimation());
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getAlphaRightInAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 1.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(getAlphaInAnimation());
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getAlphaRightOutAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 1.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(getAlphaOutAnimation());
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getAlphaBottomInAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 1.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(getAlphaInAnimation());
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getAlphaBottomOutAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 1.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(getAlphaOutAnimation());
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getAlphaTopInAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, -1.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(getAlphaInAnimation());
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getAlphaTopOutAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, -1.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(getAlphaOutAnimation());
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getSuddenInAnimation() {
		Animation animation = new AlphaAnimation(0, 1);
		animation.setDuration(0);
		animation.setFillAfter(true);
		return animation;
	}
	public static Animation getSuddenOutAnimation() {
		Animation animation = new AlphaAnimation(1, 0);
		animation.setDuration(0);
		animation.setFillAfter(true);
		return animation;
	}
	public static Animation getAlphaZoomInAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, -0.5f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, -0.5f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		ScaleAnimation sAnimation = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f);
		sAnimation.setDuration(500);
		sAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(getAlphaInAnimation());
		animation.addAnimation(sAnimation);
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getAlphaZoomOutAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, -0.5f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, -0.5f);
		tAnimation.setDuration(500);
		tAnimation.setFillBefore(false);
		tAnimation.setFillAfter(true);
		ScaleAnimation sAnimation = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f);
		sAnimation.setDuration(500);
		sAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(getAlphaOutAnimation());
		animation.addAnimation(sAnimation);
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getZoomInAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, -0.5f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, -0.5f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		ScaleAnimation sAnimation = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f);
		sAnimation.setDuration(500);
		sAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(sAnimation);
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getZoomOutAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, -0.5f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, -0.5f);
		tAnimation.setDuration(500);
		tAnimation.setFillBefore(false);
		tAnimation.setFillAfter(true);
		ScaleAnimation sAnimation = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f);
		sAnimation.setDuration(500);
		sAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(sAnimation);
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getZoomOutAppearAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.5f, TranslateAnimation.RELATIVE_TO_SELF, 0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.5f, TranslateAnimation.RELATIVE_TO_SELF, 0f);
		tAnimation.setDuration(500);
		tAnimation.setFillBefore(false);
		tAnimation.setFillAfter(true);
		ScaleAnimation sAnimation = new ScaleAnimation(0f, 1.0f, 0f, 1.0f);
		sAnimation.setDuration(500);
		sAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(sAnimation);
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getZoomInDisappearAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.5f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.5f);
		tAnimation.setDuration(500);
		tAnimation.setFillBefore(false);
		tAnimation.setFillAfter(true);
		ScaleAnimation sAnimation = new ScaleAnimation(1.0f, 0f, 1.0f, 0f);
		sAnimation.setDuration(500);
		sAnimation.setFillAfter(true);
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(sAnimation);
		animation.addAnimation(tAnimation);
		return animation;
	}
	public static Animation getSuddenLeftTranslateAnimation() {
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, -1.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(0);
		animation.setFillAfter(true);
		return animation;
	}

	public static Animation getLeftAlphaOutAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, -1.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		tAnimation.setDuration(300);
		tAnimation.setFillAfter(true);
		Animation aAnimation = getAlphaOutAnimation();
		AnimationSet animation = new AnimationSet(true);
		animation.addAnimation(tAnimation);
		animation.addAnimation(aAnimation);
		return animation;
	}
	public static Animation getBottomInAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 1.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		return tAnimation;
	}
	public static Animation getBottomOutAnimation() {
		TranslateAnimation tAnimation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.RELATIVE_TO_SELF, 1.0f);
		tAnimation.setDuration(500);
		tAnimation.setFillAfter(true);
		return tAnimation;
	}
}
