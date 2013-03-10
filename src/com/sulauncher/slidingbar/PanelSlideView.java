package com.sulauncher.slidingbar;

import org.miscwidgets.widget.Panel;

import com.sulauncher.Animations;
import com.sulauncher.R;
import com.sulauncher.SuApp;
import com.sulauncher.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PanelSlideView extends Panel {

	private Context mContext;
	public PanelSlideView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
//		mgd = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
//
//			@Override
//			public boolean onFling(MotionEvent e1, MotionEvent e2,
//					float velocityX, float velocityY) {
//				int dx = (int) Math.abs(e1.getX() - e2.getX());
//				if (dx > 40 && Math.abs(velocityX) > Math.abs(velocityY)) {
//					if (velocityX > 0 && PanelSlideView.this.isOpen()) {
//						PanelSlideView.this.setOpen(false, true);
//						return true;
//					}
//				}
//				return false;
//			}
//		});	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		RelativeLayout content = (RelativeLayout) this.getContent();
		editLayout = (RelativeLayout) content.findViewById(R.id.panel_edit_layout);
		Utils.hideView(editLayout);
		text = (TextView) content.findViewById(R.id.panel_name);
		text.bringToFront();
		mEdit = (EditText) content.findViewById(R.id.panel_edit_name);
		imm = (InputMethodManager) mEdit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		text.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Utils.hideView(text, Animations.getAlphaOutAnimation());
				Utils.showView(editLayout, Animations.getAlphaInAnimation());
				editLayout.bringToFront();
//				mEdit.bringToFront();
				mEdit.setText("");
				mEdit.setHint(text.getText());
			}
		});		
		mEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				} else {
					change2Text();
				}
			}
		});
		mButton = (Button) content.findViewById(R.id.panel_edit_name_ok);
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				change2Text();
			}
		});
//		favorite = (GridView) findViewById(R.id.panel_drawer_content_grid);
	}
	private OnChangeTextListener mListener;
	private TextView text;
	private EditText mEdit;
	private Button mButton;
	private RelativeLayout editLayout;
	private InputMethodManager imm;
	private GestureDetector mgd;
//	private GridView favorite;
	public void setOnChangeTextListener(OnChangeTextListener listener) {
		mListener = listener;
	}
	public interface OnChangeTextListener {
		public void onChangeText(String text);
	}
	public void change2Text() {
		Utils.hideView(editLayout, Animations.getAlphaOutAnimation());
		Utils.showView(text, Animations.getAlphaInAnimation());
		text.bringToFront();
		if (mEdit.getText().length() == 0)
			text.setText(mEdit.getHint());
		else
			text.setText(mEdit.getText());
			imm.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
		if (mListener != null) {
			mListener.onChangeText(text.getText().toString());
		}
	}
}
