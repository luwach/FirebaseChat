package com.firebase.chatapplication.prefs

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.firebase.chatapplication.FirebaseChatApp
import kotlin.reflect.KProperty

abstract class SharedPrefs(private val sharedPrefsName: String) {

    protected val sharedPrefs: SharedPreferences by lazy {
        FirebaseChatApp.instance.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
    }

    fun clear() {
        sharedPrefs.edit().clear().apply()
    }
}

fun SharedPreferences.string(key: String, defValue: String?) =
    SharedPrefsDelegate(
        this,
        key,
        defValue,
        SharedPreferences::getString,
        SharedPreferences.Editor::putString
    )

open class SharedPrefsDelegate<T>(
    private val prefs: SharedPreferences,
    private val key: String,
    private val defValue: T,
    private val getFunction: (SharedPreferences, String, T) -> T,
    private val setFunction: (SharedPreferences.Editor, String, T) -> SharedPreferences.Editor
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getFunction(prefs, key, defValue)
    }

    @SuppressLint("CommitPrefEdits")
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        setFunction(prefs.edit(), key, value).apply()
    }
}