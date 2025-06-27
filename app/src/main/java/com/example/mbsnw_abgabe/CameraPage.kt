package com.example.mbsnw_abgabe

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.mbsnw_abgabe.data.Meal
import com.example.mbsnw_abgabe.data.MealDatabase
import com.example.mbsnw_abgabe.data.MealRepository
import com.google.mlkit.vision.barcode.BarcodeScanning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPage(mealDB: MealDatabase,navController: NavController, onMealScanned: (Meal) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val repository = remember { MealRepository(mealDB.dao) }
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

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val barcodeScanner = remember { BarcodeScanning.getClient() }

    var hasScanned by remember { mutableStateOf(false) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Scan") },
            navigationIcon = {
                IconButton(onClick = { /* TODO: Open drawer */ }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            }
        )
    }) {
        (Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                if (hasPermission && !hasScanned) {  // Only show scanner if not yet scanned
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
                                                listOf(barcodeScanner),
                                                ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                                                ContextCompat.getMainExecutor(context)
                                            ) { result ->
                                                if (!hasScanned) {  // Only process if not yet scanned
                                                    val barcodeResults =
                                                        result.getValue(barcodeScanner)
                                                    if (barcodeResults != null && barcodeResults.isNotEmpty()) {
                                                        val barcode = barcodeResults[0]
                                                        val scannedValue = barcode.rawValue
                                                        if (scannedValue != null) {
                                                            val mealTemplate =
                                                                barcodeToMeal[scannedValue]
                                                            if (mealTemplate != null) {
                                                                val scannedMeal = mealTemplate.copy(
                                                                    date = dateFormat.format(Date())
                                                                )
                                                                scope.launch(Dispatchers.IO) {
                                                                    try {
                                                                        onMealScanned(scannedMeal)
                                                                        hasScanned =
                                                                            true  // Mark as scanned
                                                                        // Navigate back after successful scan
                                                                        scope.launch(Dispatchers.Main) {
                                                                            navController.navigate("home")
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        Log.e(
                                                                            "Database",
                                                                            "Error adding meal: ${e.message}"
                                                                        )
                                                                    }
                                                                }
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
                } else if (hasScanned) {
                    Text("Barcode successfully scanned!")
                } else {
                    Text("Kamera-Berechtigung nicht erteilt.")
                }
            }

            Button(
                onClick = {
                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            ) {
                Text(if (hasPermission) "📷 Scanning..." else "📷 Kamera aktivieren")
            }
        })
    }
}