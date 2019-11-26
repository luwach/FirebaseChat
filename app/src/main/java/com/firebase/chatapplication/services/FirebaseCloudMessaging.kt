package com.firebase.chatapplication.services

import android.preference.PreferenceManager
import android.util.Log
import com.firebase.chatapplication.R
import com.firebase.chatapplication.utils.Constants.CONFIG_STALE_KEY
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseCloudMessaging: FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()

        // Subscribe for remote config push topic
        FirebaseMessaging.getInstance().subscribeToTopic(applicationContext.getString(R.string.topic_push_remote_config))
        FirebaseMessaging.getInstance().subscribeToTopic(applicationContext.getString(R.string.topic_random))
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data[CONFIG_STALE_KEY] == "true") {
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                .putBoolean(CONFIG_STALE_KEY, true).apply()
        } else {
            remoteMessage.data?.let {
                Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            }
        }
    }

    companion object {
        const val TAG = "FirebaseCloudMessaging"
    }
}