package com.firebase.chatapplication.prefs

class UserPreferences: SharedPrefs("UserPreferences") {

    var username by sharedPrefs.string("username", null)
}