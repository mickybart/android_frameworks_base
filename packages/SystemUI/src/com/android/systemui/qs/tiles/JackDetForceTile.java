/*
 * Copyright (C) 2016 nAOSP ROM
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
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;

public class JackDetForceTile extends QSTile<QSTile.BooleanState> {
    private static final String DET_FORCE_FILE = "/sys/devices/platform/msm_ssbi.0/pm8058-core/simple_remote_pf/simple_remote/det_force";
    private static final String JACK_BROKEN_PROPERTY = "persist.sys.jack_broken";

    private final AnimationIcon mEnable
	                = new AnimationIcon(R.drawable.ic_signal_headphone_enable_animation,
					            R.drawable.ic_signal_headphone_enable);
    private final AnimationIcon mDisable
                        = new AnimationIcon(R.drawable.ic_signal_headphone_disable_animation,
					            R.drawable.ic_signal_headphone_disable);

    public JackDetForceTile(Host host) {
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
        return mContext.getString(R.string.quick_settings_jackdetforce_label);
    }

    protected void toggleState() {
        String detForceState = FileUtils.readOneLine(DET_FORCE_FILE);
        if (detForceState != null) {
            boolean state = detForceState.contentEquals("1");
            FileUtils.writeLine(DET_FORCE_FILE, state ? "0" : "1");
        }
        refreshState();
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
	state.label = mHost.getContext().getString(R.string.quick_settings_jackdetforce_label);
        if (!SystemProperties.getBoolean(JACK_BROKEN_PROPERTY, false)) {
            Drawable icon = mHost.getContext().getDrawable(R.drawable.ic_signal_headphone_disable)
                    .mutate();
            final int disabledColor = mHost.getContext().getColor(R.color.qs_tile_tint_unavailable);
            icon.setTint(disabledColor);
            state.icon = new DrawableIcon(icon);
            state.label = new SpannableStringBuilder().append(state.label,
                    new ForegroundColorSpan(disabledColor),
                    SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
            return;
        }

        String detForceState = FileUtils.readOneLine(DET_FORCE_FILE);
        boolean curValue = (detForceState != null) && (detForceState.contentEquals("1"));

	if (arg instanceof Boolean && state.value == curValue)
	    return;

        state.value = curValue;
        final AnimationIcon icon = state.value ? mEnable : mDisable;
        state.icon = icon;
        state.label = mContext.getString(state.value
                ? R.string.qs_tile_jackdetforce_on : R.string.qs_tile_jackdetforce_off);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_PANEL;
    }

    @Override
    public void setListening(boolean listening) {
    }
}
