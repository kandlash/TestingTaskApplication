package com.example.clientapplication

import android.app.NotificationManager
import com.example.clientapplication.websocket.WebSocketClient
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.clientapplication.service.MyAccessibilityService
import com.example.clientapplication.ui.theme.TestingTaskApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


class MainActivity : ComponentActivity() {
    private var myAccessibilityServiceIntent: Intent? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRunService()
        setContent {
            var isConnected by remember { mutableStateOf(false) }
            TestingTaskApplicationTheme {
                MyScreen(
                    onConnectClick = {
                        if (!isConnected) {
                            start()
                        } else {
                            pause()
                        }
                        isConnected = !isConnected
                    },
                    buttonText = if (isConnected) "Pause" else "Start",
                    onConfigChanged = {ip, port ->
                        changeConfig(ip, port)
                     }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(myAccessibilityServiceIntent)
    }

    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(ComponentName(this, MyAccessibilityService::class.java).flattenToString())) {
                return true
            }
        }

        return false
    }

    private fun checkAndRunService(){
        if (!isAccessibilityServiceEnabled()) {
            requestAccessibilityPermission()
            return
        }
        myAccessibilityServiceIntent =  Intent(applicationContext, MyAccessibilityService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(myAccessibilityServiceIntent)
        } else {
            startService(myAccessibilityServiceIntent)
        }
    }

    private fun start() {
        Log.d("MainACt", "start pressed")
        sendBroadcast(Intent("com.example.clientapplication.CONNECT"))
        openChrome()
    }

    private fun changeConfig(ip: String?, port: String?){
        Log.d("MainAct", "changeConfig called")
        val intent = Intent("com.example.clientapplication.CHANGECONFIG")
        intent.putExtra("ip", ip)
        intent.putExtra("port", port)
        sendBroadcast(intent)
    }

    private fun pause() {
        Log.d("MainACt", "pause pressed")
        sendBroadcast(Intent("com.example.clientapplication.DISCONNECT"))
    }

    private fun openChrome() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
        intent.setPackage("com.android.chrome")
        startActivity(intent)
    }
}

@Composable
fun MyScreen(
    modifier: Modifier = Modifier,
    onConnectClick: () -> Unit,
    onConfigChanged: (ip: String?, port: String?) -> Unit,
    buttonText: String
) {
    val openDialog = remember { mutableStateOf(false) }
    val ipInput = remember { mutableStateOf("192.168.1.236") }
    val portInput = remember { mutableStateOf("8081") }

    fun updateConnectionConfig() {
        val ip = ipInput.value.toString()
        val port = portInput.value.toString()
        onConfigChanged(ip, port)
        openDialog.value = false
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { onConnectClick() }) {
            Text(buttonText)
        }
        Button(onClick = {openDialog.value = true}){
            Text("Config")
        }
    }

    if(openDialog.value){
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text("Connection Config") },
            text = {
                Column {
                    TextField(
                        value = ipInput.value,
                        onValueChange = {ipInput.value = it},
                        label = { Text("Ip") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text
                        )
                    )

                    TextField(
                        value = portInput.value,
                        onValueChange = { portInput.value = it },
                        label = { Text("Port") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text
                        )
                    )
                }


            },
            confirmButton = {
                Button(onClick = ::updateConnectionConfig) {
                    Text("Save")
                }
            }
        )
    }
}