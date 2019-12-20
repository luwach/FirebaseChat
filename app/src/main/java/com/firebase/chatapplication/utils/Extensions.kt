package com.firebase.chatapplication.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.firebase.chatapplication.MainActivity
import com.firebase.chatapplication.R

fun Context.openLink(link: String) = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(this)
}

fun ViewGroup.inflateItem(@LayoutRes layout: Int): View =
    LayoutInflater.from(context).inflate(layout, this, false)

@RequiresApi(Build.VERSION_CODES.N)
fun Context.createNotificationChannel(
    channelInfo: List<Triple<String, String, String>>,
    importance: Int = NotificationManager.IMPORTANCE_DEFAULT
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        channelInfo.map {
            NotificationChannel(it.first, it.second, importance).apply { description = it.third }
        }.run {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(this)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
fun Context.createNotificationChannel(
    channelId: String,
    channelName: String,
    channelDescription: String,
    importance: Int = NotificationManager.IMPORTANCE_DEFAULT
) = createNotificationChannel(
    listOf(Triple(channelId, channelName, channelDescription)),
    importance
)

fun Context.sendNotification(
    channelId: String,
    id: Int,
    title: String,
    text: String?,
    requestCode: Int = 0,
    @DrawableRes iconRes: Int = R.mipmap.ic_launcher,
    priority: Int = NotificationCompat.PRIORITY_DEFAULT,
    autoCancel: Boolean = true
) {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(this, requestCode, intent, 0)

    val notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
        setSmallIcon(iconRes)
        setContentTitle(title)
        if (text != null) setContentText(text)
        this.priority = priority
        setContentIntent(pendingIntent)
        setChannelId(channelId)
        setAutoCancel(autoCancel)
    }.build()
    with(NotificationManagerCompat.from(this)) {
        notify(id, notificationBuilder)
    }
}