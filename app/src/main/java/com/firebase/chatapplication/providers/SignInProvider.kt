package com.firebase.chatapplication.providers

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.firebase.chatapplication.MainActivity
import com.firebase.chatapplication.R
import com.firebase.chatapplication.model.Message
import com.firebase.chatapplication.prefs.UserPreferences
import com.firebase.chatapplication.utils.Constants
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.koin.standalone.KoinComponent

class SignInProvider(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val userPreferences: UserPreferences,
    private val activity: MainActivity
): LifecycleObserver, KoinComponent {

    private var messages = ArrayList<Message>()
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var valueEventListener: ValueEventListener? = null
    private lateinit var databaseReference: DatabaseReference
    lateinit var onItemsUpdate: (List<Message>) -> Unit

    fun init(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        databaseReference = firebaseDatabase.reference.child("messages")

        authStateListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser

            when (it.currentUser) {
                null -> {
                    onSignedOutCleanUp()

                    val providers = mutableListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )

                    // Create and launch sign-in intent
                    activity.startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(providers)
                            .setTheme(R.style.ThemeOverlay_AppCompat_Dark)
                            .setLogo(R.drawable.ic_android)
                            .build(), Constants.RC_SIGN_IN
                    )
                }
                else -> onSignedInInitialize(user?.displayName)
            }
        }
    }

    private fun onSignedInInitialize(name: String?) {
        userPreferences.username = name

        valueEventListener = object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                messages.clear()

                dataSnapshot.children.forEach(
                    fun(dataSnapshot: DataSnapshot) {
                        val message = dataSnapshot.getValue<Message>(Message::class.java)
                        message?.let {
                            it.key = dataSnapshot.key
                            messages.add(it)
                        }
                    }
                )
                onItemsUpdate.invoke(messages)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        databaseReference.addValueEventListener(valueEventListener!!)
    }

    private fun onSignedOutCleanUp() {
        valueEventListener?.let {
            databaseReference.removeEventListener(it)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        firebaseAuth.addAuthStateListener(authStateListener!!)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        authStateListener?.let {
            firebaseAuth.removeAuthStateListener(it)
        }
    }
}