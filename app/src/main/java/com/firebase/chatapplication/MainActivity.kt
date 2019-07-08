package com.firebase.chatapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.TextureView
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.firebase.chatapplication.Constants.ANONYMOUS
import com.firebase.chatapplication.Constants.DEFAULT_MSG_LENGTH_LIMIT
import com.firebase.chatapplication.Constants.MSG_LENGTH_KEY
import com.firebase.chatapplication.Constants.RC_PHOTO_CAMERA
import com.firebase.chatapplication.Constants.RC_PHOTO_PICKER
import com.firebase.chatapplication.Constants.RC_SIGN_IN
import com.firebase.chatapplication.Constants.REQUEST_CODE_PERMISSIONS
import com.firebase.chatapplication.Constants.REQUIRED_PERMISSIONS
import com.firebase.chatapplication.camera.CameraXManager
import com.firebase.chatapplication.camera.IntentCameraManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var currentPhotoPath: String? = null
    private var username = ""
    private lateinit var listAdapter: ListAdapter
    private lateinit var viewFinder: TextureView

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var valueEventListener: ValueEventListener? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    private val intentCameraManager = IntentCameraManager()
    private lateinit var cameraXManager: CameraXManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        username = ANONYMOUS

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        remoteConfig = FirebaseRemoteConfig.getInstance()

        databaseReference = firebaseDatabase.reference.child("messages")
        storageReference = firebaseStorage.reference.child("chat_photos")

        listAdapter = ListAdapter()
        messageRecyclerView.adapter = listAdapter

        viewFinder = findViewById(R.id.view_finder)
        cameraXManager = CameraXManager(viewFinder)

        cameraPickerButton.setOnLongClickListener {
            if (allPermissionsGranted()) {
                cameraView.visibility = View.VISIBLE
                mainView.visibility = View.GONE
                viewFinder.post { cameraXManager.startCamera(this) }
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
            true
        }
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            cameraXManager.updateTransform()
        }
        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
            cameraView.visibility = View.GONE
            mainView.visibility = View.VISIBLE
            cameraXManager.takePicture(createImageFile()) { file ->
                file.path?.split("/")?.last()?.run {
                    val photoRef = storageReference.child(this)
                    handleResponse(photoRef.putFile(Uri.fromFile(file)))
                }
            }
        }

        messageTextWatcher()
        deletePhoto()

        authStateListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser

            when (user) {
                null -> {
                    onSignedOutCleanUp()

                    val providers = mutableListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )

                    // Create and launch sign-in intent
                    startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(providers)
                            .setTheme(R.style.ThemeOverlay_AppCompat_Dark)
                            .setLogo(R.drawable.ic_android)
                            .build(), RC_SIGN_IN
                    )
                }
                else -> onSignedInInitialize(user.displayName)
            }
        }

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()

        val defaultConfigMap = hashMapOf<String, Any>(MSG_LENGTH_KEY to DEFAULT_MSG_LENGTH_LIMIT)

        remoteConfig.apply {
            setConfigSettings(configSettings)
            setDefaults(defaultConfigMap)
        }

        fetchConfig()
    }

    private fun fetchConfig() {
        var cacheExpiration = 3600L
        if (remoteConfig.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }
        remoteConfig.fetch(cacheExpiration)
            .addOnSuccessListener {
                remoteConfig.activateFetched()
                applyRetrievedLengthLimit()
            }.addOnFailureListener {
                Log.w("MAIN_ACTIVITY", "Error fetching config", it)
                applyRetrievedLengthLimit()
            }
    }

    private fun applyRetrievedLengthLimit() {
        val messageLength = remoteConfig.getLong(MSG_LENGTH_KEY)
        messageEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(messageLength.toInt()))
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
                        handleResponse(photoRef.putFile(selectedImageUri))
                    }
                }
            RC_PHOTO_CAMERA ->
                if (resultCode == Activity.RESULT_OK) {
                    currentPhotoPath?.split("/")?.last()?.run {
                        val photoRef = storageReference.child(this)
                        handleResponse(photoRef.putFile(Uri.fromFile(File(currentPhotoPath))))
                    }
                }
        }
    }

    private fun handleResponse(uploadTask: UploadTask) {
        uploadTask.addOnSuccessListener { taskSnapshot ->
            Handler().postDelayed({ downloadProgress.progress = 0 }, 5000)
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val message = Message(null, username, uri.toString())
                databaseReference.push().setValue(message)
            }
        }.addOnProgressListener {
            downloadProgress.progress =
                with(it) { (100 * bytesTransferred / totalByteCount).toInt() }
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
                    }
                )

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

    fun onClickUploadCamera(view: View) {
        if (allPermissionsGranted()) {
            startActivityForResult(intentCameraManager.getIntent(this), RC_PHOTO_CAMERA)
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { cameraXManager.startCamera(this) }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }
}
