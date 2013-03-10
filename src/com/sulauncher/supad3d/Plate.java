package com.sulauncher.supad3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

/**
 * @author YuMS
 *
 */
public class Plate {
	public static final int animationFrames = 20;
	
	public static final int Width = Supad3DView.SUPAD_WIDTH;
	public static final float WidthExpandRatio = Supad3DView.SUPAD_WIDTH_EXPAND_RATIO;
	public static final float WidthExpandDelta = (WidthExpandRatio - 1) / animationFrames;
	
	public static final float AlphaDelta = 1.0f / animationFrames;
	public static final int Height = Supad3DView.SUPAD_HEIGHT;
	public static final int Thick = Supad3DView.SUPAD_THICK;
	private IntBuffer mFrontVerBuffer;
	private FloatBuffer mFrontTexBuffer;
	private IntBuffer mOtherVerBuffer;
	private FloatBuffer mOtherTexBuffer;
	private int otherTexture;
	private int frontTexture;
	private float widthExpandRatio;
	private float widthExpandDelta;
	private float alpha;
	private float alphaDelta;
	private boolean scaling;
	public float _anglex;
	public float _angley;

	public int[] frontVertexArray = {
			// front
			Width, -Height, Thick, Width, Height, Thick, -Width,
			-Height,
			Thick,
			-Width,
			Height,
			Thick, };

	public int[] otherVertexArray = {
			// back
			-Width, -Height, -Thick, -Width, Height, -Thick, Width, -Height,
			-Thick,
			Width,
			Height,
			-Thick,

			// left
			-Width, -Height, Thick, -Width, Height, Thick, -Width, -Height, -Thick,
			-Width,
			Height,
			-Thick,

			// right
			Width, -Height, -Thick, Width, Height, -Thick, Width, -Height, Thick, Width,
			Height,
			Thick,
			
			// top
			Width, Height, Thick, Width, Height, -Thick, -Width, Height, Thick, -Width,
			Height, -Thick,

			// bottom
			-Width, -Height, -Thick, Width, -Height, -Thick, -Width, -Height, Thick, Width,
			-Height, Thick,

	};

	public float[] frontTextureArray = {
			// front
//			0.5f, 0.5f, .5f, 0, 0, .5f, 0, 0, };
			1, 1, 1, 0, 0, 1, 0, 0, };

	public float[] otherTextureArray = {
			// back
			1, 1, 1, 0, 0, 1, 0, 0,

			// left
			1, 1, 1, 0, 0, 1, 0, 0,

			// right
			1, 1, 1, 0, 0, 1, 0, 0,

			// top
			1, 1, 1, 0, 0, 1, 0, 0,

			// bottom
			1, 0, 0, 0, 1, 1, 0, 1, };

	public Plate() {
		mFrontVerBuffer = getIntBuffByArray(frontVertexArray);
		mFrontTexBuffer = getFloatBuffByArray(frontTextureArray);
		mOtherVerBuffer = getIntBuffByArray(otherVertexArray);
		mOtherTexBuffer = getFloatBuffByArray(otherTextureArray);
		widthExpandRatio = 1.0f;
		alpha = 1.0f;
		scaling = false;
	}

	private IntBuffer getIntBuffByArray(int[] temp) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(temp.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		IntBuffer verBuffer = vbb.asIntBuffer();
		verBuffer.put(temp);
		verBuffer.position(0);
		return verBuffer;
	}

	private FloatBuffer getFloatBuffByArray(float[] temp) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(temp.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer mVerBuffer = vbb.asFloatBuffer();
		mVerBuffer.put(temp);
		mVerBuffer.position(0);
		return mVerBuffer;
	}

	public void doAnimate() {
		if (scaling) {
			widthExpandRatio += widthExpandDelta;
			alpha += alphaDelta;
			if (widthExpandRatio > WidthExpandRatio) {
				widthExpandRatio = WidthExpandRatio;
				alpha = 0;
				scaling = false;
			} else if (widthExpandRatio < 1) {
				widthExpandRatio = 1;
				alpha = 1;
				scaling = false;
			}
		}
	}
	/**
	 * @param anglex
	 * @param angley
	 * @param speed maxSpeed is 10
	 */
	public void setAngle(float anglex, float angley, int speed) {
		if (speed > 10) {
			speed = 10;
		}
		if (speed < 1) {
			speed = 1;
		}
		_anglex = (anglex * speed + _anglex) / (1 + speed);
		_angley = (angley * speed + _angley) / (1 + speed);
	}

	protected void draw(GL10 gl) {
		doAnimate();
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glRotatef(_anglex, 0, 1, 0);
		gl.glRotatef(_angley, 1, 0, 0);
		gl.glColor4f(1, 1, 1, alpha);
		gl.glScalef(widthExpandRatio, 1, 1);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, otherTexture);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mOtherTexBuffer);
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mOtherVerBuffer);
		for (int i = 0; i < 5; i++) {
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, i * 4, 4);
		}
//		gl.glBindTexture(GL10.GL_TEXTURE_2D, frontTexture);
//		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mFrontTexBuffer);
//		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mFrontVerBuffer);
//		for (int i = 0; i < 1; i++) {
//			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, i * 4, 4);
//		}
		gl.glScalef(1 / widthExpandRatio, 1, 1);
		gl.glColor4f(1, 1, 1, 1);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	public int getOtherTexture() {
		return otherTexture;
	}

	public void setOtherTexture(int texture) {
		this.otherTexture = texture;
	}

	public int getFrontTexture() {
		return frontTexture;
	}

	public void setFrontTexture(int texture) {
		this.frontTexture = texture;
	}
	
	public void startExpanding() {
		scaling = true;
		widthExpandDelta = WidthExpandDelta;
		alphaDelta = -AlphaDelta;
	}
	
	public void startShrinking() {
		scaling = true;
		widthExpandDelta = -WidthExpandDelta;
		alphaDelta = AlphaDelta;
	}
}
