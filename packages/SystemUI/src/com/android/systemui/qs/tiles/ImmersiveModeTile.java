/*
 * Copyright (C) 2016 nAOSProm
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

package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.WindowManagerGlobal;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.qs.QSTile;
import com.android.systemui.R;

/** Quick settings tile: ImmersiveMode **/
public class ImmersiveModeTile extends QSTile<QSTile.BooleanState> {
    private static final int IMMERSIVE_MODE_OFF = 0;
    private static final int IMMERSIVE_MODE_FULL = 1;
    private static final int IMMERSIVE_MODE_HIDE_NAVIGATION = 2;
    private static final int IMMERSIVE_MODE_HIDE_STATUS_BAR = 3;

    private static final String [] POLICY_CONTROL_VALUES = {
        null, "immersive.full=*", "immersive.navigation=*", "immersive.status=*"
    };

    private int mActiveImmersiveMode = 0;
    private long mLastClickTime = -1;
    private ContentObserver mContentObserver;
    private boolean mModeChanged = false;
    private boolean mHasNavigationBar;

    public ImmersiveModeTile(Host host) {
        super(host);
        init();
    }

    private void init() {
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri setting = Settings.Global.getUriFor(Settings.Global.POLICY_CONTROL);
        mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                updateActiveImmersiveMode();
            }

            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }
        };
        try {
            mHasNavigationBar = WindowManagerGlobal.getWindowManagerService().hasNavigationBar();
        } catch (RemoteException ex) {
        }
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_PANEL;
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_immersive_mode_label);
    }

    @Override
    public void setListening(boolean listening) {
        ContentResolver contentResolver = mContext.getContentResolver();
        if (listening) {
            Uri setting = Settings.Global.getUriFor(Settings.Global.POLICY_CONTROL);
            contentResolver.registerContentObserver(setting, false, mContentObserver);
        } else {
            contentResolver.unregisterContentObserver(mContentObserver);
            if (mModeChanged) {
                String policyControl = POLICY_CONTROL_VALUES[mActiveImmersiveMode];
                Settings.Global.putString(mContext.getContentResolver(),  Settings.Global.POLICY_CONTROL, policyControl);
                mModeChanged = false;
            }
        }
    }

    @Override
    public void handleClick() {
        boolean wasEnabled = (Boolean) mState.value;
        if (!wasEnabled) {
            mActiveImmersiveMode = IMMERSIVE_MODE_FULL;
        } else if (mHasNavigationBar && SystemClock.elapsedRealtime() - mLastClickTime < 5000) {
            mActiveImmersiveMode = (mActiveImmersiveMode + 1) % 4;
        } else {
            mActiveImmersiveMode = IMMERSIVE_MODE_OFF;
        }
        mModeChanged = true;
        mLastClickTime = SystemClock.elapsedRealtime();
        refreshState();
    }
    
    private void updateActiveImmersiveMode() {
        String policyControl = Settings.Global.getString(mContext.getContentResolver(),
                        Settings.Global.POLICY_CONTROL);
        mActiveImmersiveMode = IMMERSIVE_MODE_OFF;
        if (policyControl != null) {
            for(int i = 1; i < 4; i++)    {
                if (POLICY_CONTROL_VALUES[i].equals(policyControl)) {
                    mActiveImmersiveMode = mHasNavigationBar ? i : 1;
                    break;
                }
            }
        }
        mModeChanged = false;
        refreshState();  
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = (mActiveImmersiveMode != IMMERSIVE_MODE_OFF);
        switch (mActiveImmersiveMode) {
        case IMMERSIVE_MODE_OFF:
            state.label = mContext.getString(R.string.quick_settings_immersive_mode_label);
            state.icon = ResourceIcon.get(R.drawable.ic_qs_immersive_off);
            state.contentDescription =  mContext.getString(R.string.qs_tile_immersive_mode_off);
            break;
        case IMMERSIVE_MODE_FULL:
            state.label = mContext.getString(R.string.qs_tile_immersive_mode_full_screen);
            state.icon = ResourceIcon.get(R.drawable.ic_qs_immersive_full);
            state.contentDescription =  mContext.getString(R.string.qs_tile_immersive_mode_full_screen);
            break;
        case IMMERSIVE_MODE_HIDE_NAVIGATION:
            state.label = mContext.getString(R.string.qs_tile_immersive_mode_navbar_hidden);
            state.icon = ResourceIcon.get(R.drawable.ic_qs_immersive_nav);
            state.contentDescription =  mContext.getString(R.string.qs_tile_immersive_mode_navbar_hidden);
            break;
        case IMMERSIVE_MODE_HIDE_STATUS_BAR:
            state.label = mContext.getString(R.string.qs_tile_immersive_mode_status_hidden);
            state.icon = ResourceIcon.get(R.drawable.ic_qs_immersive_status);
            state.contentDescription =  mContext.getString(R.string.qs_tile_immersive_mode_status_hidden);
            break;
        }
    }
}
