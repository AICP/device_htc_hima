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

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.android.internal.util.aicp.FileUtils
import java.util.*

class GestureMotionSensor private constructor(private val mContext: Context) {
    private val mSensorManager: SensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val mSensor: Sensor?
    private var mEnabledGestures = 0
    private val mListeners: MutableList<GestureMotionSensorListener>

    interface GestureMotionSensorListener {
        fun onEvent(gesture: Int, event: SensorEvent?)
    }

    fun enableGesture(gesture: Int) {
        if (DEBUG) Log.d(
            TAG,
            "Enabling"
        )
        mEnabledGestures = mEnabledGestures or gesture
    }

    fun disableGesture(gesture: Int) {
        if (DEBUG) Log.d(
            TAG,
            "Disabling"
        )
        mEnabledGestures = mEnabledGestures and gesture.inv()
    }

    fun beginListening() {
        if (!FileUtils.isFileReadable(CONTROL_PATH) || !FileUtils.isFileWritable(
                CONTROL_PATH
            )
        ) {
            Log.w(
                TAG,
                "Control path not accessible, unable to disable sensor"
            )
            return
        }
        if (!FileUtils.writeLine(
                CONTROL_PATH,
                Integer.toHexString(mEnabledGestures)
            )
        ) {
            Log.w(
                TAG,
                "Failed to write control path, unable to disable sensor"
            )
            return
        }
        mSensorManager.registerListener(
            mSensorEventListener,
            mSensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            BATCH_LATENCY_IN_MS * 1000
        )
    }

    fun stopListening() {
        if (!FileUtils.isFileReadable(CONTROL_PATH) || !FileUtils.isFileWritable(
                CONTROL_PATH
            )
        ) {
            Log.w(
                TAG,
                "Control path not accessible, unable to disable sensor"
            )
            return
        }
        if (!FileUtils.writeLine(
                CONTROL_PATH,
                Integer.toHexString(0)
            )
        ) {
            Log.w(
                TAG,
                "Failed to write control path, unable to disable sensor"
            )
            return
        }
        mSensorManager.unregisterListener(mSensorEventListener)
    }

    fun registerListener(listener: GestureMotionSensorListener) {
        mListeners.add(listener)
    }

    /* TODO: figure out why
         * mSensorManager.getDefaultSensor(65538);
         * isn't returning a valid sensor.
         */
    private val gestureMotionSensor: Sensor?
        get() {
            /* TODO: figure out why
         * mSensorManager.getDefaultSensor(65538);
         * isn't returning a valid sensor.
         */
            val it: Iterator<*> =
                mSensorManager.getSensorList(Sensor.TYPE_ALL).iterator()
            while (it.hasNext()) {
                val sensor = it.next() as Sensor
                if (GESTURE_MOTION_SENSOR_NAME == sensor.name) {
                    return sensor
                }
            }
            Log.w(
                TAG,
                "Unable to find valid gesture motion sensor"
            )
            return null
        }

    private fun onSensorEvent(gesture: Int, event: SensorEvent) {
        for (l in mListeners) {
            l.onEvent(gesture, event)
        }
    }

    private val mSensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (DEBUG) Log.d(
                TAG,
                "onSensorChanged: got event: " + event.values[0].toInt()
            )
            val sensorEvent = event.values[0].toInt()
            val gesture = sensorEventToGesture(sensorEvent)
            if (gesture and mEnabledGestures != 0) {
                /* Only report events which we care about */
                onSensorEvent(gesture, event)
            }
        }

        override fun onAccuracyChanged(
            sensor: Sensor,
            accuracy: Int
        ) {
            /* Empty */
        }
    }

    companion object {
        private const val DEBUG = false
        private const val TAG = "GestureMotionSensor"
        private const val CONTROL_PATH =
            "/sys/devices/virtual/htc_sensorhub/sensor_hub/gesture_motion"
        private const val GESTURE_MOTION_SENSOR_NAME = "hTC Gesture_Motion"

        /* Sensor gesture definition used to instantiate GestureMotionSensor, externally usable */ /* These values also correspond to kernel driver values, so don't change them */
        const val SENSOR_GESTURE_SWIPE_UP = 0x4
        const val SENSOR_GESTURE_SWIPE_DOWN = 0x8
        const val SENSOR_GESTURE_SWIPE_LEFT = 0x10
        const val SENSOR_GESTURE_SWIPE_RIGHT = 0x20
        const val SENSOR_GESTURE_CAMERA = 0x40
        const val SENSOR_GESTURE_DOUBLE_TAP = 0x8000

        /* Corresponds to actual sensor ID, internal use only */
        private const val SENSOR_TYPE_ANY_MOTION = 65537
        private const val SENSOR_TYPE_GESTURE_MOTION = 65538

        /* Corresponds to sensor event value, internal use only */
        private const val SENSOR_EVENT_ID_DOUBLE_TAP = 15
        private const val SENSOR_EVENT_ID_SWIPE_UP = 2
        private const val SENSOR_EVENT_ID_SWIPE_DOWN = 3
        private const val SENSOR_EVENT_ID_SWIPE_LEFT = 4
        private const val SENSOR_EVENT_ID_SWIPE_RIGHT = 5
        private const val SENSOR_EVENT_ID_CAMERA = 6
        protected const val BATCH_LATENCY_IN_MS = 100
        private var sInstance: GestureMotionSensor? = null
        private fun sensorEventToGesture(event: Int): Int {
            return when (event) {
                SENSOR_EVENT_ID_DOUBLE_TAP -> SENSOR_GESTURE_DOUBLE_TAP
                SENSOR_EVENT_ID_SWIPE_UP -> SENSOR_GESTURE_SWIPE_UP
                SENSOR_EVENT_ID_SWIPE_DOWN -> SENSOR_GESTURE_SWIPE_DOWN
                SENSOR_EVENT_ID_SWIPE_LEFT -> SENSOR_GESTURE_SWIPE_LEFT
                SENSOR_EVENT_ID_SWIPE_RIGHT -> SENSOR_GESTURE_SWIPE_RIGHT
                else -> -1
            }
        }

        @JvmStatic
        fun getInstance(context: Context): GestureMotionSensor? {
            /* The hTC Gesture_Motion sensor doesn't accept multiple listeners anyway */
            if (sInstance == null) {
                sInstance = GestureMotionSensor(context)
            }
            return sInstance
        }
    }

    init {
        mSensor = gestureMotionSensor
        mListeners = ArrayList()
    }
}
