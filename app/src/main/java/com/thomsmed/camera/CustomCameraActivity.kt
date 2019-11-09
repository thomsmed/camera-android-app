package com.thomsmed.camera

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.widget.Button

class CustomCameraActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    val REQUEST_PERMISSION_CAMERA = 1

    private lateinit var textureView: TextureView
    private lateinit var btnCapture: Button

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraAvailabilityCallback: CameraAvailabilityCallback
    private lateinit var cameraDeviceStateCallback: CameraDeviceStateCallback
    private lateinit var captureSessionStateCallback: CaptureSessionStateCallback
    private var cameraId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_camera)

        textureView = findViewById(R.id.output_camera)
        textureView.surfaceTextureListener = this

        btnCapture = findViewById(R.id.button_capture)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraAvailabilityCallback = CameraAvailabilityCallback(this)
        captureSessionStateCallback = CaptureSessionStateCallback()
        cameraDeviceStateCallback = CameraDeviceStateCallback(textureView, captureSessionStateCallback)

//        try {
//            cameraId = cameraManager.cameraIdList[0]
//        } catch (e: Exception) {
//            // Ignore...
//        }

        requestPermissions()
    }

    override fun onResume() {
        super.onResume()

        cameraManager.registerAvailabilityCallback(cameraAvailabilityCallback, null)
    }

    override fun onPause() {
        super.onPause()

        cameraManager.unregisterAvailabilityCallback(cameraAvailabilityCallback)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return  true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    @TargetApi(23)
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION_CAMERA)
        }
    }

    fun openCamera(cameraId: String) {
        if (Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        this.cameraId = cameraId
        cameraManager.openCamera(cameraId, cameraDeviceStateCallback, null)
    }

    fun closeCamera() {
        cameraId = null
    }









    class CameraAvailabilityCallback(private val customCameraActivity: CustomCameraActivity) : CameraManager.AvailabilityCallback() {

        private var lastCameraId: String? = null

        override fun onCameraUnavailable(cameraId: String) {
            if (lastCameraId != cameraId) {
                return
            }
            lastCameraId = null
            customCameraActivity.closeCamera()
        }

        override fun onCameraAvailable(cameraId: String) {
            lastCameraId = cameraId
            customCameraActivity.openCamera(cameraId)
        }
    }

    class CaptureSessionStateCallback : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onConfigured(session: CameraCaptureSession) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    class CameraDeviceStateCallback(private val textureView: TextureView, private val cameraCaptureStateCallback: CameraCaptureSession.StateCallback) : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            camera.createCaptureSession(listOf(Surface(textureView.surfaceTexture)), cameraCaptureStateCallback, null)
        }

        override fun onDisconnected(camera: CameraDevice) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onError(camera: CameraDevice, error: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

}
