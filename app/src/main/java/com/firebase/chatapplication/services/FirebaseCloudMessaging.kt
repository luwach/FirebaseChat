package com.firebase.chatapplication.services

import android.preference.PreferenceManager
import com.firebase.chatapplication.utils.Constants.CONFIG_STALE_KEY
import com.firebase.chatapplication.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseCloudMessaging: FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()

        // Subscribe for remote config push topic
        FirebaseMessaging.getInstance().subscribeToTopic(applicationContext.getString(R.string.topic_push_remote_config))
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        handleRemoteMessage(remoteMessage)
    }

    private fun handleRemoteMessage(remoteMessage: RemoteMessage) =
        if (remoteMessage.data[CONFIG_STALE_KEY] == "true") {
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                .putBoolean(CONFIG_STALE_KEY, true).apply()
        } else {
            // TODO create notification for the message
        }
}