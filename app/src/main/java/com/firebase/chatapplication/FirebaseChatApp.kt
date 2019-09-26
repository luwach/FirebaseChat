package com.firebase.chatapplication

import android.app.Application
import com.firebase.chatapplication.di.firebaseModule
import com.firebase.chatapplication.di.providersModule
import com.firebase.chatapplication.di.repositoriesModule
import org.koin.android.ext.android.startKoin

class FirebaseChatApp: Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        initKoin()
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