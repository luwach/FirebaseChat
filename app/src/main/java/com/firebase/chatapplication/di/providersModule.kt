package com.firebase.chatapplication.di

import android.view.TextureView
import com.firebase.chatapplication.MainActivity
import com.firebase.chatapplication.managers.CameraXManager
import com.firebase.chatapplication.managers.GeofenceManager
import com.firebase.chatapplication.managers.IntentCameraManager
import com.firebase.chatapplication.providers.SignInProvider
import com.google.android.gms.location.LocationServices
import org.koin.dsl.module.module

val providersModule = module {

    single { (activity: MainActivity) -> SignInProvider(get(), get(), get(), activity) }
    single { IntentCameraManager() }
    single { (activity: MainActivity) ->
        GeofenceManager(
            LocationServices.getGeofencingClient(
                activity.applicationContext
            ), activity.applicationContext
        )
    }
    single { (finderView: TextureView) -> CameraXManager(finderView) }
}