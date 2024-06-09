package com.example.clientapplication.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.clientapplication.MainActivity
import com.example.clientapplication.websocket.WebSocketClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyAccessibilityService: AccessibilityService() {
    private var webSocketClient: WebSocketClient? = null
    private var ip: String = "192.168.1.236"
    private var port: String = "8081"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d("service", "Service is opened!")
        startForeground(1, createNotification())
        webSocketClient = WebSocketClient(this, ip, port)

        val filter = IntentFilter().apply {
            addAction("com.example.clientapplication.CONNECT")
            addAction("com.example.clientapplication.DISCONNECT")
            addAction("com.example.clientapplication.CHANGECONFIG")
        }
        registerReceiver(broadcastReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    private fun createNotification(): Notification {
        val channelId = "my_accessibility_service_channel"
        val channelName = "My Accessibility Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle("Service is running in the background")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        return notification
    }


    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.clientapplication.CONNECT" -> connectToServer()
                "com.example.clientapplication.CHANGECONFIG" -> changeConfig(intent)
                "com.example.clientapplication.DISCONNECT" -> disconnectFromServer()
            }
        }
    }

    fun changeConfig(intent: Intent){
        Log.d("Service", "changeConfig()")
        val ip = intent.getStringExtra("ip")
        val port = intent.getStringExtra("port")
        if (ip != null) {
            this.ip = ip
        }
        if (port != null) {
            this.port = port
        }
        if(webSocketClient == null){
            webSocketClient = WebSocketClient(this, this.ip, this.port)
        }
    }

    fun connectToServer(){
        Log.d("Service", "Service is using client to connect")
        if(webSocketClient == null){
            webSocketClient = WebSocketClient(this, this.ip, this.port)
        }
        webSocketClient?.connect()
    }

    fun disconnectFromServer() {
        Log.d("Service", "Service is using client to disconnect")
        try{
            webSocketClient?.disconnect()
        }catch (e: Error){
            Log.e("ServiceError", "${e.message}")
        }finally {
            webSocketClient = null
        }

    }

    fun performSwipe(length: Int, direction: String, speed: Int) {
        val displayMetrics = resources.displayMetrics
        val path = Path().apply {
            val centerY = displayMetrics.heightPixels / 2f
            val centerX = displayMetrics.widthPixels / 2f
            when (direction) {
                "up" -> {
                    val endY = Math.max(0f, centerY - length)
                    moveTo(centerX, centerY)
                    lineTo(centerX, endY)
                }
                "down" -> {
                    val endY = Math.min(displayMetrics.heightPixels.toFloat(), centerY + length)
                    moveTo(centerX, centerY)
                    lineTo(centerX, endY)
                }
            }
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, speed.toLong()))
            .build()
        val isDispatched = dispatchGesture(gesture, null, null)
        webSocketClient?.sendMessage(isDispatched)

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val source = event.source
                source?.let {
                    val packageName = source.packageName.toString()
                    if (packageName == "com.android.chrome") {
                        Log.d("event", "This is CHROME")

                    }
                }
            }
        }
    }

    override fun onInterrupt() {
    }
}