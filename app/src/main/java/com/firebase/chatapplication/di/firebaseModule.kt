package com.firebase.chatapplication.di

import android.preference.PreferenceManager
import com.firebase.chatapplication.managers.RemoteConfigManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import org.koin.dsl.module.module

val firebaseModule = module {

    factory { FirebaseAuth.getInstance() }
    factory { FirebaseDatabase.getInstance() }
    factory { FirebaseStorage.getInstance() }
    factory { RemoteConfigManager(PreferenceManager.getDefaultSharedPreferences(get())) }
}