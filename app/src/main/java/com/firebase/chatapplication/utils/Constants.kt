package com.firebase.chatapplication.utils

object Constants {

    const val RC_SIGN_IN = 1
    const val RC_PHOTO_PICKER = 2
    const val RC_PHOTO_CAMERA = 3

    // Remote config
    const val CONFIG_STALE_KEY = "config_stale"
    const val FORCE_UPDATE_KEY = "force_update"
    const val MSG_LENGTH_KEY = "message_length"

    const val DEFAULT_MSG_LENGTH_LIMIT = 1000
    const val DEFAULT_MIN_REQ_VERSION = 1

    const val CACHE_EXPIRATION_TIME = 3600L  // s

    const val CHANNEL_1_ID = "channel1"
    const val CHANNEL_2_ID = "channel2"
}