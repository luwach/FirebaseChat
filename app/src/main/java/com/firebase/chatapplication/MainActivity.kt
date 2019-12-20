package com.firebase.chatapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.InputFilter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.chatapplication.managers.CameraXManager
import com.firebase.chatapplication.managers.GeofenceManager
import com.firebase.chatapplication.managers.IntentCameraManager
import com.firebase.chatapplication.managers.RemoteConfigManager
import com.firebase.chatapplication.model.Message
import com.firebase.chatapplication.prefs.UserPreferences
import com.firebase.chatapplication.providers.SignInProvider
import com.firebase.chatapplication.utils.Constants.RC_PHOTO_CAMERA
import com.firebase.chatapplication.utils.Constants.RC_PHOTO_PICKER
import com.firebase.chatapplication.utils.Constants.RC_SIGN_IN
import com.firebase.chatapplication.utils.SimpleTextWatcher
import com.firebase.chatapplication.view.ForceUpdateDialogFragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@RuntimePermissions
class MainActivity : AppCompatActivity(), KoinComponent {

    private val firebaseDatabase: FirebaseDatabase by inject()
    private val firebaseStorage: FirebaseStorage by inject()
    private val provider: SignInProvider by inject { parametersOf(this@MainActivity) }
    private val cameraXManager: CameraXManager by inject { parametersOf(finderView) }
    private val userPreferences: UserPreferences by inject()
    private val remoteConfigManager: RemoteConfigManager by inject()
    private val intentCameraManager: IntentCameraManager by inject()
    private val geofenceManager: GeofenceManager by inject { parametersOf(this@MainActivity) }

    private var currentPhotoPath: String? = null
    private val listAdapter = ListAdapter()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        provider.init(this.lifecycle)
        initGeoFenceWithPermissionCheck()
        firebaseInit()
        initView()
        initCameraX()
    }

    fun initCameraX() {
        cameraPickerButton.setOnClickListener {
            openCameraWithPermissionCheck(false)
        }

        cameraPickerButton.setOnLongClickListener {
            openCameraWithPermissionCheck(true)
            true
        }

//        finderView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
//            cameraXManager.updateTransform()
//        }

        captureButton.setOnClickListener {
            cameraView.visibility = View.GONE
            mainView.visibility = View.VISIBLE
            cameraXManager.takePicture(createImageFile()) {
                it.path.split("/").last().run {
                    val photoRef = storageReference.child(this)
                    handleResponse(photoRef.putFile(Uri.fromFile(it)))
                }
            }
        }
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    fun initGeoFence() {
        geofenceManager.addGeofences()
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun openCamera(inApp: Boolean) {
        if (inApp) {
            cameraView.visibility = View.VISIBLE
            mainView.visibility = View.GONE
//            finderView.post { cameraXManager.startCamera(this) }
        } else {
            startActivityForResult(intentCameraManager.getIntent(this), RC_PHOTO_CAMERA)
        }
    }

    private fun firebaseInit() {

        databaseReference = firebaseDatabase.reference.child("messages")
        storageReference = firebaseStorage.reference.child("chat_photos")

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            Log.d("MyToken", "FCM token: ${it.token}")
        }

        provider.onItemsUpdate = {
            listAdapter.setMessages(it)
            progressBar.visibility = ProgressBar.INVISIBLE
        }

        remoteConfigManager.fetchAndActivate {
            if (it) {
                Log.d("###", "isForceUpdate = ${remoteConfigManager.isUpdateRequired()}")
                if (remoteConfigManager.isUpdateRequired())
                    ForceUpdateDialogFragment().show(
                        supportFragmentManager,
                        "ForceUpdateDialogFragment"
                    )
            } else {
                Toast.makeText(this, "Remote config fetch failed!", Toast.LENGTH_SHORT).show()
            }
            applyRetrievedLengthLimit(remoteConfigManager.getMsgLength())
        }
    }

    private fun applyRetrievedLengthLimit(messageLength: Long) =
        messageEditText.also {
            it.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(messageLength.toInt()))
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
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        currentPhotoPath?.split("/")?.last()?.run {
                            val photoRef = storageReference.child(this)
                            handleResponse(photoRef.putFile(Uri.fromFile(File(currentPhotoPath))))
                        }
                    }
                }
        }
    }

    private fun handleResponse(uploadTask: UploadTask) =
        uploadTask.addOnSuccessListener { taskSnapshot ->
            Handler().postDelayed({ downloadProgress.progress = 0 }, 5000)
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                databaseReference.push()
                    .setValue(Message(null, userPreferences.username ?: "Anonymous", it.toString()))
            }
        }.addOnProgressListener {
            downloadProgress.progress =
                with(it) { (100 * bytesTransferred / totalByteCount).toInt() }
        }

    private fun initView() {
        messageRecyclerView.adapter = listAdapter
        messageEditText.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                sendButton.isEnabled = charSequence.trim { it <= ' ' }.isNotEmpty()
            }
        })

        listAdapter.onDeleteClick = { photoUrl, key ->
            val photoRef = firebaseStorage.getReferenceFromUrl(photoUrl)
            photoRef.delete().addOnSuccessListener {
                databaseReference.child(key).removeValue()
                Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.sign_out_menu -> {
            AuthUI.getInstance().signOut(this)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    fun onClickSendButton(view: View) {
        databaseReference.push().setValue(
            Message(
                messageEditText.text.toString(), userPreferences.username ?: "Anonymous"
            )
        )
        messageEditText.setText("")
    }

    fun onClickUploadImage(view: View) = Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(this, "Complete action"), RC_PHOTO_PICKER)
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraPermissionDenied() {
        Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
    }
}
