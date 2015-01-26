/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.server.lights;

import android.os.SystemProperties;

public abstract class LightsManager {
    public static final int LIGHT_ID_BUTTONS_DISABLE = 0;
    public static final int LIGHT_ID_BUTTONS_TIMEOUT = 1;
    public static final int LIGHT_ID_BUTTONS_LINKED = 2;
    protected int mButtonsLightMode;

    public static final int LIGHT_ID_BACKLIGHT = 0;
    public static final int LIGHT_ID_KEYBOARD = 1;
    public static final int LIGHT_ID_BUTTONS = 2;
    public static final int LIGHT_ID_BATTERY = 3;
    public static final int LIGHT_ID_NOTIFICATIONS = 4;
    public static final int LIGHT_ID_ATTENTION = 5;
    public static final int LIGHT_ID_BLUETOOTH = 6;
    public static final int LIGHT_ID_WIFI = 7;
    public static final int LIGHT_ID_COUNT = 8;

    public LightsManager() {
        //Set the LIGHT_ID_BUTTONS mode
        mButtonsLightMode = SystemProperties.getInt("sys.lightbar.mode", LIGHT_ID_BUTTONS_TIMEOUT);
        if (mButtonsLightMode < LIGHT_ID_BUTTONS_DISABLE || 
                   mButtonsLightMode > LIGHT_ID_BUTTONS_LINKED) {
            mButtonsLightMode = LIGHT_ID_BUTTONS_TIMEOUT;
        }
    }

    public abstract Light getLight(int id);

    public boolean isButtonsLightTimeout() {
        return mButtonsLightMode == LIGHT_ID_BUTTONS_TIMEOUT;
    }

    public boolean isButtonsLightLinked() {
        return mButtonsLightMode == LIGHT_ID_BUTTONS_LINKED;
    }
}
