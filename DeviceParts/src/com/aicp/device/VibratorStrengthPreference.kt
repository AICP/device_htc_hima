/*
* Copyright (C) 2016 The OmniROM Project
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

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Bundle
import android.os.Vibrator
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.aicp.device.Utils.fileWritable
import com.aicp.device.Utils.getFileValueVibrator
import com.aicp.device.Utils.writeValue

class VibratorStrengthPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs), SeekBar.OnSeekBarChangeListener {
    private var mSeekBar: SeekBar? = null
    private var mOldStrength = 0
    private val mMinValue = 1200
    private val mMaxValue = 3200
    private val mVibrator: Vibrator
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        mOldStrength = getValue(getContext()).toInt()
        mSeekBar = holder.findViewById(R.id.seekbar) as SeekBar
        mSeekBar!!.setMax(mMaxValue - mMinValue)
        mSeekBar!!.setProgress(mOldStrength - mMinValue)
        mSeekBar!!.setOnSeekBarChangeListener(this)
    }

    private fun setValue(newValue: String, withFeedback: Boolean) {
        writeValue(FILE_LEVEL, newValue)
        Settings.System.putString(getContext().getContentResolver(), SETTINGS_KEY, newValue)
        if (withFeedback) {
            mVibrator.vibrate(testVibrationPattern, -1)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int,
                          fromTouch: Boolean) {
        setValue((progress + mMinValue).toString(), true)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        // NA
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        // NA
    }

    companion object {
        private const val DEBUG = false
        private const val TAG = "VibratorStrengthPreference"
        private const val FILE_LEVEL = "/sys/devices/virtual/timed_output/vibrator/voltage_level"
        private val testVibrationPattern = longArrayOf(0, 250)
        const val SETTINGS_KEY = DeviceSettings.KEY_SETTINGS_PREFIX + DeviceSettings.KEY_VIBSTRENGTH
        const val DEFAULT_VALUE = "2008"
        val isSupported: Boolean
            get() = fileWritable(FILE_LEVEL)

        fun getValue(context: Context?): String {
            Log.i(TAG, "reading sysfs file: $FILE_LEVEL")
            return getFileValueVibrator(FILE_LEVEL, DEFAULT_VALUE)
        }

        @JvmStatic
        fun restore(context: Context) {
            if (!isSupported) {
                return
            }
            var storedValue: String = Settings.System.getString(context.getContentResolver(), SETTINGS_KEY)?: DEFAULT_VALUE
            if (DEBUG) {
                Log.d(TAG, "restore value:$storedValue")
                Log.d(TAG, "restore file:$FILE_LEVEL")
            }
            writeValue(FILE_LEVEL, storedValue)
        }
    }

    init {
        // from drivers/platform/msm/qpnp-haptic.c
        // #define QPNP_HAP_VMAX_MIN_MV		116
        // #define QPNP_HAP_VMAX_MAX_MV		7308
        mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        setLayoutResource(R.layout.preference_seek_bar)
    }
}