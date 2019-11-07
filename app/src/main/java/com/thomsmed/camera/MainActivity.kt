package com.thomsmed.camera

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var btnDefaultCamera: Button
    lateinit var btnCustomCamera: Button

    val REQUEST_IMAGE_CAPTURE = 1

    lateinit var defaultCameraHandler: DefaultCameraHandler
    lateinit var customCameraHandler: CustomCameraHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.image_camera)

        btnDefaultCamera = findViewById(R.id.button_default)
        defaultCameraHandler = DefaultCameraHandler(this)
        btnDefaultCamera.setOnClickListener(defaultCameraHandler)

        btnCustomCamera = findViewById(R.id.button_custom)
        customCameraHandler = CustomCameraHandler(this)
        btnCustomCamera.setOnClickListener(customCameraHandler)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_IMAGE_CAPTURE || resultCode != Activity.RESULT_OK) {
            return
        }

        var imageBitmap = data?.extras?.get("data") as Bitmap
        imageBitmap?.let {
            imageView.setImageURI(defaultCameraHandler.imageUri)
        }
    }


    inner class DefaultCameraHandler(private val activity: Activity) : View.OnClickListener {

        var imageUri: Uri? = null

        override fun onClick(v: View?) {
            if (!activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                return
            }

            val timeStamp =  SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imagesDir = File(activity.getExternalFilesDir(null), "images")
            //val newFile = File(activity.filesDir, "image_${timeStamp}") // internal app storage
            val newFile = File(imagesDir,"image_${timeStamp}") // external app storage

            try {
                imageUri = FileProvider.getUriForFile(activity, "com.thomsmed.camera.fileprovider", newFile)
            } catch (e: IllegalArgumentException) {
                return
            }

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(activity.packageManager)?.also {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    inner class CustomCameraHandler(private val activity: Activity) : View.OnClickListener {

        override fun onClick(v: View?) {
            if (!activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                return
            }


        }
    }
}
