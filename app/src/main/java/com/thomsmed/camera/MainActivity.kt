package com.thomsmed.camera

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var btnPickImage: Button
    lateinit var btnDefaultCamera: Button
    lateinit var btnCustomCamera: Button

    val REQUEST_PERMISSION_CAMERA_AND_WRITE_EXTERNAL = 0
    val REQUEST_PERMISSION_CAMERA = 1
    val REQUEST_PERMISSION_WRITE_EXTERNAL = 2
    val REQUEST_PICK_IMAGE = 3
    val REQUEST_IMAGE_CAPTURE = 4
    val REQUEST_CUSTOM_IMAGE_CAPTURE = 5

    lateinit var pickImageHandler: PickImageHandler
    lateinit var defaultCameraHandler: DefaultCameraHandler
    lateinit var customCameraHandler: CustomCameraHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.image_camera)

        btnPickImage = findViewById(R.id.button_pick_image)
        pickImageHandler = PickImageHandler(this)
        btnPickImage.setOnClickListener(pickImageHandler)

        btnDefaultCamera = findViewById(R.id.button_default)
        defaultCameraHandler = DefaultCameraHandler(this)
        btnDefaultCamera.setOnClickListener(defaultCameraHandler)

        btnCustomCamera = findViewById(R.id.button_custom)
        customCameraHandler = CustomCameraHandler(this)
        btnCustomCamera.setOnClickListener(customCameraHandler)

        requestPermissions()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICK_IMAGE -> {
                handlePickImageRequest(resultCode, data)
            }
            REQUEST_IMAGE_CAPTURE -> {
                handleImageCaptureRequest(resultCode, data)
            }
            REQUEST_CUSTOM_IMAGE_CAPTURE -> {
                handleCustomImageCaptureRequest(resultCode, data)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_WRITE_EXTERNAL -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Set some flags, do som stuff...
                }
            }
            REQUEST_PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Set some flags, do som stuff...
                }
            }
            REQUEST_PERMISSION_CAMERA_AND_WRITE_EXTERNAL -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Set some flags, do som stuff...
                }
            }
        }
    }

    @TargetApi(23)
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            return
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA),
                REQUEST_PERMISSION_CAMERA_AND_WRITE_EXTERNAL)
        }
    }

    private fun handlePickImageRequest(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        data?.let {intent ->
            val thumbnail: Bitmap? = intent.getParcelableExtra("data")

            intent.data?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream))
                } catch (e: Exception) {
                    // Ignore...
                }
            }
        }
    }

    private fun handleImageCaptureRequest(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val thumbnail: Bitmap? = data?.getParcelableExtra("data")

        // After getting the image it might also be meaningful to add the image to the media library (Intent with Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        try {
            defaultCameraHandler.imageUri?.let {
                val inputStream = contentResolver.openInputStream(it)
                imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream))
            }
        } catch (e: Exception) {
            // Ignore...
        }
    }

    private fun handleCustomImageCaptureRequest(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val thumbnail: Bitmap? = data?.getParcelableExtra("data")

        // After getting the image it might also be meaningful to add the image to the media library (Intent with Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        try {
            customCameraHandler.imageUri?.let {
                val inputStream = contentResolver.openInputStream(it)
                imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream))
            }
        } catch (e: Exception) {
            // Ignore...
        }
    }

    inner class PickImageHandler(private val activity: Activity) : View.OnClickListener {

        override fun onClick(v: View?) {
            Intent(Intent.ACTION_GET_CONTENT).also { pickImageIntent ->
                pickImageIntent.addCategory(Intent.CATEGORY_OPENABLE)
                pickImageIntent.type = "image/*"
                activity.startActivityForResult(pickImageIntent, REQUEST_PICK_IMAGE)
            }
        }
    }

    inner class DefaultCameraHandler(private val activity: Activity) : View.OnClickListener {

        private val contentUriService: ContentUriService = ContentUriService(activity)

        var imageUri: Uri? = null
            private set

        override fun onClick(v: View?) {
            if (!activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                return
            }

            val timeStamp =  SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val filePrefix = "image_${timeStamp}"
            val fileSuffix = ".jpg"
            val mimeType = "image/jpg"

            imageUri = contentUriService.createExternalMediaStoreContentUri(filePrefix, mimeType)

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(activity.packageManager)?.also {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    takePictureIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION // Note: Seems like the default Camera app don't really need this permission?
                    activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    inner class CustomCameraHandler(private val activity: Activity) : View.OnClickListener {

        private val contentUriService: ContentUriService = ContentUriService(activity)

        var imageUri: Uri? = null
            private set

        override fun onClick(v: View?) {
            val timeStamp =  SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val filePrefix = "image_${timeStamp}"
            val fileSuffix = ".jpg"
            val mimeType = "image/jpg"

            imageUri = contentUriService.createExternalMediaStoreContentUri(filePrefix, mimeType)

            Intent(activity, CustomCameraActivity::class.java).also { takeCustomPictureIntent ->
                takeCustomPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                activity.startActivityForResult(takeCustomPictureIntent, REQUEST_CUSTOM_IMAGE_CAPTURE)
            }
        }

    }
}
