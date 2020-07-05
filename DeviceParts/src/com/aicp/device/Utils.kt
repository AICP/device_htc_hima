/*
* Copyright (C) 2013 The OmniROM Project
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

import android.util.Log
import java.io.*

object Utils {
    private const val DEBUG = false
    private const val TAG = "Utils"

    /**
     * Write a string value to the specified file.
     * @param filename      The filename
     * @param value         The value
     */
    @JvmStatic
    fun writeValue(filename: String?, value: String) {
        if (filename == null) {
            return
        }
        if (DEBUG) Log.d(TAG, "writeValue filename / value:$filename / $value")
        try {
            val fos = FileOutputStream(File(filename))
            fos.write(value.toByteArray())
            fos.flush()
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Write a string value to the specified sysfs file.
     * @param filename      The filename
     * @param value         The value
     */
    @JvmStatic
    fun writeValueSimple(filename: String?, value: String) {
        if (filename == null) {
            return
        }
        if (DEBUG) Log.d(TAG, "writeValueSimple filename / value:$filename / $value")
        try {
            val fos = FileOutputStream(File(filename))
            val valueSimple = """
                $value
                
                """.trimIndent()
            fos.write(valueSimple.toByteArray())
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Write a string value to the specified file.
     * @param filename      The filename
     * @param value         The value
     */
    fun writeValueDual(filename: String?, value: String) {
        if (filename == null) {
            return
        }
        val Dualvalue = "$value $value"
        if (DEBUG) Log.d(TAG, "writeValueDual: filename / value:$filename / $Dualvalue")
        try {
            val fos = FileOutputStream(File(filename))
            fos.write(Dualvalue.toByteArray())
            fos.flush()
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Check if the specified file exists.
     * @param filename      The filename
     * @return              Whether the file exists or not
     */
    fun fileExists(filename: String?): Boolean {
        return if (filename == null) {
            false
        } else File(filename).exists()
    }

    @JvmStatic
    fun fileWritable(filename: String?): Boolean {
        return fileExists(filename) && File(filename).canWrite()
    }

    fun readLine(filename: String?): String? {
        if (filename == null) {
            return null
        }
        var br: BufferedReader? = null
        var line: String? = null
        try {
            br = BufferedReader(FileReader(filename), 1024)
            line = br.readLine()
        } catch (e: IOException) {
            return null
        } finally {
            if (br != null) {
                try {
                    br.close()
                } catch (e: IOException) {
                    // ignore
                }
            }
        }
        return line
    }

    /*
     * we need this little helper method, because HTC decided in their infinite wisdom
     * to chose this format in corresponding sysfs file:
     * [VIB] voltage input:2500mV
    */
    fun declutterVibratorValue(HtcOutput: String): String {
        var declutteredValue: String? = null
        val seperateVolts = HtcOutput.split(":".toRegex(), 2).toTypedArray()
        val declutterLength = seperateVolts[1].length
        declutteredValue = seperateVolts[1].substring(0, declutterLength - 2)
        Log.i(TAG, "decluttered vibrator value: $declutteredValue")
        return declutteredValue
    }

    /*
     * we need this little helper method, because api offers us values for left and right.
     * We want to handle both values equal, so only read left value.
     * Format in sysfs file is:
     * 1 1
     * BUT... for some reasons, when writing in the file a -1, the value in the file is 255,
     * -2 is 254, so we have here to do some maths...
    */
    fun declutterDualValue(HtcOutput: String): String {
        val seperateDual = HtcOutput.split(" ".toRegex(), 2).toTypedArray()
        var declutteredValue = Integer.parseUnsignedInt(seperateDual[0])
        if (declutteredValue > 20) {
            // The chosen variablename is like the thing it does ;-) ...
            val declutteredandConvertedValue = declutteredValue - 256
            declutteredValue = declutteredandConvertedValue
        }
        Log.i(TAG, "deplutterDualvalue: decluttered value: $declutteredValue")
        return declutteredValue.toString()
    }

    fun getFileValueAsBoolean(filename: String?, defValue: Boolean): Boolean {
        val fileValue = readLine(filename)
        return if (fileValue != null) {
            if (fileValue == "0") false else true
        } else defValue
    }

    fun getFileValue(filename: String, defValue: String): String {
        val fileValue = readLine(filename)
        if (DEBUG) Log.d(TAG, "getFileValue file / value:$filename / $fileValue")
        if (fileValue != null) {
            return fileValue
        }
        if (DEBUG) Log.e(TAG, "getFileValue file / value:$filename / $defValue")
        return defValue
    }

    @JvmStatic
    fun getFileValueVibrator(filename: String, defValue: String): String {
        val fileValue = readLine(filename)
        if (fileValue != null) {
            return declutterVibratorValue(fileValue)
        }
        if (DEBUG) Log.d(TAG, "getFileValue file / value:$filename / $defValue")
        return defValue
    }

    fun getFileValueDual(filename: String, defValue: String): String {
        val fileValue = readLine(filename)
        if (DEBUG) Log.d(TAG, "getFileValueDual: file / value:$filename / $fileValue")
        if (fileValue != null) {
            return declutterDualValue(fileValue)
        }
        if (DEBUG) Log.e(TAG, "getFileValueDual: file / default value:$filename / $defValue")
        return defValue
    }
}