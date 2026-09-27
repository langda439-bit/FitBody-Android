package com.fitbody.app.feature.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.graphics.BitmapFactory
import java.io.File

@Composable
fun FoodCameraScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val recognizer = remember<FoodRecognizer> { PlaceholderFoodRecognizer() }

    val imageCapture = remember { ImageCapture.Builder().build() }
    var results by remember { mutableStateOf<List<RecognizedFood>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val providerFuture = ProcessCameraProvider.getInstance(ctx)
                providerFuture.addListener({
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                enabled = !busy,
                onClick = {
                    busy = true
                    val photoFile = File(context.cacheDir, "food_${System.currentTimeMillis()}.jpg")
                    val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    imageCapture.takePicture(
                        options,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                scope.launch(Dispatchers.Default) {
                                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                                    results = if (bitmap != null) {
                                        recognizer.recognize(bitmap)
                                    } else emptyList()
                                    busy = false
                                }
                            }

                            override fun onError(exc: ImageCaptureException) {
                                busy = false
                            }
                        }
                    )
                }
            ) { Text(if (busy) "识别中…" else "拍照识别") }

            Spacer(Modifier.height(8.dp))
            if (results.isEmpty() && !busy) {
                Text("未能识别或尚未接入识别模型，请手动搜索/录入食物。")
            } else {
                results.forEach { food ->
                    Text(
                        "${food.name}  ${food.grams.toInt()}g  ${food.kcal.toInt()}kcal (${(food.confidence * 100).toInt()}%)",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Button(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("返回") }
        }
    }
}
