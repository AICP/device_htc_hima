/*
* Copyright (C) 2016 The OmniROM Project
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
package com.aicp.device;

import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;

public class DeviceSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String KEY_VIBSTRENGTH = "vib_strength";

    public static final String KEY_SETTINGS_PREFIX = "device_setting_";

    private VibratorStrengthPreference mVibratorStrength;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main, rootKey);

        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength != null) {
            mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());
        }

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }
}
