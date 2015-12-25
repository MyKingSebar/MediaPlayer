package com.youngsee.mediaplayer.view;

import java.io.IOException;

import com.youngsee.mediaplayer.R;
import com.youngsee.mediaplayer.common.Constants;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MultiMediaView extends MediaView {

	private final int EVENT_BASE = 0x9000;
	private final int EVENT_PLAY = EVENT_BASE + 0;
	private final int EVENT_PAUSE = EVENT_BASE + 1;
	private final int EVENT_VISIBLE = EVENT_BASE + 2;
	private final int EVENT_GONE = EVENT_BASE + 3;

	private SurfaceView mSurfaceView = null;

	private SurfaceHolder mSurfaceHolder = null;

	private MediaPlayer mMediaPlayer = null;

	private int mMediaPosition = -1;

	private AssetFileDescriptor mFdTestMedia = null;

	public MultiMediaView(Context context) {
		super(context);

		init(context);
	}
	
	private void init(Context context) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_multimedia, this);

		mSurfaceView = (SurfaceView)findViewById(R.id.multimedia_surfaceview);
		mSurfaceView.setVisibility(View.VISIBLE);
		
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		mSurfaceHolder.addCallback(new SurfaceHolderCallBack());

		try {
			mFdTestMedia = context.getAssets().openFd(Constants.TESTMEDIA);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private final class SurfaceHolderCallBack implements SurfaceHolder.Callback {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			mLogger.i("Surface is changed.");
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mLogger.i("Surface is created.");
			createPlayer();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			mLogger.i("Surface is destroyed.");
			if (mMediaPlayer != null) {
				mMediaPosition = mMediaPlayer.getCurrentPosition();
			}
			releasePlayer();
		}
		
	}
	
	private MediaPlayer.OnPreparedListener mMediaPlayerPreparedListener =
			new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
        	startPlayer();
        }
    };
    
    private MediaPlayer.OnCompletionListener mMediaPlayerCompletionListener =
    		new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
        	mLogger.i("Media player completion.");
        	startPlayer();
        }
    };
    
    private MediaPlayer.OnErrorListener mMediaPlayerErrorListener =
    		new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
        	mLogger.i("Media player error.");
        	releasePlayer();

            return true;
        }
    };

	@Override
	public void onPause() {

	}

	@Override
	public void onResume() {

	}

	@Override
	public void onDestroy() {
		cleanupMsg();

		releasePlayer();
	}

	private void createPlayer() {
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
	        mMediaPlayer.setOnPreparedListener(mMediaPlayerPreparedListener);
	        mMediaPlayer.setOnCompletionListener(mMediaPlayerCompletionListener);
	        mMediaPlayer.setOnErrorListener(mMediaPlayerErrorListener);
		} else {
			mMediaPlayer.reset();
		}

        try {
			mMediaPlayer.setDataSource(mFdTestMedia.getFileDescriptor(),
					mFdTestMedia.getStartOffset(), mFdTestMedia.getLength());
	        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startPlayer() {
		if (mMediaPlayer != null) {
			if (mMediaPosition != -1) {
	    		mMediaPlayer.seekTo(mMediaPosition);
	    		mMediaPosition = -1;
	    	}
	    	mMediaPlayer.start();
		}
	}

	private void pausePlayer() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
	}

	private void releasePlayer() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
	}

	private void cleanupMsg() {
    	mHandler.removeMessages(EVENT_PLAY);
    	mHandler.removeMessages(EVENT_PAUSE);
    	mHandler.removeMessages(EVENT_VISIBLE);
    	mHandler.removeMessages(EVENT_GONE);
    }

	@Override
	public void play() {
		mHandler.sendEmptyMessage(EVENT_PLAY);
	}

	@Override
	public void pause() {
		mHandler.sendEmptyMessage(EVENT_PAUSE);
	}

	@Override
	public void visible() {
		mHandler.sendEmptyMessage(EVENT_VISIBLE);
    }

	@Override
	public void gone() {
		mHandler.sendEmptyMessage(EVENT_GONE);
	}

	private void doPlay() {
		startPlayer();
	}

	private void doPause() {
		pausePlayer();
	}

	private void doVisible() {
		mSurfaceView.setVisibility(View.VISIBLE);
	}

	private void doGone() {
		mSurfaceView.setVisibility(View.GONE);
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_PLAY:
				doPlay();

				break;
			case EVENT_PAUSE:
				doPause();

				break;
			case EVENT_VISIBLE:
				doVisible();

				break;
			case EVENT_GONE:
				doGone();

				break;
            default:
            	mLogger.i("Unknown event, msg.what = " + msg.what + ".");
                break;
            }

            super.handleMessage(msg);
		}
    };

}
