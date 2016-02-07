/*
 * Copyright (C) 2015 The CyanogenMod Project
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

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.NetworkUtils;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;

import java.net.InetAddress;

public class AdbOverNetworkTile extends QSTile<QSTile.BooleanState> {
    private static final String ADB_TCP_PROPERTY = "service.adb.tcp.port";
    private static final int ADB_TCP_ON = 5555;
    private static final int ADB_TCP_OFF = -1;

    private boolean mListening;

    private static final Intent SETTINGS_DEVELOPMENT =
            new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    protected void handleClick() {
        // toggle adb network support
        toggleState();
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_network_adb_label);
    }

    private void toggleState() {
        setAdbNetwork(!isAdbNetworkEnabled());

        // restart service adbd
        SystemProperties.set("ctl.stop", "adbd");
        SystemProperties.set("ctl.start", "adbd");

        refreshState();
    }

    @Override
    protected void handleLongClick() {
        mHost.startActivityDismissingKeyguard(SETTINGS_DEVELOPMENT);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        //state.visible = isAdbEnabled();
        if (!isAdbEnabled()) {
            return;
        }
        state.value = isAdbNetworkEnabled();
        if (state.value) {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo != null) {
                // if wifiInfo is not null, set the label to "hostAddress"
                InetAddress address = NetworkUtils.intToInetAddress(wifiInfo.getIpAddress());
                state.label = address.getHostAddress();
            } else {
                // if wifiInfo is null, set the label without host address
                state.label = mContext.getString(R.string.qs_tile_network_adb_on);
            }
            state.icon = ResourceIcon.get(R.drawable.ic_qs_network_adb_on);
        } else {
            // Otherwise set the disabled label and icon
            state.label = mContext.getString(R.string.qs_tile_network_adb_off);
            state.icon = ResourceIcon.get(R.drawable.ic_qs_network_adb_off);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_PANEL;
    }

    private boolean isAdbEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Global.ADB_ENABLED, 0) > 0;
    }

    private boolean isAdbNetworkEnabled() {
        return SystemProperties.getInt(ADB_TCP_PROPERTY, ADB_TCP_OFF) != ADB_TCP_OFF;
    }

    private void setAdbNetwork(boolean on) {
        if (on) {
            SystemProperties.set(ADB_TCP_PROPERTY, Integer.toString(ADB_TCP_ON));
        } else {
            SystemProperties.set(ADB_TCP_PROPERTY, Integer.toString(ADB_TCP_OFF));
        }
    }

    public AdbOverNetworkTile(Host host) {
        super(host);
    }

    private ContentObserver mObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            refreshState();
        }
    };

    @Override
    public void setListening(boolean listening) {
        if (mListening != listening) {
            mListening = listening;
            if (listening) {
                mContext.getContentResolver().registerContentObserver(
                        Settings.Secure.getUriFor(Settings.Global.ADB_ENABLED),
                        false, mObserver);
            } else {
                mContext.getContentResolver().unregisterContentObserver(mObserver);
            }
        }
    }
}
