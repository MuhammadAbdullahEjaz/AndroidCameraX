package com.example.camera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.camera.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import android.provider.MediaStore
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContentResolverCompat.query

typealias LumaListener = (luma: Double) -> Unit


class MainActivity : AppCompatActivity() {
    val viewModel: MainViewModel by viewModels()

    val imageSelectionActivity =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { it ->
            it.forEach {
                Log.d("fetch", "$it")
            }
            viewModel.updateImages(it)
        }

    private lateinit var viewBinding: ActivityMainBinding
    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null
    private var imageCapture: ImageCapture? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private val TAG = "fetch"
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        val adapter = AddImageRvAdapter(this)
        viewModel.images.observe(this) {
            Log.d(TAG, "data changed: data: $it")
            adapter.updateData(it)
        }
        viewBinding.addImageRV.adapter = adapter

        //FlashButton Click Listener
        viewBinding.flashB.setOnClickListener {
            if (cameraInfo?.torchState?.value == 1) {
                cameraControl?.enableTorch(false)
                viewBinding.flashB.setImageResource(R.drawable.ic_baseline_flash_off_24)
            } else {
                cameraControl?.enableTorch(true)
                viewBinding.flashB.setImageResource(R.drawable.ic_baseline_flash_on_24)
            }
            Log.d(TAG, "flash: ${cameraInfo?.torchState?.value}")
        }

        //CameraButton Click Listener (Change switch camera FRONT/BACK)
        viewBinding.cameraB.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera()
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener {
            takePhoto()
        }

        //GalleryButton Click Listener (Selecting images from gallery)
        viewBinding.gallery.setOnClickListener {

            val images:MutableList<Uri> = mutableListOf()

            Log.d("fetch","onGalleryClick")
            val query = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                ),
                null,
                null,
                null
            )
            query?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
                val displayNameColumn =
                    it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                Log.d("fetch","dataCount: ${it.count}")
                while (it.moveToNext()) {

                    val id = it.getLong(idColumn)
                    val displayName = it.getString(displayNameColumn)
                    val contentUris = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    images.add(contentUris)
                    Log.d("fetch", "Images: $id, $displayName, $contentUris")
                }
                it.close()
            }

            viewModel.updateImages(images)

//            Accessing images by gallery with system intent
//            imageSelectionActivity.launch("image/jpeg")

        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    viewModel.updateImages(listOf(savedUri!!))
                    val msg = "Photo capture succeeded: ${savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture
                .Builder()
                .build()

            // Select back camera as a default
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}

