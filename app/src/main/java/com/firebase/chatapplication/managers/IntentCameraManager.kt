package com.firebase.chatapplication.managers

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.firebase.chatapplication.MainActivity
import java.io.File
import java.io.IOException

class IntentCameraManager {

    private var currentPath: String? = null

    fun getIntent(mainActivity: MainActivity): Intent? {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(mainActivity.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    mainActivity.createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    currentPath = it.absolutePath
                    val photoURI: Uri = FileProvider.getUriForFile(mainActivity, "com.firebase.chatapplication", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    return takePictureIntent
                }
            }
        }
        return null
    }

}