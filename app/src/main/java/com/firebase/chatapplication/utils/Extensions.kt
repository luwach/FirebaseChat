package com.firebase.chatapplication.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun Context.openLink(link: String) = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(this)
}

fun ViewGroup.inflateItem(@LayoutRes layout: Int): View = LayoutInflater.from(context).inflate(layout, this, false)