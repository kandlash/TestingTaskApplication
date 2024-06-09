package com.example.testingtaskapplication

import android.content.Intent
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.testingtaskapplication.server.WebsocketServer
import com.example.testingtaskapplication.ui.theme.TestingTaskApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import javax.inject.Inject


@DelicateCoroutinesApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var server: WebsocketServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestingTaskApplicationTheme {
                ServerControlButtons(
                    onStartClick = { server.start() },
                    onStopClick = { server.stop() },
                    onConfigClick = {
                        if (it != null) {
                            server.updatePort(it)
                        }
                    },
                    onLogsClick = { openLogs() }
                )
            }
        }
    }

    private fun openLogs() {
        val intent = Intent(this, LogActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun ServerControlButtons(
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onConfigClick: (Int?) -> Unit,
    onLogsClick: () -> Unit
) {
    val isServerRunning = remember { mutableStateOf(false) }
    val openDialog = remember { mutableStateOf(false) }
    val portInput = remember { mutableStateOf("8081") }

    fun updatePort() {
        val port = portInput.value.toIntOrNull()
        onConfigClick(port)
        openDialog.value = false
        if (isServerRunning.value) {
            isServerRunning.value = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (!isServerRunning.value) {
            Button(
                modifier = Modifier.wrapContentSize(),
                onClick = {
                    onStartClick()
                    isServerRunning.value = true
                }
            ) {
                Text("Start Server")
            }
        } else {
            Button(
                modifier = Modifier.wrapContentSize(),
                onClick = {
                    onStopClick()
                    isServerRunning.value = false
                }
            ) {
                Text("Stop Server")
            }
        }
        Button(onClick = { openDialog.value = true }) {
            Text("Config")
        }

        Button(onClick = { onLogsClick() }) {
            Text("Logs")
        }
    }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text("Server Config") },
            text = {
                TextField(
                    value = portInput.value,
                    onValueChange = { portInput.value = it },
                    label = { Text("Port") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.NumberPassword
                    )
                )
            },
            confirmButton = {
                Button(onClick = ::updatePort) {
                    Text("Save")
                }
            }
        )
    }
}
