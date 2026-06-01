package com.laszlo.tienda_app.util

import android.content.Context
import android.content.SharedPreferences

class SettingsManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var highContrastMode: Boolean
        get() = prefs.getBoolean(KEY_HIGH_CONTRAST, false)
        set(value) = prefs.edit().putBoolean(KEY_HIGH_CONTRAST, value).apply()

    var audioAssistant: Boolean
        get() = prefs.getBoolean(KEY_AUDIO_ASSISTANT, false)
        set(value) = prefs.edit().putBoolean(KEY_AUDIO_ASSISTANT, value).apply()

    companion object {
        private const val PREFS_NAME = "tienda_app_settings"
        private const val KEY_HIGH_CONTRAST = "pref_high_contrast"
        private const val KEY_AUDIO_ASSISTANT = "pref_audio_assistant"

        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsManager(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
