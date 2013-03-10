package com.sulauncher.supad3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;
import android.view.View;

public class ButtonNum {
	private static final int MaxRow = Supad3DView.MAX_ROW;
	private static final int MaxColumn = Supad3DView.MAX_COLUMN;
	private static final int Size = Supad3DView.SUPAD_BUTTON_WIDTH;
	private static final int Elevation = Supad3DView.SUPAD_BUTTON_NUM_ELEVATION;
	private static final float TranslateYDelta = (float)Supad3DView.SUPAD_HEIGHT * 4f/ 0x10000 / Supad3DView.ANIMATION_FRAMES /Supad3DView.ANIMATION_FRAMES;
	private static final float TranslateY = -(float)Supad3DView.SUPAD_HEIGHT * 2.5f / 0x10000;
	private OnHideFinishListener mOnHideFinishListener = null;
	private OnShowFinishListener mOnShowFinishListener = null;
	
	private IntBuffer mVerBuffer;
	private FloatBuffer mTexBuffer;
	private int texture;
	private boolean pressed = false;
	private boolean lastPressed = false;
	private boolean animating = false;
	private boolean showing = false;
	private float translateY = 0;
	private float translateYDelta;
	private float translateYAlpha;
	private Object changeStatusLock = new Object();
//	private float alpha = 1;
//	private float alphaDelta;
	// Our vertex buffer.
	// private FloatBuffer vertexBuffer;
	// Our index buffer.
	// private ShortBuffer indexBuffer;

	public int getTexture() {
		return texture;
	}

	public void setTexture(int numTexture) {
		this.texture = numTexture;
	}

	public float _anglex;
	public float _angley;

	public int[] vertexArray = new int[12];
	public int[] vertexBase = { Size, -Size, Elevation, Size, Size, Elevation, -Size, -Size, Elevation,
			-Size, Size, Elevation };
	public float[] textureArray = { 1, 1, 1, 0, 0, 1, 0, 0, };

	// The order we like to connect them.
	// private short[] indices = { 0, 1, 2, 0, 2, 3 };

    private int row;
    private int column;

    /**
     * @param row The row of the cell.
     * @param column The column of the cell.
     */
    public ButtonNum(int row, int column) {
        checkRange(row, column);
        this.row = row;
        this.column = column;
        reposition();
		mVerBuffer = getIntBuffeByArray(vertexArray);
		mTexBuffer = getFloatBuffByArray(textureArray);
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
//    public static synchronized Square of(int row, int column) {
//        checkRange(row, column);
//        return sSquare[row][column];
//    }

    private static void checkRange(int row, int column) {
        if (row < 0 || row > MaxRow) {
            throw new IllegalArgumentException("row must be in range 0-3");
        }
        if (column < 0 || column > MaxColumn) {
            throw new IllegalArgumentException("column must be in range 0-2");
        }
    }

    public String toString() {
        return "(row=" + row + ",clmn=" + column + ")";
    }
    
	private IntBuffer getIntBuffeByArray(int[] temp) {
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

	public void setAngle(float anglex, float angley, float anglez) {
//		_anglex = anglex;
//		_angley = angley;
		_anglex += anglex;
		_angley += angley;
//		Log.d("gl", "x set to" + _anglex);
		// _anglez += anglez;
	}

	public void draw(GL10 gl) {
		doAnimate();
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glTranslatef(0, translateY, 0);
//		gl.glRotatef(_anglex, 0, 1, 0);
//		gl.glRotatef(_angley, 1, 0, 0);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
		// Specifies the location and data format of
		// an array of vertex
		// coordinates to use when rendering.
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVerBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		// Counter-clockwise winding.
		// gl.glFrontFace(GL10.GL_CCW);
		// Enable face culling.
		// gl.glEnable(GL10.GL_CULL_FACE);
		// What faces to remove with the face culling.
		// gl.glCullFace(GL10.GL_BACK);

		// gl.glDrawElements(GL10.GL_TRIANGLES,
		// indices.length,GL10.GL_UNSIGNED_SHORT, indexBuffer);
		gl.glTranslatef(0, -translateY, 0);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		// Disable face culling.
		// gl.glDisable(GL10.GL_CULL_FACE);
	}
	public void reposition() {
		try {
		float offsetX = 0, offsetY = 0;
		switch (column) {
		case 0:
			offsetX = -(Supad3DView.squareW + Supad3DView.spaceW) * Supad3DView.GLPerPix;
			break;
		case 2:
			offsetX = (Supad3DView.squareW + Supad3DView.spaceW) * Supad3DView.GLPerPix;
			break;
		}
		switch (row) {
		case 0:
			offsetY = 1.5f * (Supad3DView.squareH + Supad3DView.spaceH) * Supad3DView.GLPerPix;
			break;
		case 1:
			offsetY = 0.5f * (Supad3DView.squareH + Supad3DView.spaceH) * Supad3DView.GLPerPix;
			break;
		case 2:
			offsetY = -0.5f * (Supad3DView.squareH + Supad3DView.spaceH) * Supad3DView.GLPerPix;
			break;
		case 3:
			offsetY = -1.5f * (Supad3DView.squareH + Supad3DView.spaceH) * Supad3DView.GLPerPix;
			break;
		}
		for (int i = 0; i < 12; i++) {
			switch (i % 3) {
			case 0:
				vertexArray[i] = vertexBase[i] + (int) offsetX;
				break;
			case 1:
				vertexArray[i] = vertexBase[i] + (int) offsetY;
//				Log.d("gl", "vertexBase" + vertexBase[i] + "offsetY" + offsetY);
				break;
			case 2:
				vertexArray[i] = vertexBase[i];
				break;
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void startShowing() {
		synchronized(changeStatusLock) {
//			translateYDelta = TranslateYDelta;
			translateYAlpha = TranslateYDelta * ((float) Math.random() * 2 + 1);
			Log.d("gl", "translateYDelta assigned to" + translateYDelta);
			animating = true;
			showing = true;
		}
	}
	public void startHiding() {
		synchronized(changeStatusLock) {
//			translateYDelta = -TranslateYDelta;
			translateYAlpha = -TranslateYDelta * ((float) Math.random() * 2+ 1);
			Log.d("gl", "translateYDelta assigned to" + translateYDelta);
			animating = true;
			showing = false;
		}
	}
	public void doAnimate() {
		synchronized(changeStatusLock) {
			if (animating) {
				translateYDelta += translateYAlpha;
				translateY += translateYDelta;
//				Log.d("gl", "translateYDelta is" + translateYDelta);
	//			alpha += alphaDelta;
				if (translateY < TranslateY && !showing) {
					translateY = TranslateY;
					translateYDelta = 0;
					translateYAlpha = 0;
	//				alpha = 0;
					animating = false;
					if (mOnHideFinishListener != null) {
						Log.d("gl", "will callback onHideFinish");
						mOnHideFinishListener.onHideFinish();
					}
				} else if (translateY > 0 && showing) {
					translateY = 0;
					translateYDelta = 0;
					translateYAlpha = 0;
	//				alpha = 1;
					animating = false;
					if (mOnShowFinishListener != null) {
						Log.d("gl", "will callback onShowFinish");
						mOnShowFinishListener.onShowFinish();
					}
				}
			}
		}
	}
	public static interface OnHideFinishListener {
		void onHideFinish();
	}
	public static interface OnShowFinishListener {
		void onShowFinish();
	}
	public void setOnHideFinishListener(OnHideFinishListener listener) {
		mOnHideFinishListener = listener;
	}
	public void setOnShowFinishListener(OnShowFinishListener listener) {
		mOnShowFinishListener = listener;
	}
}