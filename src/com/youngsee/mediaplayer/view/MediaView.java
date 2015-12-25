package com.youngsee.mediaplayer.view;

import com.youngsee.mediaplayer.util.Logger;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public abstract class MediaView extends LinearLayout {

	protected Logger mLogger = new Logger();
	
	protected Context mContext = null;

	protected int mViewWidth = -1;
	protected int mViewHeight = -1;

	public MediaView(Context context) {
		super(context);
		mContext = context;
	}

	public MediaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

    public MediaView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

    public int getViewWidth() {
    	return mViewWidth;
    }

    public int getViewHeight() {
    	return mViewHeight;
    }

    public void setViewWidth(int width) {
    	mViewWidth = width;
    }

    public void setViewHeight(int height) {
    	mViewHeight = height;
    }

	public abstract void onPause();
    public abstract void onResume();

    public abstract void onDestroy();

    public abstract void play();
    public abstract void pause();

    public abstract void visible();
    public abstract void gone();

}
