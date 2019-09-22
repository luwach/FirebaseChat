package com.firebase.chatapplication.managers

import android.content.SharedPreferences
import android.util.Log
import com.firebase.chatapplication.BuildConfig
import com.firebase.chatapplication.Constants.CACHE_EXPIRATION_TIME
import com.firebase.chatapplication.Constants.CONFIG_STALE_KEY
import com.firebase.chatapplication.Constants.DEFAULT_MIN_REQ_VERSION
import com.firebase.chatapplication.Constants.DEFAULT_MSG_LENGTH_LIMIT
import com.firebase.chatapplication.Constants.FORCE_UPDATE_KEY
import com.firebase.chatapplication.Constants.MSG_LENGTH_KEY
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class RemoteConfigManager(private val sharedPreferences: SharedPreferences) {

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    private val defaults =
        mapOf(
            FORCE_UPDATE_KEY to DEFAULT_MIN_REQ_VERSION,
            MSG_LENGTH_KEY to DEFAULT_MSG_LENGTH_LIMIT
        )

    init {
        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. Also use Remote Config
        // Setting to set the minimum fetch interval.
        // [START enable_dev_mode]
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        // [END enable_dev_mode]

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
        remoteConfig.setDefaults(defaults)
        // [END set_default_values]
    }

    fun isUpdateRequired() = remoteConfig.getLong(FORCE_UPDATE_KEY) > BuildConfig.VERSION_CODE

    fun getMsgLength() = remoteConfig.getLong(MSG_LENGTH_KEY)

    fun fetchAndActivate(successListener: (Boolean) -> Unit) {
        remoteConfig.fetch(
            if (
                sharedPreferences.getBoolean(CONFIG_STALE_KEY, false) ||
                remoteConfig.info.configSettings.isDeveloperModeEnabled
            ) 0 else CACHE_EXPIRATION_TIME
        ).addOnSuccessListener {
            remoteConfig.activate()
            sharedPreferences.edit().putBoolean(CONFIG_STALE_KEY, false).apply()
            successListener.invoke(true)
        }.addOnFailureListener {
            it.printStackTrace()
            successListener.invoke(false)
        }
    }
}