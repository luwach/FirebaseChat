package com.firebase.chatapplication.di

import com.firebase.chatapplication.prefs.UserPreferences
import org.koin.dsl.module.module

val repositoriesModule = module {

    single { UserPreferences() }
}