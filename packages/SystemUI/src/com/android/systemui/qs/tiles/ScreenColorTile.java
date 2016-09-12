/*
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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;

public class ScreenColorTile extends QSTile<QSTile.BooleanState> {
    private static final String COLOR_FILE = "/sys/devices/platform/kcal_ctrl.0/kcal";
    private static final String COLOR_MODE_PROPERTY = "screen.color_isday";
    private static final String COLOR_MODE_DAY_PROPERTY = "persist.screen.color_day";
    private static final String COLOR_MODE_NIGHT_PROPERTY = "persist.screen.color_night";
    private static final String COLOR_MODE_DEFAULT_VALUE = "255 255 255";

    public ScreenColorTile(Host host) {
        super(host);
    }

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
        toggleState();
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_fastcharge_label);
    }

    protected void toggleState() {
        boolean state = SystemProperties.getBoolean(COLOR_MODE_PROPERTY, true);

        if(FileUtils.writeLine(COLOR_FILE, 
            state ? SystemProperties.get(COLOR_MODE_NIGHT_PROPERTY, COLOR_MODE_DEFAULT_VALUE) : SystemProperties.get(COLOR_MODE_DAY_PROPERTY, COLOR_MODE_DEFAULT_VALUE))) {

            SystemProperties.set(COLOR_MODE_PROPERTY, state ? "false" : "true");
        }

        refreshState();
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = SystemProperties.getBoolean(COLOR_MODE_PROPERTY, true);
        state.icon = ResourceIcon.get(state.value ?
                R.drawable.ic_qs_screen_color_day : R.drawable.ic_qs_screen_color_night);
        state.label = mContext.getString(state.value
                ? R.string.qs_tile_screen_color_day : R.string.qs_tile_screen_color_night);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_PANEL;
    }

    @Override
    public void setListening(boolean listening) {
    }
}
