package com.firebase.chatapplication

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.openLink(link: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(browserIntent)
}