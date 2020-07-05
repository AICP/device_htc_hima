/*
 * Copyright (C) 2016 The CyanogenMod Project
 *               2017 The LineageOS Project
 *               2020 Android Ice Cold Project
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
package com.aicp.device

import android.app.ActionBar
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.TwoStatePreference

class TouchscreenGestureSettingsFragment : PreferenceFragment() {
    private var mOffscreenGestureFeedbackSwitch: TwoStatePreference? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.gesture_panel)
        val actionBar: ActionBar = getActivity().getActionBar()
        actionBar.setDisplayHomeAsUpEnabled(true)
        mOffscreenGestureFeedbackSwitch = findPreference(HtcGestureService.KEY_GESTURE_HAPTIC_FEEDBACK) as TwoStatePreference?
        mOffscreenGestureFeedbackSwitch!!.setChecked(Settings.System.getInt(getContext().getContentResolver(),
                "Settings.System." + HtcGestureService.GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, 1) !== 0)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference === mOffscreenGestureFeedbackSwitch) {
            Settings.System.putInt(getContext().getContentResolver(),
                    "Settings.System." + HtcGestureService.GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME, if (mOffscreenGestureFeedbackSwitch!!.isChecked()) 1 else 0)
            return true
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() === android.R.id.home) {
            getActivity().onBackPressed()
            return true
        }
        return false
    }
}
