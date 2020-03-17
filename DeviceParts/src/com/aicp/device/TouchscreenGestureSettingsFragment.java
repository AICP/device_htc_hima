/*
 * Copyright (C) 2016 The CyanogenMod Project
 *               2017 The LineageOS Project
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

package com.aicp.device;

import android.app.ActionBar;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;
import static com.aicp.device.HtcGestureService.KEY_GESTURE_HAPTIC_FEEDBACK;
import static com.aicp.device.HtcGestureService.GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME;

public class TouchscreenGestureSettingsFragment extends PreferenceFragment {

private TwoStatePreference mOffscreenGestureFeedbackSwitch;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.gesture_panel);
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mOffscreenGestureFeedbackSwitch = (TwoStatePreference) findPreference(com.aicp.device.HtcGestureService.KEY_GESTURE_HAPTIC_FEEDBACK);
        mOffscreenGestureFeedbackSwitch.setChecked(Settings.System.getInt(getContext().getContentResolver(),
                "Settings.System."+com.aicp.device.HtcGestureService.GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, 1) != 0);
    }
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
       if (preference == mOffscreenGestureFeedbackSwitch) {
            Settings.System.putInt(getContext().getContentResolver(),
                    "Settings.System."+com.aicp.device.HtcGestureService.GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, mOffscreenGestureFeedbackSwitch.isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }
}
