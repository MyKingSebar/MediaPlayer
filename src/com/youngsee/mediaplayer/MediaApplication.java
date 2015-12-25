package com.youngsee.mediaplayer;

import android.app.Application;
import android.provider.Settings;

public class MediaApplication extends Application {

    private static MediaApplication INSTANCE = null;

    private boolean mShowInExtendDisplay = false;

	@Override
    public void onCreate() {
        super.onCreate();

        INSTANCE = this;
    }

	public static MediaApplication getInstance() {
        return INSTANCE;
    }

	public boolean isDaulScreenMode() {
        return Settings.System.getInt(getContentResolver(), Settings.System.DUAL_SCREEN_MODE, 0) != 0;
    }

    public boolean isShowInExtendDisplay() {
        return mShowInExtendDisplay;
    }

    public void setShowInExtendDisplay(boolean flag) {
        mShowInExtendDisplay = flag;
    }

}
