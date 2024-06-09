package com.example.testingtaskapplication.server

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.example.testingtaskapplication.MainViewModel
import com.example.testingtaskapplication.database.LogItem
import com.example.testingtaskapplication.database.MainDb
import com.google.gson.Gson
import io.ktor.server.application.*
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import io.ktor.server.websocket.*
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
@DelicateCoroutinesApi
class WebsocketServer(private val context: Context, private val db: MainDb) {
    private var port: Int = 8081
    private var server: ApplicationEngine? = null

    fun start() {
        GlobalScope.launch {
            server = embeddedServer(CIO, port) {
                install(WebSockets)
                install(CORS) {
                    anyHost()
                }
                routing {
                    webSocket("/startGettingGestureParams") {
                        while (true) {
                            val gestureParams = generateGestureParams()
                            val jsonParams = Gson().toJson(gestureParams)
                            send(Frame.Text(jsonParams))
                            delay(3000L)
                        }
                    }
                }
                routing {
                    webSocket ("/writeGestureDispatch"){
                        while(true){
                            val frame = incoming.receive()
                            if (frame is Frame.Text) {
                                Log.d("SERVER", "Got dispatch message: " + frame.readText())
                                val currentDate = LocalDateTime.now()
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                val dateString = currentDate.format(formatter)

                                val logItem = LogItem(id=null, date=dateString, logText = frame.readText())
                                Thread{
                                    db.getDao().insertLogItem(logItem)
                                }.start()
                            }
                        }
                    }
                }
            }.also { engine ->
                engine.start()
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Server is running on port: $port!",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun stop() {
        server?.stop(0, 0)
        server = null
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Server has been stopped!",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun updatePort(newPort: Int) {
        if(server != null){
            stop()
        }
        port = newPort
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Server's port was changed to: $port",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

}
