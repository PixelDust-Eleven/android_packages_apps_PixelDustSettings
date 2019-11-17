/*
 * Copyright (C) 2017-2020 The PixelDust Project
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

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.widget.Toast;

import androidx.preference.PreferenceCategory;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.android.internal.logging.nano.MetricsProto;

import com.pixeldust.settings.preferences.SystemSettingMasterSwitchPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class NotificationsSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String GAMING_MODE_ENABLED = "gaming_mode_enabled";

    private Preference mChargingLeds;
    private Preference mAlertSlider;
    private SystemSettingMasterSwitchPreference mGamingMode;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.pixeldust_settings_notifications);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        Preference LightOptions = findPreference("light_options");
        if (!getResources().getBoolean(R.bool.has_notification_light)) {
            prefScreen.removePreference(LightOptions);
        }

        mGamingMode = (SystemSettingMasterSwitchPreference) findPreference(GAMING_MODE_ENABLED);
        boolean gameEnabled = Settings.System.getInt(
        getActivity().getApplicationContext().getContentResolver(),
                Settings.System.GAMING_MODE_ENABLED, 0) == 1;
        updateGameModeEnabledUpdatePrefs(gameEnabled);
        mGamingMode.setOnPreferenceChangeListener(this);
    }

    private void updateGameModeEnabledUpdatePrefs(boolean gameEnabled) {
        mGamingMode.setChecked(gameEnabled);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mGamingMode) {
            boolean gameEnabled = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.GAMING_MODE_ENABLED, gameEnabled ? 1 : 0);
            updateGameModeEnabledUpdatePrefs(gameEnabled);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.PIXELDUST;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.pixeldust_settings_notifications;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}
