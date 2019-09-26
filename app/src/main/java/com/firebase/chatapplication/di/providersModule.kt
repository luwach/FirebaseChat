package com.firebase.chatapplication.di

import android.view.TextureView
import com.firebase.chatapplication.MainActivity
import com.firebase.chatapplication.managers.CameraXManager
import com.firebase.chatapplication.managers.IntentCameraManager
import com.firebase.chatapplication.providers.SignInProvider
import org.koin.dsl.module.module

val providersModule = module {

    single { (activity: MainActivity) -> SignInProvider(get(), get(), get(), activity) }
    single { IntentCameraManager() }
    single { (finderView: TextureView) -> CameraXManager(finderView) }
}