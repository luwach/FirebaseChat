package com.firebase.chatapplication.managers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.firebase.chatapplication.services.GeofenceBroadcastReceiver
import com.firebase.chatapplication.utils.Constants.GEOFENCE_OFFICE_ID
import com.firebase.chatapplication.utils.Constants.GEOFENCE_OFFICE_LAT
import com.firebase.chatapplication.utils.Constants.GEOFENCE_OFFICE_LON
import com.firebase.chatapplication.utils.Constants.GEOFENCE_OFFICE_RADIUS
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest

class GeofenceManager(private val client: GeofencingClient, context: Context) {

    private var geofencePendingIntent: PendingIntent
    private var geofenceRequest: GeofencingRequest

    init {
        val geofenceOne = Geofence.Builder()
            .setRequestId(GEOFENCE_OFFICE_ID)
            .setCircularRegion(
                GEOFENCE_OFFICE_LAT,
                GEOFENCE_OFFICE_LON,
                GEOFENCE_OFFICE_RADIUS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofenceOne)
            .build()
        geofencePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun addGeofences() {
        client.addGeofences(geofenceRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d(this@GeofenceManager.javaClass.simpleName, "addGeofences success")
            }
            addOnFailureListener { it.printStackTrace() }
        }
    }

    fun removeGeofences() {
        client.removeGeofences(listOf(GEOFENCE_OFFICE_ID)).run {
            addOnSuccessListener {
                Log.d(this@GeofenceManager.javaClass.simpleName, "removeGeofences success")
            }
            addOnFailureListener { it.printStackTrace() }
        }
    }
}