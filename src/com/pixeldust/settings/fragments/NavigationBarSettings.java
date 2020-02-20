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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
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

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.pixeldust.PixeldustUtils;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class NavigationBarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String GESTURE_SYSTEM_NAVIGATION = "gesture_system_navigation";
    private static final String PIXEL_NAV_ANIMATION = "pixel_nav_animation";
    private static final String LAYOUT_SETTINGS = "navbar_layout_views";
    private static final String NAVIGATION_BAR_INVERSE = "navbar_inverse_layout";
    private static final String NAVBAR_VISIBILITY = "navbar_visibility";
    private static final String NAVIGATION_BAR_KEYS = "navigation_bar_menu_arrow_keys";

    private SwitchPreference mNavbarVisibility;
    private Preference mGestureSystemNavigation;
    private SwitchPreference mPixelNavAnimation;
    private Preference mLayoutSettings;
    private SwitchPreference mSwapNavButtons;
    private Preference mNavbarArrowKeys;

    private boolean mIsNavSwitchingMode = false;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.pixeldust_settings_navigation);
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mGestureSystemNavigation = (Preference) findPreference(GESTURE_SYSTEM_NAVIGATION);
        mPixelNavAnimation = (SwitchPreference) findPreference(PIXEL_NAV_ANIMATION);
        mLayoutSettings = (Preference) findPreference(LAYOUT_SETTINGS);
        mSwapNavButtons = (SwitchPreference) findPreference(NAVIGATION_BAR_INVERSE);
        mNavbarArrowKeys = (Preference) findPreference(NAVIGATION_BAR_KEYS);

        if (!PixeldustUtils.isThemeEnabled("com.android.internal.systemui.navbar.threebutton")) {
            prefScreen.removePreference(mLayoutSettings);
        }

        if (PixeldustUtils.isThemeEnabled("com.android.internal.systemui.navbar.threebutton")) {
            mGestureSystemNavigation.setSummary(getString(R.string.legacy_navigation_title));
        } else if (PixeldustUtils.isThemeEnabled("com.android.internal.systemui.navbar.twobutton")) {
            mGestureSystemNavigation.setSummary(getString(R.string.swipe_up_to_switch_apps_title));
        } else {
            mGestureSystemNavigation.setSummary(getString(R.string.edge_to_edge_navigation_title));
            prefScreen.removePreference(mPixelNavAnimation);
            prefScreen.removePreference(mSwapNavButtons);
            prefScreen.removePreference(mNavbarArrowKeys);
        }

        mNavbarVisibility = (SwitchPreference) findPreference(NAVBAR_VISIBILITY);

        boolean defaultToNavigationBar = PixeldustUtils.deviceSupportNavigationBar(getActivity());
        boolean showing = Settings.System.getInt(getContentResolver(),
                Settings.System.FORCE_SHOW_NAVBAR,
                defaultToNavigationBar ? 1 : 0) != 0;
        updateBarVisibleAndUpdatePrefs(showing);
        mNavbarVisibility.setOnPreferenceChangeListener(this);

        mHandler = new Handler();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNavbarVisibility)) {
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            boolean showing = ((Boolean)newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.FORCE_SHOW_NAVBAR,
                    showing ? 1 : 0);
            updateBarVisibleAndUpdatePrefs(showing);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                }
            }, 1500);
            return true;
        }
        return false;
    }

    private void updateBarVisibleAndUpdatePrefs(boolean showing) {
        mNavbarVisibility.setChecked(showing);
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
                    sir.xmlResId = R.xml.pixeldust_settings_navigation;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}
