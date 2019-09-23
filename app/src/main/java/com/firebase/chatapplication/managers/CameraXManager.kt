package com.firebase.chatapplication.managers

import android.graphics.Matrix
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.lifecycle.LifecycleOwner
import java.io.File

class CameraXManager(
    private val viewFinder: TextureView
) {

    private lateinit var imageCapture: ImageCapture

    fun startCamera(lifecycleOwner: LifecycleOwner) {
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(640, 640))
        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {
            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            // We don't set a resolution for image capture; instead, we
            // select a capture mode which will infer the appropriate
            // resolution based on aspect ration and requested mode
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
        }.build()

        // Build the image capture use case and attach button click listener
        imageCapture = ImageCapture(imageCaptureConfig)

        // Bind use cases to lifecycle
        CameraX.bindToLifecycle(lifecycleOwner, preview, imageCapture)
    }

    fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when (viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }

    fun takePicture(file: File, onSuccess: (File) -> Unit) =
        imageCapture.takePicture(file, object: ImageCapture.OnImageSavedListener {
            override fun onError(
                error: ImageCapture.UseCaseError,
                message: String, exc: Throwable?
            ) {
                val msg = "Photo capture failed: $message"
                Log.e("CameraXApp", msg)
                exc?.printStackTrace()
            }

            override fun onImageSaved(file: File) {
                val msg = "Photo capture succeeded: ${file.absolutePath}"
                Log.d("CameraXApp", msg)
                onSuccess(file)
                // Clear CameraX
                CameraX.unbindAll()
            }
        })
}