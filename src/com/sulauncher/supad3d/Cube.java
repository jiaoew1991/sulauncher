package com.sulauncher.supad3d;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class Cube {
	public final int oneF = 0x30000;
	private IntBuffer mVerBuffer;
	private FloatBuffer mTexBuffer;
	public float _anglex;
	public float _angley;

	public int[] vertex = {
			// front
			oneF, -oneF, oneF, oneF, oneF, oneF, -oneF,
			-oneF,
			oneF,
			-oneF,
			oneF,
			oneF,

			// back
			-oneF, -oneF, -oneF, -oneF, oneF, -oneF, oneF, -oneF,
			-oneF,
			oneF,
			oneF,
			-oneF,

			// left
			-oneF, -oneF, oneF, -oneF, oneF, oneF, -oneF, -oneF, -oneF,
			-oneF,
			oneF,
			-oneF,

			// right
			oneF, -oneF, -oneF, oneF, oneF, -oneF, oneF, -oneF, oneF, oneF,
			oneF,
			oneF,

			// top
			oneF, oneF, oneF, oneF, oneF, -oneF, -oneF, oneF, oneF, -oneF,
			oneF, -oneF,

			// bottom
			-oneF, -oneF, -oneF, oneF, -oneF, -oneF, -oneF, -oneF, oneF, oneF,
			-oneF, oneF,

	};

	public float[] texture = {
			// front
			1, 1, 1, 0, 0, 1, 0, 0,

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

	public Cube() {
		mVerBuffer = getIntBuffeByArray(vertex);
		mTexBuffer = getFloatBuffByArray(texture);
	}

	private IntBuffer getIntBuffeByArray(int[] temp) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertex.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		IntBuffer verBuffer = vbb.asIntBuffer();
		verBuffer.put(vertex);
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

	public void setAngle(float anglex, float angley, float anglez) {
		_anglex = anglex;
		_angley = angley;
		// _anglez += anglez;
	}

	protected void Draw(GL10 gl) {
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glRotatef(_anglex, 0, 1, 0);
		gl.glRotatef(_angley, 1, 0, 0);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVerBuffer);
		for (int i = 0; i < 6; i++) {
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, i * 4, 4);
		}
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	public static int LoadTexture(GL10 gl, Context context, int res) {
		int textTure[] = new int[1];
		int mTextures;
		gl.glGenTextures(1, textTure, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textTure[0]);
		mTextures = textTure[0];
		// InputStream is = context.getResources().openRawResource(res);
//		InputStream is = null;
//		Bitmap bitmap = null;
		InputStream is = context.getResources().openRawResource(res);
		Bitmap bitmap = BitmapFactory.decodeStream(is);
//		try {
//			is = context.getAssets().open(fileName);
//			bitmap = BitmapFactory.decodeStream(is);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		} finally {
//			try {
//				is.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
		// gl.glTexParameterf(GL10.GL_TEXTURE_2D,
		// GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
		// gl.glTexParameterf(GL10.GL_TEXTURE_2D,
		// GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);

		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
		return mTextures;
	}
}
