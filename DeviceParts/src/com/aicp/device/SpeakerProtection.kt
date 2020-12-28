/*
* Copyright (C) 2020 The Android Ice Cold Project
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

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceManager

class SpeakerProtectionSwitch(context: Context) : OnPreferenceChangeListener {
    private val mContext: Context
    override fun onPreferenceChange(preference: Preference?, newValue: Any): Boolean {
        val enabled = newValue as Boolean
        Settings.System.putInt(mContext.getContentResolver(), SETTINGS_KEY, if (enabled) 1 else 0)
        Utils.setSystemProp("persist.vendor.audio.speaker.prot.enable", if (enabled) "true" else "false")
        return true
    }

    companion object {
        const val SETTINGS_KEY = DeviceSettings.KEY_SETTINGS_PREFIX + DeviceSettings.KEY_SPEAKER_PROTECTION
        fun isCurrentlyEnabled(context: Context?): Boolean {
            if(Utils.getSystemProp("persist.vendor.audio.speaker.prot.enable").equals("true"))
            {
                return true
            }else {
                return false
            }
        }
    }

    init {
        mContext = context
    }
}
