package com.firebase.chatapplication

import android.app.Application
import android.app.NotificationManager
import android.os.Build
import com.firebase.chatapplication.di.firebaseModule
import com.firebase.chatapplication.di.providersModule
import com.firebase.chatapplication.di.repositoriesModule
import com.firebase.chatapplication.utils.Constants.CHANNEL_1_ID
import com.firebase.chatapplication.utils.Constants.CHANNEL_2_ID
import com.firebase.chatapplication.utils.Constants.GEOFENCE_NOTIFICATION_CHANNEL_ID
import com.firebase.chatapplication.utils.Constants.GEOFENCE_NOTIFICATION_CHANNEL_NAME
import com.firebase.chatapplication.utils.createNotificationChannel
import org.koin.android.ext.android.startKoin


class FirebaseChatApp : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        createNotificationChannels()
        initKoin()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                listOf(
                    Triple(CHANNEL_2_ID, "Urgent channel", "This is Channel 2"),
                    Triple(
                        GEOFENCE_NOTIFICATION_CHANNEL_ID,
                        GEOFENCE_NOTIFICATION_CHANNEL_NAME,
                        "This is $GEOFENCE_NOTIFICATION_CHANNEL_NAME"
                    )
                ), NotificationManager.IMPORTANCE_DEFAULT
            )
            createNotificationChannel(
                CHANNEL_1_ID,
                "Default channel",
                "This is Channel 1",
                NotificationManager.IMPORTANCE_HIGH
            )
        }
    }

    private fun initKoin() {
        startKoin(this, listOf(firebaseModule, providersModule, repositoriesModule))
    }

    companion object {
        @JvmStatic
        lateinit var instance: FirebaseChatApp
            private set
    }
}