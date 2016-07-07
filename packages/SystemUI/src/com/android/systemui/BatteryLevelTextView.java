/*
 * Copyright (C) 2014 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui;

import com.android.systemui.statusbar.policy.BatteryController;

import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.util.Log;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import java.text.NumberFormat;

public class BatteryLevelTextView extends TextView implements
        BatteryController.BatteryStateChangeCallback {
    static final String TAG = "BatteryLevelTextView";
    private BatteryController mBatteryController;

    private boolean mBatteryCharging;
    private boolean mForceShow;
    private boolean mAttached;
    private int mRequestedVisibility;
    private int mPercentMode;
    private int mLevel;    
    private final SettingObserver mSettingObserver = new SettingObserver();

    public BatteryLevelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // setBatteryStateRegistar (if called) will made the view visible and ready to be hidden
        // if the view shouldn't be displayed. Otherwise this view should be hidden from start.
        mRequestedVisibility = GONE;
    }

    public void setForceShow(boolean forceShow) {
        mForceShow = forceShow;
        updateVisibility();
    }

    public void setBatteryController(BatteryController batteryController) {
        Log.i(TAG, "setBatteryController");
        mRequestedVisibility = VISIBLE;
        mBatteryController = batteryController;
        if (mAttached) {
            batteryController.addStateChangedCallback(this);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        mRequestedVisibility = visibility;
        updateVisibility();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Respect font size setting.
        setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelSize(R.dimen.battery_level_text_size));
    }
    
    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }
    
    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        if (mLevel != level) {
            mLevel = level;
            String percentage = NumberFormat.getPercentInstance().format((double) level / 100.0);
            setText(percentage);
        }
        if (mBatteryCharging != charging) {
            mBatteryCharging = charging;
            updateVisibility();
        }
    }

    @Override
    public void onPowerSaveChanged() {
        // Not used
    }

    @Override
    public void onAttachedToWindow() {
        Log.i(TAG, "onAttachedToWindow");
        super.onAttachedToWindow();

        if (mBatteryController != null) {
            mBatteryController.addStateChangedCallback(this);
        }
        getContext().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT),
                false, mSettingObserver);
        updatePercentMode();

        mAttached = true;
    }

    @Override
    public void onDetachedFromWindow() {
        Log.i(TAG, "onDetachedFromWindow");
        super.onDetachedFromWindow();
        mAttached = false;

        if (mBatteryController != null) {
            mBatteryController.removeStateChangedCallback(this);
        }
        getContext().getContentResolver().unregisterContentObserver(mSettingObserver);
    }

    private void updateVisibility() {
        boolean showPercent = mPercentMode == BatteryController.PERCENTAGE_MODE_OUTSIDE
                || mBatteryCharging;
        if (mBatteryController != null && (showPercent || mForceShow)) {
            super.setVisibility(mRequestedVisibility);
        } else {
            super.setVisibility(GONE);
        }
    }

    private void updatePercentMode() {
        mPercentMode = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        updateVisibility();
    }

    private final class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            updatePercentMode();
        }
    }
}
