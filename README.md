# Scan Barcodes with ML Kit on Android without Google Mobile Services (GMS)

To use Android ML Kit for QR code scanning without relying on Google Mobile Services (GMS), you can utilize the ML Kit Barcode Scanning API, which is available as part of ML Kit's standalone libraries. This approach is ideal when you want to ensure that your app can function on devices without Google Play Services, such as in regions where GMS is not available.

Here's how to set up a QR code scanner using the ML Kit Barcode Scanning API without GMS:

## Step 1: Add Dependency
To use the standalone ML Kit Barcode Scanning API without GMS, add the following dependency to your build.gradle file:

```
dependencies {
    implementation 'com.google.mlkit:barcode-scanning:17.0.3'
}
```

Make sure you use a version of the library that is compatible with the standalone use case.

## Step 2: Set Up CameraX or Camera2
You'll need to set up a camera source. Using CameraX is simpler, but you can also use Camera2 if you want more control. Here's a basic setup using CameraX:

```kotlin
val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
cameraProviderFuture.addListener({
    val cameraProvider = cameraProviderFuture.get()
    
    // Set up the preview
    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider(previewView.surfaceProvider)
    }

    // Set up the image analysis use case with a custom analyzer
    val imageAnalysis = ImageAnalysis.Builder()
        .setTargetResolution(Size(1280, 720))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    imageAnalysis.setAnalyzer(cameraExecutor, YourBarcodeAnalyzer { barcode ->
        // Handle scanned barcode data here
    })

    // Bind use cases to the camera
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
    } catch(exc: Exception) {
        Log.e(TAG, "Use case binding failed", exc)
    }
}, ContextCompat.getMainExecutor(this))
```

## Step 3: Implement the Barcode Analyzer
You will need to create an ImageAnalysis.Analyzer to process camera frames and detect barcodes using the ML Kit Barcode Scanning API:

```kotlin
class YourBarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let {
                            onBarcodeDetected(it)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Barcode scanning failed", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
```

## Step 4: Handle Barcode Results
In the onBarcodeDetected callback, you can handle the detected QR codes as you wish, such as displaying them or performing other actions based on the QR code content.

## Step 5: Add Camera Permissions
Don't forget to add the necessary camera permissions to your AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

Also, request the camera permission at runtime if targeting Android 6.0 (API level 23) or higher.

### Key Points to Note
 - Using the standalone com.google.mlkit:barcode-scanning library means that your app does not rely on GMS, making it suitable for devices without Google Play Services.
 - CameraX is easier for most use cases, but Camera2 offers more control if needed.
 - Ensure you handle permission requests properly, especially on devices running Android 6.0 and above.
  
This setup should enable a QR code scanner in your Android app without relying on Google Mobile Services.