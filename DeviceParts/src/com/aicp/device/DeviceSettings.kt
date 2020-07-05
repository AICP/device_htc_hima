/*
* Copyright (C) 2016 The OmniROM Project
* Copyright (C) 2020 Android Ice Cold Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.aicp.device

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.TwoStatePreference

class DeviceSettings : PreferenceFragment(), Preference.OnPreferenceChangeListener {
    private var mVibratorStrength: VibratorStrengthPreference? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main, rootKey)
        mVibratorStrength = findPreference(KEY_VIBSTRENGTH) as VibratorStrengthPreference?
        if (mVibratorStrength != null) {
            mVibratorStrength!!.setEnabled(VibratorStrengthPreference.Companion.isSupported)
        }
        mFastChargeSwitch = findPreference(KEY_FASTCHARGE) as TwoStatePreference?
        mFastChargeSwitch!!.setEnabled(FastChargeSwitch.Companion.isSupported)
        mFastChargeSwitch!!.setChecked(FastChargeSwitch.Companion.isCurrentlyEnabled(this.getContext()))
        mFastChargeSwitch!!.setOnPreferenceChangeListener(FastChargeSwitch(getContext()))
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return super.onPreferenceTreeClick(preference)
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return true
    }

    companion object {
        const val KEY_VIBSTRENGTH = "vib_strength"
        const val KEY_FASTCHARGE = "fastcharge"
        const val KEY_BACKLIGHT_DIMMER = "backlight_dimmer"
        const val KEY_HEADPHONE_GAIN = "headphone_gain"
        const val KEY_SETTINGS_PREFIX = "device_setting_"
        private var mFastChargeSwitch: TwoStatePreference? = null
    }
}
