package com.firebase.chatapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.firebase.chatapplication.di.firebaseModule
import com.firebase.chatapplication.di.providersModule
import com.firebase.chatapplication.di.repositoriesModule
import com.firebase.chatapplication.utils.Constants.CHANNEL_1_ID
import com.firebase.chatapplication.utils.Constants.CHANNEL_2_ID
import org.koin.android.ext.android.startKoin


class FirebaseChatApp: Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        createNotificationChannels()
        initKoin()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel1 = NotificationChannel(CHANNEL_1_ID, "Default channel", NotificationManager.IMPORTANCE_HIGH)
                .also { it.description = "This is Channel 1" }

            val channel2 = NotificationChannel(CHANNEL_2_ID, "Urgent channel", NotificationManager.IMPORTANCE_DEFAULT)
                .also { it.description = "This is Channel 2" }

            getSystemService(NotificationManager::class.java).apply {
                this?.createNotificationChannel(channel1)
                this?.createNotificationChannel(channel2)
            }
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