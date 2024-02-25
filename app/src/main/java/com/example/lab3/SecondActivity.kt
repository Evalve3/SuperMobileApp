package com.example.lab3

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.LocationServices
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                MySecondScreenContent()
            }
        }
    }
}

private fun parseWeatherData(json: String): String {
    // Parse JSON response to extract relevant weather data
    val jsonObject = JSONObject(json)
    val cityName = jsonObject.getString("name")
    val weatherArray = jsonObject.getJSONArray("weather")
    val weatherObject = weatherArray.getJSONObject(0)
    val description = weatherObject.getString("description")
    val temperatureKelvin = jsonObject.getJSONObject("main").getDouble("temp")
    val temperatureCelsius = temperatureKelvin - 273.15 // Convert temperature to Celsius
    val humidity = jsonObject.getJSONObject("main").getInt("humidity")

    return "City: $cityName\nWeather: $description\nTemperature: ${
        String.format(
            "%.2f",
            temperatureCelsius
        )
    } °C\nHumidity: $humidity%"
}

@SuppressLint("MissingPermission")
fun getLocation(context: android.content.Context, onWeatherDataReceived: (String) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            // Got last known location. In some rare situations, this can be null.
            if (location != null) {
                // Fetch weather data based on location
                val latitude = location.latitude
                val longitude = location.longitude
                val apiKey = "e4293a4a6b35e295ba3c91f75607ec81"
                val url =
                    "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey"

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.body?.let { responseBody ->
                            val jsonResponse = responseBody.string()
                            val weatherData = parseWeatherData(jsonResponse)
                            onWeatherDataReceived(weatherData)
                        }
                    }
                })
            }
        }
}

@Composable
fun MySecondScreenContent() {
    var text by remember { mutableStateOf("") }
    var shuffledText by remember { mutableStateOf("") }
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("") }

    val LOCATION_PERMISSION_REQUEST_CODE = 103

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
                shuffledText = text.toList().shuffled().joinToString(separator = "")
            }
        ) {
            Text("Shuffle Text")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = shuffledText)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            Text("Go to Main Screen")
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

        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission is already granted, proceed to get location
                    getLocation(context) { location ->
                        locationText = location
                    }
                } else {
                    // Request LOCATION permission
                    ActivityCompat.requestPermissions(
                        context as ThirdActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        ) {
            Text("Get Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = locationText)

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    MyApp {
        MySecondScreenContent()
    }
}
