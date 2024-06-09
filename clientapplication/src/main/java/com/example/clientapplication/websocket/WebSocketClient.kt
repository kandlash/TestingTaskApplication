package com.example.clientapplication.websocket

import android.os.Build
import android.util.Log
import com.example.clientapplication.service.GestureParams
import com.example.clientapplication.service.MyAccessibilityService
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.*


class WebSocketClient(private val myAccessibilityService: MyAccessibilityService?,
                      private val ip: String,
                      private val port: String) {

    private var client: HttpClient? = null
    private var session: WebSocketSession? = null

    fun connect() {
        Log.d("connect", "connect is init on $ip:$port")
        client = HttpClient(CIO) {
            install(WebSockets) {
                pingInterval = 20_000
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                client!!.webSocket(method = HttpMethod.Get, host = ip, port = port.toInt(), path = "/startGettingGestureParams") {
                    session = this
                    while (true) {
                        val othersMessage = incoming.receive() as? Frame.Text
                        othersMessage?.let {
                            val message = it.readText()
                            val gestureParams = Gson().fromJson(message, GestureParams::class.java)
                            Log.d("performSwipeFromClient",
                                "performedSwipe ${gestureParams.length}," +
                                        " ${gestureParams.direction}," +
                                        " ${gestureParams.speed}")
                            myAccessibilityService?.performSwipe(gestureParams.length, gestureParams.direction, gestureParams.speed)
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.d("WebSocketClient", "Connection error: ${e.message}")
                }
            }
        }
    }

    fun disconnect() {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                session?.close()
                client?.close()
                Log.d("disconnect", "WebSocket and HttpClient closed")
            } catch (e: Exception) {
                Log.d("WebSocketClient", "Disconnection error: ${e.message}")
            }
        }
    }

    fun sendMessage(isDispatched: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            var session: DefaultClientWebSocketSession? = null
            try {
                val deviceModel = Build.MANUFACTURER + " " + Build.MODEL

                val message = if (isDispatched) "Gesture was dispatched! Device model: $deviceModel" else "Gesture wasn't dispatched! Device model: $deviceModel"

                client!!.webSocket(method = HttpMethod.Get, host = ip, port = port.toInt(), path = "/writeGestureDispatch") {
                    send(Frame.Text(message))
                    session = this
                    close()
                }
                session?.close()
            } catch (e: Exception) {
                Log.d("WebSocketClient", "Send message error: ${e.message}")
            }
        }
    }
}

