package com.firebase.chatapplication

import android.Manifest

object Constants {

    const val RC_SIGN_IN = 1
    const val RC_PHOTO_PICKER = 2
    const val RC_PHOTO_CAMERA = 3
    const val DEFAULT_MSG_LENGTH_LIMIT = 1000
    const val MSG_LENGTH_KEY = "message_length"
    const val ANONYMOUS = "anonymous"

    const val REQUEST_CODE_PERMISSIONS = 10
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
}