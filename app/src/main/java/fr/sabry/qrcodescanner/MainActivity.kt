package fr.sabry.qrcodescanner

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import fr.sabry.qrcodescanner.databinding.ActivityMainBinding
import timber.log.Timber
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val cameraPermission = android.Manifest.permission.CAMERA

    private lateinit var cameraSelector: CameraSelector
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    private val cameraViewModel = viewModels<CameraViewModel>()
    private var dialog: Boolean = false
    private var flashOn = false

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Timber.treeCount == 0) Timber.plant(Timber.DebugTree())
        Timber.i("===> Start ${getString(R.string.app_name)}")

        binding.version.text = "(Version ${ packageManager.getPackageInfo(packageName, 0).versionName })"

        cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        binding.torch.setOnClickListener {
            torch()
        }

        if (isPermissionGranted(cameraPermission)) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest(
                    positive = { openPermissionSetting() }
                )
            }
            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }

    private fun startCamera() {
        cameraViewModel.value.processCameraProvider.observe(this) { provider ->
            processCameraProvider = provider
            bindCameraPreview()
            bindInputAnalyser()
        }
    }

    private fun bindCameraPreview() {
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)
        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
        } catch (illegalStateException: IllegalStateException) {
            Timber.e(illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Timber.e(illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    private fun bindInputAnalyser() {
        val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .enableAllPotentialBarcodes()
                .build()
        )
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(barcodeScanner, imageProxy)
        }

        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
        } catch (illegalStateException: IllegalStateException) {
            Timber.e(illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Timber.e(illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if ((barcode.rawValue != null) && barcode.rawValue!!.isNotEmpty()) {
                        showBarcodeInfo(barcode)
                        dialog = true
                    }
                }
            }
            .addOnFailureListener {
                Timber.e(it.message ?: it.toString())
            }.addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun torch() {
        flashOn = !flashOn
        val id = if (flashOn) R.drawable.ic_torch_on else R.drawable.ic_torch_off
        binding.torch.setImageDrawable(ContextCompat.getDrawable(this, id))
        try {
            // Bind use cases to lifecycleOwner
            val cam = processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview, imageAnalysis)
            cam.cameraControl.enableTorch(flashOn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showBarcodeInfo(barcode: Barcode) {
        if(dialog) return
        beep()
        vibrate(this)
        AlertDialog.Builder(this)
            .setTitle("${getBarcodeFormat(barcode.format)} / ${getBarcodeType(barcode.valueType)}")
            .setCancelable(false)
            .setMessage(barcode.rawValue)
            .setPositiveButton("OK") { dialog, which ->
                this@MainActivity.dialog = false
            }
            .show()
    }

}