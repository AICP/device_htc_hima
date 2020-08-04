/*
 * Copyright (C) 2014 SlimRoms Project
 *               2016 The CyanogenMod Project
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

import android.app.Service
import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.hardware.SensorEvent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.TorchCallback
import android.media.AudioManager
import android.os.*
import android.os.PowerManager.WakeLock
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.core.content.ContextCompat.getSystemService
import com.aicp.device.GestureMotionSensor.Companion.getInstance
import com.aicp.device.GestureMotionSensor.GestureMotionSensorListener
import com.android.internal.util.aicp.AicpVibe


class HtcGestureService : Service() {
    private var mContext: Context? = null
    private var mGestureSensor: GestureMotionSensor? = null
    private var mPowerManager: PowerManager? = null
    private var mSensorWakeLock: WakeLock? = null
    private var mCameraManager: CameraManager? = null
    private var mTorchCameraId: String? = null
    private var mTorchEnabled = false
    private var mAudioManager: AudioManager? = null
    private var mVibrator: Vibrator? = null
    private var mSwipeUpAction = 0
    private var mSwipeDownAction = 0
    private var mSwipeLeftAction = 0
    private var mSwipeRightAction = 0
    private val mListener: GestureMotionSensorListener = object : GestureMotionSensorListener {
        override fun onEvent(type: Int, event: SensorEvent?) {
            if (DEBUG) Log.d(
                TAG,
                "Received event: $type"
            )
            when (type) {
                GestureMotionSensor.SENSOR_GESTURE_DOUBLE_TAP -> doDoubleTapToWake()
                GestureMotionSensor.SENSOR_GESTURE_SWIPE_UP, GestureMotionSensor.SENSOR_GESTURE_SWIPE_DOWN, GestureMotionSensor.SENSOR_GESTURE_SWIPE_LEFT, GestureMotionSensor.SENSOR_GESTURE_SWIPE_RIGHT -> handleGestureAction(
                    gestureToAction(type)
                )
                GestureMotionSensor.SENSOR_GESTURE_CAMERA -> handleCameraActivation()
            }
        }
    }

    override fun onCreate() {
        if (DEBUG) Log.d(
            TAG,
            "Creating service"
        )
        super.onCreate()
        mContext = this
        mGestureSensor = getInstance(mContext as HtcGestureService)
        mGestureSensor!!.registerListener(mListener)
        val sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(mContext)
        loadPreferences(sharedPrefs)
        sharedPrefs.registerOnSharedPreferenceChangeListener(mPrefListener)
        mPowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        mSensorWakeLock =
            mPowerManager!!.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HtcGestureWakeLock")
        mCameraManager =
            (mContext as HtcGestureService).getSystemService(Context.CAMERA_SERVICE) as CameraManager
        mCameraManager!!.registerTorchCallback(mTorchCallback, null)
        mTorchCameraId = torchCameraId
        mAudioManager =
                (mContext as HtcGestureService).getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mVibrator = (mContext as HtcGestureService).getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (DEBUG) Log.d(
            TAG,
            "Starting service"
        )
        val screenStateFilter = IntentFilter(Intent.ACTION_SCREEN_ON)
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF)
        mContext!!.registerReceiver(mScreenStateReceiver, screenStateFilter)
        return START_STICKY
    }

    override fun onDestroy() {
        if (DEBUG) Log.d(
            TAG,
            "Destroying service"
        )
        super.onDestroy()
        unregisterReceiver(mScreenStateReceiver)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun onDisplayOn() {
        if (DEBUG) Log.d(
            TAG,
            "Display on"
        )
        if (isDoubleTapEnabled) {
            mGestureSensor!!.disableGesture(GestureMotionSensor.SENSOR_GESTURE_DOUBLE_TAP)
        }
        if (mSwipeUpAction != ACTION_NONE) {
            mGestureSensor!!.disableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_UP)
        }
        if (mSwipeDownAction != ACTION_NONE) {
            mGestureSensor!!.disableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_DOWN)
        }
        if (mSwipeLeftAction != ACTION_NONE) {
            mGestureSensor!!.disableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_LEFT)
        }
        if (mSwipeRightAction != ACTION_NONE) {
            mGestureSensor!!.disableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_RIGHT)
        }
        mGestureSensor!!.stopListening()
    }

    private fun onDisplayOff() {
        if (DEBUG) Log.d(
            TAG,
            "Display off"
        )
        if (isDoubleTapEnabled) {
            mGestureSensor!!.enableGesture(GestureMotionSensor.SENSOR_GESTURE_DOUBLE_TAP)
        }
        if (mSwipeUpAction != ACTION_NONE) {
            mGestureSensor!!.enableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_UP)
        }
        if (mSwipeDownAction != ACTION_NONE) {
            mGestureSensor!!.enableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_DOWN)
        }
        if (mSwipeLeftAction != ACTION_NONE) {
            mGestureSensor!!.enableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_LEFT)
        }
        if (mSwipeRightAction != ACTION_NONE) {
            mGestureSensor!!.enableGesture(GestureMotionSensor.SENSOR_GESTURE_SWIPE_RIGHT)
        }
        mGestureSensor!!.beginListening()
    }

    private val isDoubleTapEnabled: Boolean
        get() = Settings.Secure.getInt(
            mContext!!.contentResolver,
            Settings.Secure.DOUBLE_TAP_TO_WAKE, 0
        ) != 0

    private fun handleGestureAction(action: Int) {
        if (DEBUG) Log.d(
            TAG,
            "Performing gesture action: $action"
        )
        when (action) {
            ACTION_CAMERA -> handleCameraActivation()
            ACTION_TORCH -> handleFlashlightActivation()
            ACTION_WAKE_DISPLAY -> handleWakeDisplay()
            ACTION_NONE -> {
            }
            else -> {
            }
        }
    }

    private fun doDoubleTapToWake() {
        doHapticFeedback()
        mPowerManager!!.wakeUp(SystemClock.uptimeMillis())
    }

    private fun handleCameraActivation() {
        doHapticFeedback()
        launchCamera()
    }

    private fun handleFlashlightActivation() {
        doHapticFeedback()
        launchFlashlight()
    }

    private fun handleWakeDisplay() {
        doHapticFeedback()
        wakeDisplay()
    }

    private fun launchCamera() {
        wakeDisplay()
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
        try {
            mContext!!.startActivityAsUser(intent, null, UserHandle(UserHandle.USER_CURRENT))
        } catch (e: ActivityNotFoundException) {
            /* Ignore */
        }
    }

    private fun wakeDisplay() {
        mSensorWakeLock!!.acquire(SENSOR_WAKELOCK_DURATION.toLong())
        mPowerManager!!.wakeUp(SystemClock.uptimeMillis())
    }

    private fun launchFlashlight() {
        mSensorWakeLock!!.acquire(SENSOR_WAKELOCK_DURATION.toLong())
        mPowerManager!!.wakeUp(SystemClock.uptimeMillis())
        try {
            mCameraManager!!.setTorchMode(mTorchCameraId!!, !mTorchEnabled)
        } catch (e: CameraAccessException) {
            // Ignore
        }
    }

    private fun doHapticFeedback() {
        AicpVibe.performHapticFeedbackLw(
            HapticFeedbackConstants.LONG_PRESS,
            false,
            mContext,
            GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME,
            GESTURE_HAPTIC_DURATION
        )
    }

    // Ignore
    private val torchCameraId: String?
        get() {
            try {
                for (id in mCameraManager!!.cameraIdList) {
                    val cc = mCameraManager!!.getCameraCharacteristics(id)
                    val direction = cc.get(CameraCharacteristics.LENS_FACING)!!
                    if (direction == CameraCharacteristics.LENS_FACING_BACK) {
                        return id
                    }
                }
            } catch (e: CameraAccessException) {
                // Ignore
            }
            return null
        }

    private val mTorchCallback: TorchCallback = object : TorchCallback() {
        override fun onTorchModeChanged(
            cameraId: String,
            enabled: Boolean
        ) {
            if (cameraId != mTorchCameraId) return
            mTorchEnabled = enabled
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId != mTorchCameraId) return
            mTorchEnabled = false
        }
    }
    private val mScreenStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                onDisplayOff()
            } else if (intent.action == Intent.ACTION_SCREEN_ON) {
                onDisplayOn()
            }
        }
    }

    private fun gestureToAction(gesture: Int): Int {
        return when (gesture) {
            GestureMotionSensor.SENSOR_GESTURE_SWIPE_UP -> mSwipeUpAction
            GestureMotionSensor.SENSOR_GESTURE_SWIPE_DOWN -> mSwipeDownAction
            GestureMotionSensor.SENSOR_GESTURE_SWIPE_LEFT -> mSwipeLeftAction
            GestureMotionSensor.SENSOR_GESTURE_SWIPE_RIGHT -> mSwipeRightAction
            else -> -1
        }
    }

    private fun loadPreferences(sharedPreferences: SharedPreferences) {
        try {
            mSwipeUpAction = sharedPreferences.getString(
                KEY_SWIPE_UP,
                ACTION_NONE.toString()
            )!!.toInt()
            mSwipeDownAction = sharedPreferences.getString(
                KEY_SWIPE_DOWN,
                ACTION_NONE.toString()
            )!!.toInt()
            mSwipeLeftAction = sharedPreferences.getString(
                KEY_SWIPE_LEFT,
                ACTION_NONE.toString()
            )!!.toInt()
            mSwipeRightAction = sharedPreferences.getString(
                KEY_SWIPE_RIGHT,
                ACTION_NONE.toString()
            )!!.toInt()
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Error loading preferences")
        }
    }

    private val mPrefListener =
        OnSharedPreferenceChangeListener { sharedPreferences, key ->
            try {
                when (key) {
                    KEY_SWIPE_UP -> {
                        mSwipeUpAction = sharedPreferences.getString(
                            KEY_SWIPE_UP,
                            ACTION_NONE.toString()
                        )!!.toInt()
                    }
                    KEY_SWIPE_DOWN -> {
                        mSwipeDownAction = sharedPreferences.getString(
                            KEY_SWIPE_DOWN,
                            ACTION_NONE.toString()
                        )!!.toInt()
                    }
                    KEY_SWIPE_LEFT -> {
                        mSwipeLeftAction = sharedPreferences.getString(
                            KEY_SWIPE_LEFT,
                            ACTION_NONE.toString()
                        )!!.toInt()
                    }
                    KEY_SWIPE_RIGHT -> {
                        mSwipeRightAction = sharedPreferences.getString(
                            KEY_SWIPE_RIGHT,
                            ACTION_NONE.toString()
                        )!!.toInt()
                    }
                }
            } catch (e: NumberFormatException) {
                Log.e(TAG, "Error loading preferences")
            }
        }

    companion object {
        private const val DEBUG = false
        const val TAG = "GestureService"
        private const val KEY_SWIPE_UP = "swipe_up_action_key"
        private const val KEY_SWIPE_DOWN = "swipe_down_action_key"
        private const val KEY_SWIPE_LEFT = "swipe_left_action_key"
        private const val KEY_SWIPE_RIGHT = "swipe_right_action_key"
        const val KEY_GESTURE_HAPTIC_FEEDBACK = "touchscreen_gesture_haptic_feedback"
        const val GESTURE_HAPTIC_SETTINGS_VARIABLE_NAME = "OFF_GESTURE_HAPTIC_ENABLE"
        private const val GESTURE_HAPTIC_DURATION = 100
        private const val SENSOR_WAKELOCK_DURATION = 200
        private const val ACTION_NONE = 0
        private const val ACTION_CAMERA = 1
        private const val ACTION_TORCH = 2
        private const val ACTION_WAKE_DISPLAY = 3
    }
}
