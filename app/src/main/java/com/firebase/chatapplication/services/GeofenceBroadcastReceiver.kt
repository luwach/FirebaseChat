package com.firebase.chatapplication.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.firebase.chatapplication.R
import com.firebase.chatapplication.utils.Constants.GEOFENCE_NOTIFICATION_CHANNEL_ID
import com.firebase.chatapplication.utils.Constants.NOTIFICATION_ID_GEOFENCE
import com.firebase.chatapplication.utils.sendNotification
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            // TODO log error here errorCode ${geofencingEvent.errorCode}
            return
        }
        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            handleGeofenceTransition(
                context,
                geofencingEvent.triggeringGeofences.firstOrNull(),
                geofenceTransition
            )
        } else {
            // TODO log error here Unknown geofence transition $geofenceTransition
        }
    }

    private fun handleGeofenceTransition(
        context: Context?,
        geofence: Geofence?,
        geofenceTransition: Int
    ) {
        if (context == null
            || geofence == null
            || (geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER
                    && geofenceTransition != Geofence.GEOFENCE_TRANSITION_EXIT
                    && geofenceTransition != Geofence.GEOFENCE_TRANSITION_DWELL)
        ) return
        val title = context.getString(
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
                R.string.geofence_exit_notification_desc
            else
                R.string.geofence_enter_notification_desc,
            context.getString(R.string.geofence_main_office)
        )
        context.sendNotification(
            GEOFENCE_NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_ID_GEOFENCE,
            title,
            null
        )
    }
}