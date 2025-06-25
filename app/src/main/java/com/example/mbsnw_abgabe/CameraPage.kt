package com.example.mbsnw_abgabe

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.Alignment
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.core.ImageAnalysis
import com.example.mbsnw_abgabe.data.Meal
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.camera.core.CameraSelector
import com.example.mbsnw_abgabe.data.MealDatabase

@Composable
fun CameraPage(mealDB: MealDatabase, onMealScanned: (Meal) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val barcodeToMeal = remember {
        mapOf(
            "123456789" to Meal(
                id = 0,
                name = "Pizza Margherita",
                cal = 266.0,
                date = dateFormat.format(Date())
            ),
            "987654321" to Meal(
                id = 0,
                name = "Chicken Salad",
                cal = 180.0,
                date = dateFormat.format(Date())
            )
        )
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    // Request permission on first composition if not granted
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Camera implementation
    if (hasPermission) {
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                MlKitAnalyzer(
                                    listOf(BarcodeScanning.getClient()),
                                    ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                                    ContextCompat.getMainExecutor(context)
                                ) { result ->
                                    val barcodeResults = result.getValue(BarcodeScanning.getClient())
                                    if (barcodeResults != null && barcodeResults.isNotEmpty()) {
                                        val barcode = barcodeResults[0]
                                        val scannedValue = barcode.rawValue
                                        if (scannedValue != null) {
                                            val mealTemplate = barcodeToMeal[scannedValue]
                                            if (mealTemplate != null) {
                                                // Create new meal with current date
                                                val scannedMeal = mealTemplate.copy(
                                                    date = dateFormat.format(Date())
                                                )
                                                scope.launch {
                                                    onMealScanned(scannedMeal)
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Barcode-Scanner",
            style = MaterialTheme.typography.titleLarge
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            if (hasPermission) {
                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner, cameraSelector, preview
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(context))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("Kamera-Berechtigung nicht erteilt.")
            }
        }

        Button(
            onClick = {
                if (hasPermission) {
                    println("📷 Barcode Scan gestartet (Platzhalter)")
                    // TODO: Start scanning logic and save to DB
                } else {
                    println("❌ Keine Kamera-Berechtigung")
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        ) {
            Text("📷 Scan starten")
        }
    }
}