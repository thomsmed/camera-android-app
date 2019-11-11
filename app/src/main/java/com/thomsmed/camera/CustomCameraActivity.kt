package com.thomsmed.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.Toast
import java.util.*

class CustomCameraActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private lateinit var textureView: TextureView
    private lateinit var btnCapture: Button
    private lateinit var btnOk: Button
    private lateinit var btnCancel: Button

    private lateinit var imageReader: ImageReader

    private lateinit var cameraManager: CameraManager
    private val cameraAvailabilityCallback = object : CameraManager.AvailabilityCallback() {
        override fun onCameraUnavailable(cameraId: String) {
            super.onCameraUnavailable(cameraId)
        }

        override fun onCameraAccessPrioritiesChanged() {
            super.onCameraAccessPrioritiesChanged()
        }

        override fun onCameraAvailable(cameraId: String) {
            if (cameraId != selectedCameraId) {
                return
            }

            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            cameraManager.openCamera(selectedCameraId!!, cameraDeviceStateCallback, null)
        }
    }
    private val cameraDeviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            selectedCameraDevice = camera

            selectedCameraDevice!!.createCaptureSession(listOf(Surface(textureView.surfaceTexture), imageReader.surface), captureSessionStateCallback, null)
        }

        override fun onDisconnected(camera: CameraDevice) {

        }

        override fun onError(camera: CameraDevice, error: Int) {

        }
    }
    private val captureSessionStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {

        }

        override fun onConfigured(session: CameraCaptureSession) {
            activeCaptureSession = session
            val repeatingCaptureRequest = selectedCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(Surface(textureView.surfaceTexture))
            }.build()
            activeCaptureSession!!.setRepeatingRequest(repeatingCaptureRequest, null, null)
        }
    }
    private val captureSessionCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureSequenceAborted(session: CameraCaptureSession, sequenceId: Int) {
            super.onCaptureSequenceAborted(session, sequenceId)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
        }

        override fun onCaptureFailed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            failure: CaptureFailure
        ) {
            super.onCaptureFailed(session, request, failure)
        }

        override fun onCaptureSequenceCompleted(
            session: CameraCaptureSession,
            sequenceId: Int,
            frameNumber: Long
        ) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber)
        }

        override fun onCaptureStarted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            timestamp: Long,
            frameNumber: Long
        ) {
            super.onCaptureStarted(session, request, timestamp, frameNumber)
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            super.onCaptureProgressed(session, request, partialResult)
        }

        override fun onCaptureBufferLost(
            session: CameraCaptureSession,
            request: CaptureRequest,
            target: Surface,
            frameNumber: Long
        ) {
            super.onCaptureBufferLost(session, request, target, frameNumber)
        }
    }
    private var activeCaptureSession: CameraCaptureSession? = null
    private var selectedCameraDevice: CameraDevice? = null
    private var selectedCameraId: String? = null
    private var imageUri: Uri? = null
    private val captureBtnCallback = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if (activeCaptureSession == null || selectedCameraDevice == null || activeCaptureSession == null) {
                return
            }

            btnCapture.visibility = View.GONE

            activeCaptureSession!!.stopRepeating()

            val captureRequest = selectedCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(Surface(textureView.surfaceTexture))
                addTarget(imageReader.surface)
            }.build()
            activeCaptureSession!!.capture(captureRequest, captureSessionCaptureCallback, null)
        }
    }
    private val okBtnCallback = object : View.OnClickListener {
        override fun onClick(v: View?) {
            btnOk.visibility = View.GONE
            btnCancel.visibility = View.GONE
            imageReader.acquireLatestImage()?.let { image ->
                // NB! IO should be done i a separate thread
                if (imageUri == null) {
                    return@let
                }

                try {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)

                    contentResolver.openOutputStream(imageUri!!)?.apply {
                        write(bytes)
                    }

                    image.close()

                    setResult(Activity.RESULT_OK)
                } catch (e: Exception) {
                    // Ignore...
                }
            }
            finish()
        }
    }
    private val cancelBtnCallback = object : View.OnClickListener {
        override fun onClick(v: View?) {
            btnCapture.visibility = View.VISIBLE
            btnOk.visibility = View.GONE
            btnCancel.visibility = View.GONE
            val repeatingCaptureRequest = selectedCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(Surface(textureView.surfaceTexture))
            }.build()
            activeCaptureSession!!.setRepeatingRequest(repeatingCaptureRequest, null, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_camera)

        imageUri = intent.extras?.get(MediaStore.EXTRA_OUTPUT) as Uri?

        textureView = findViewById(R.id.output_camera)
        textureView.surfaceTextureListener = this

        btnCapture = findViewById(R.id.button_capture)
        btnCapture.setOnClickListener(captureBtnCallback)
        btnOk = findViewById(R.id.button_capture_ok)
        btnOk.setOnClickListener(okBtnCallback)
        btnCancel = findViewById(R.id.button_capture_cancel)
        btnCancel.setOnClickListener(cancelBtnCallback)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        var largesImageSize: Size = Size(128, 128)
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                if (characteristics[CameraCharacteristics.LENS_FACING] == CameraMetadata.LENS_FACING_BACK) {
                    selectedCameraId = cameraId
                    val streamConfigurationMap = characteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP] as StreamConfigurationMap
                    val outputSizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG)
                    largesImageSize = Collections.max(outputSizes.toMutableList()) { lhs, rhs ->
                        (lhs!!.width.toLong() * lhs.height - rhs!!.width.toLong() * rhs.height).toInt()
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore...
        }

        imageReader = ImageReader.newInstance(largesImageSize.width, largesImageSize.height, ImageFormat.JPEG, 2) // maxImages = 2 is recommended when using acquireLatestImage()
        imageReader.setOnImageAvailableListener({
            btnOk.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE
        }, null)
    }

    override fun onResume() {
        super.onResume()

        cameraManager.registerAvailabilityCallback(cameraAvailabilityCallback, null)
    }

    override fun onPause() {
        super.onPause()

        cameraManager.unregisterAvailabilityCallback(cameraAvailabilityCallback)
    }

    override fun onDestroy() {
        super.onDestroy()

        activeCaptureSession?.close()
        selectedCameraDevice?.close()
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

}
