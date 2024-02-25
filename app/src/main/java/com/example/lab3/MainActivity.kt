package com.example.lab3

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.lab3.ui.theme.Lab3Theme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import android.Manifest
import android.hardware.camera2.CameraCharacteristics
import android.util.Log


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                MyScreenContent()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    MaterialTheme {
        Surface {
            content()
        }
    }
}

@SuppressLint("ServiceCast")
fun vibrate(context: android.content.Context) {
    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(500)
    }
}

@SuppressLint("ServiceCast")
fun toggleFlashlight(context: Context) {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    try {
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            val cameraIdList = cameraManager.cameraIdList
            var cameraId: String? = null
            for (id in cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (available != null && available && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    break
                }
            }
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, true)
            } else {
                Log.e("Flashlight", "No back camera with flashlight available")
            }
        } else {
            Log.e("Flashlight", "No flash available")
        }
    } catch (e: CameraAccessException) {
        e.printStackTrace()
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    } catch (e: Exception) {
        Log.e("Flashlight", "Exception: ${e.message}")
    }
}

@Composable
fun MyScreenContent() {
    var text by remember { mutableStateOf("") }
    var reversedText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val CAMERA_PERMISSION_REQUEST_CODE = 101
    val VIBRATE_PERMISSION_REQUEST_CODE = 102
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = text,
            onValueChange = { newText ->
                text = newText
            },
            label = { Text("Enter text") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                reversedText = text.reversed()
            }
        ) {
            Text("Reverse Text")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = reversedText)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, SecondActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            Text("Go to Second Screen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, ThirdActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            Text("Go to Third Screen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission is already granted, proceed to toggle flashlight
                    toggleFlashlight(context)
                } else {
                    // Request CAMERA permission
                    ActivityCompat.requestPermissions(
                        context as ThirdActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST_CODE
                    )
                }
            }
        ) {
            Text("Toggle Flashlight")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.VIBRATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission is already granted, proceed to vibrate
                    vibrate(context)
                } else {
                    // Request VIBRATE permission
                    ActivityCompat.requestPermissions(
                        context as ThirdActivity,
                        arrayOf(Manifest.permission.VIBRATE),
                        VIBRATE_PERMISSION_REQUEST_CODE
                    )
                }
            }
        ) {
            Text("Vibrate")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp {
        MyScreenContent()
    }
}