package com.firebase.chatapplication

import android.app.Application
import com.firebase.chatapplication.di.firebaseModule
import org.koin.core.context.startKoin

class FirebaseChatApp: Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        initKoin()
    }

    private fun initKoin() {
        startKoin { modules(firebaseModule) }
    }

    companion object {

        @JvmStatic
        lateinit var instance: FirebaseChatApp
            private set
    }
}