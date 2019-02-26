package com.firebase.chatapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.firebase.chatapplication.Constants.ANONYMOUS
import com.firebase.chatapplication.Constants.DEFAULT_MSG_LENGTH_LIMIT
import com.firebase.chatapplication.Constants.RC_PHOTO_PICKER
import com.firebase.chatapplication.Constants.RC_SIGN_IN
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var username = ""
    private lateinit var listAdapter: ListAdapter

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private var valueEventListener: ValueEventListener? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        username = ANONYMOUS

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        databaseReference = firebaseDatabase.reference.child("messages")
        storageReference = firebaseStorage.reference.child("chat_photos")

        listAdapter = ListAdapter()
        messageRecyclerView.adapter = listAdapter

        messageTextWatcher()
        deletePhoto()

        authStateListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser

            when (user) {
                null -> {
                    onSignedOutCleanUp()

                    val providers = mutableListOf(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build())

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .setTheme(R.style.ThemeOverlay_AppCompat_Dark)
                                    .setLogo(R.drawable.ic_android)
                                    .build(), RC_SIGN_IN)
                }
                else -> onSignedInInitialize(user.displayName)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SIGN_IN ->
                when (resultCode) {
                    RESULT_OK -> Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show()
                    RESULT_CANCELED -> {
                        Toast.makeText(this, "Signed in canceled!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            RC_PHOTO_PICKER ->
                when (resultCode) {
                    RESULT_OK -> {
                        val selectedImageUri = data?.data
                        val photoRef = storageReference.child(selectedImageUri?.lastPathSegment!!)

                        photoRef.putFile(selectedImageUri)
                                .addOnSuccessListener { taskSnapshot ->

                                    Handler().postDelayed({ downloadProgress.progress = 0 }, 5000)

                                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                                        val message = Message(null, username, uri.toString())
                                        databaseReference.push().setValue(message)

                                    }
                                }.addOnProgressListener {
                                    downloadProgress.progress = with(it) { (100 * bytesTransferred / totalByteCount).toInt() }
                                }
                    }
                }
        }
    }

    private fun deletePhoto() {
        listAdapter.onDeleteClick = { photoUrl, key ->
            val photoRef = firebaseStorage.getReferenceFromUrl(photoUrl)
            photoRef.delete().addOnSuccessListener {
                databaseReference.child(key).removeValue()
                Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun messageTextWatcher() {
        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                sendButton.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        messageEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT))
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener!!)
    }

    override fun onPause() {
        super.onPause()
        authStateListener?.let {
            firebaseAuth.removeAuthStateListener(it)
        }
        detachDatabaseReadListener()
    }

    private fun onSignedInInitialize(name: String?) {
        username = name.toString()

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                listAdapter.clearData()

                dataSnapshot.children.forEach(
                        fun(dataSnapshot: DataSnapshot) {
                            val message = dataSnapshot.getValue<Message>(Message::class.java)
                            message?.let {
                                it.key = dataSnapshot.key
                                listAdapter.swapData(message)
                            }
                        })

                listAdapter.notifyDataSetChanged()
                progressBar.visibility = ProgressBar.INVISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        databaseReference.addValueEventListener(valueEventListener!!)
    }

    private fun onSignedOutCleanUp() {
        username = ANONYMOUS
        detachDatabaseReadListener()
    }

    private fun detachDatabaseReadListener() {
        listAdapter.clearData()
        valueEventListener?.let {
            databaseReference.removeEventListener(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.sign_out_menu -> {
            AuthUI.getInstance().signOut(this)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    fun onClickSendButton(view: View) {
        val message = Message(messageEditText.text.toString(), username)
        databaseReference.push().setValue(message)

        messageEditText.setText("")
    }

    fun onClickUploadImage(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }
        startActivityForResult(Intent.createChooser(intent, "Complete action"), RC_PHOTO_PICKER)
    }
}
