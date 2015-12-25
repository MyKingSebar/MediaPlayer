package com.youngsee.mediaplayer.activity;

import java.util.ArrayList;
import java.util.List;

import com.youngsee.mediaplayer.MediaApplication;
import com.youngsee.mediaplayer.R;
import com.youngsee.mediaplayer.common.Actions;
import com.youngsee.mediaplayer.common.Constants;
import com.youngsee.mediaplayer.util.Logger;
import com.youngsee.mediaplayer.view.MultiMediaView;
import com.youngsee.mediaplayer.view.MediaView;

import android.os.Bundle;
import android.os.ExtendDisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.MessageQueue.IdleHandler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MediaActivity extends Activity {
	
	private final String TAG = "MediaActivity";

	private Logger mLogger = new Logger();

	private final int EVENT_BASE = 0x9000;
	private final int EVENT_SHOW = EVENT_BASE + 0;

	private PowerManager.WakeLock mWakeLock = null;

	private FrameLayout mFrameLayout= null;

	private List<MediaViewInfo> mViewInfoLst = null;

	private int mScreenWidth = -1;
	private int mScreenHeight = -1;

	private MediaReceiver mMediaReceiver = null;
	private IntentFilter mMediaReceiverFilter = null;

	private long mExitTime = 0;

	private class MediaViewInfo {
		public int x;
		public int y;
		public int w;
		public int h;
		public MediaView view;
		
		public MediaViewInfo(int x, int y, int w, int h, MediaView view) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.view = view;
		}
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        mFrameLayout = (FrameLayout)findViewById(R.id.activity_md_lyt);

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(
        		PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, TAG);

        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;
		mScreenHeight = dm.heightPixels;

		mViewInfoLst = new ArrayList<MediaViewInfo>();

        initViews();

        initReceiver();

        Looper.myQueue().addIdleHandler(new IdleHandler() {
            @Override
            public boolean queueIdle() {
            	if (MediaApplication.getInstance().isDaulScreenMode()
                		&& !MediaApplication.getInstance().isShowInExtendDisplay()) {
                    mHandler.postDelayed(rGoToExtendScreenDelay, 200);
                    MediaApplication.getInstance().setShowInExtendDisplay(true);
                }

            	return false;
            }
        });
    }

    private void initViews() {
    	MediaView view = new MultiMediaView(this);
    	view.setViewWidth(mScreenWidth);
    	view.setViewHeight(mScreenHeight);

    	view.setX(0);
    	view.setY(0);
		mFrameLayout.addView(view, mScreenWidth, mScreenHeight);

		MediaViewInfo viewinfo = new MediaViewInfo(0, 0, mScreenWidth, mScreenHeight, view);
		mViewInfoLst.add(viewinfo);
    }

    private void initReceiver() {
		mMediaReceiver = new MediaReceiver();

		mMediaReceiverFilter = new IntentFilter();
		mMediaReceiverFilter.addAction(Actions.MEDIAPLAYER_CONTROL_ACTION);
	}

    @Override
	protected void onResume() {
		super.onResume();
		
		if (mWakeLock != null) {
        	mWakeLock.acquire();
        }
		
		if (mViewInfoLst != null) {
    		for (MediaViewInfo info : mViewInfoLst) {
    			info.view.onResume();
    		}
    	}

		registerReceiver(mMediaReceiver, mMediaReceiverFilter);
    }

    @Override
	protected void onPause() {
    	unregisterReceiver(mMediaReceiver);

    	if (mViewInfoLst != null) {
    		for (MediaViewInfo info : mViewInfoLst) {
    			info.view.onPause();
    		}
    	}

    	if (mWakeLock != null) {
        	mWakeLock.release();
        }

    	super.onPause();
    }

    @Override
	protected void onDestroy() {
    	if (mViewInfoLst != null) {
    		for (MediaViewInfo info : mViewInfoLst) {
    			info.view.onDestroy();
    		}
    	}

    	super.onDestroy();
    }

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if ((System.currentTimeMillis() - mExitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
	            mExitTime = System.currentTimeMillis();
	        } else {
	            finish();
	            System.exit(0);
	        }
            return true;
        case KeyEvent.KEYCODE_MENU:
            return true;
        case KeyEvent.KEYCODE_PAGE_UP:
            return true;
        case KeyEvent.KEYCODE_PAGE_DOWN:
            return true;
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            return true;
        case KeyEvent.KEYCODE_MEDIA_STOP:
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void sendToExtendScreen() {
        mHandler.removeCallbacks(rGoToExtendScreenDelay);

        ((ExtendDisplayManager)getSystemService(Context.EXTEND_DISPLAY_SERVICE)).moveTo(this);
    }

    private Runnable rGoToExtendScreenDelay = new Runnable() {
		@Override
		public void run() {
			sendToExtendScreen();
		}
	};

    private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_SHOW:

				break;
            default:
            	mLogger.i("Unknown event, msg.what = " + msg.what + ".");
                break;
            }

            super.handleMessage(msg);
		}
    };

    private void doControlPlay() {
    	if (mViewInfoLst != null) {
    		for (MediaViewInfo info : mViewInfoLst) {
    			info.view.play();
    		}
    	}
    }

    private void doControlPause() {
    	if (mViewInfoLst != null) {
    		for (MediaViewInfo info : mViewInfoLst) {
    			info.view.pause();
    		}
    	}
    }

    private void doControlVisible() {
    	if (mViewInfoLst != null) {
    		for (MediaViewInfo info : mViewInfoLst) {
    			info.view.visible();
    		}
    	}
    }

    private void doControlGone() {
    	if (mViewInfoLst != null) {
    		for (MediaViewInfo info : mViewInfoLst) {
    			info.view.gone();
    		}
    	}
    }

    private class MediaReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Actions.MEDIAPLAYER_CONTROL_ACTION)) {
				int code = intent.getIntExtra(
						Actions.MEDIAPLAYER_CONTROL_ACTION_EXTRA_CODE, -1);
				switch (code) {
				case Constants.CONTROLCODE_PLAY:
					doControlPlay();

					break;
				case Constants.CONTROLCODE_PAUSE:
					doControlPause();

					break;
				case Constants.CONTROLCODE_VISIBLE:
					doControlVisible();

					break;
				case Constants.CONTROLCODE_GONE:
					doControlGone();

					break;
				default:
					mLogger.i("Control code is invalid, skip. code is " + code);
					break;
				}
			}
		}
    }

}
