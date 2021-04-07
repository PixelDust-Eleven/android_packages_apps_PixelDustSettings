/*
 * Copyright (C) 2021 AOSiP
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

package com.pixeldust.settings.fragments;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.settings.widget.SwitchWidgetController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class GamingModeEnabler implements SwitchWidgetController.OnSwitchChangeListener,
        LifecycleObserver, OnResume, OnPause {

    private final Context mContext;
    private final SwitchWidgetController mSwitchWidgetController;
    private final OnGamingModeChangeListener mGamingModeListener;

    GamingModeEnabler(Context context, SwitchWidgetController switchWidgetController,
            OnGamingModeChangeListener listener, Lifecycle lifecycle) {

        mContext = context;
        mSwitchWidgetController = switchWidgetController;

        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }

        final boolean enabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.GAMING_MODE_ACTIVE, 0, UserHandle.USER_CURRENT) != 0;
        mSwitchWidgetController.setChecked(enabled);
        mSwitchWidgetController.setupView();

        mGamingModeListener = listener;
        mSwitchWidgetController.setListener(this);
    }

    @Override
    public void onPause() {
        try {
            mSwitchWidgetController.stopListening();
        } catch (IllegalStateException e) {
            // nothing to report here
        }
    }

    @Override
    public void onResume() {
        try {
            mSwitchWidgetController.startListening();
        } catch (IllegalStateException e) {
            // nothing to report here
        }
    }

    @Override
    public boolean onSwitchToggled(boolean isChecked) {
        final boolean succeed = Settings.System.putIntForUser(mContext.getContentResolver(),
                Settings.System.GAMING_MODE_ACTIVE, isChecked ? 1 : 0, UserHandle.USER_CURRENT);
        if (succeed && mGamingModeListener != null) {
            mGamingModeListener.onChanged(isChecked);
        }
        return succeed;
    }

    public void teardownSwitchController() {
        try {
            mSwitchWidgetController.stopListening();
        } catch (IllegalStateException e) {
            // nothing to report here
        }
        mSwitchWidgetController.teardownView();
    }

    public interface OnGamingModeChangeListener {
        void onChanged(boolean enabled);
    }
}
