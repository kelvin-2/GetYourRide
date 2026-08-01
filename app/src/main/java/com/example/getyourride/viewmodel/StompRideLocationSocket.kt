package com.example.getyourride.viewmodel

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import okhttp3.OkHttpClient

/**
 * Real implementation of RideLocationSocket using STOMP over WebSockets.
 * Connects to the backend and subscribes to trip updates.
 * Includes automatic reconnection logic.
 */
class StompRideLocationSocket(
    private val baseUrl: String = "ws://10.0.2.2:8080/ws" // Default to local backend for emulator
) : RideLocationSocket {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var session: StompSession? = null
    private val gson = Gson()
    private var isConnecting = false

    override fun connect(
        rideId: String,
        onUpdate: (DriverLocationUpdate) -> Unit,
        onStopUpdate: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isConnecting) return
        isConnecting = true
        
        scope.launch {
            var retryDelay = 2000L
            val maxDelay = 30000L
            
            while (isActive) {
                try {
                    val httpClient = OkHttpClient()
                    val client = StompClient(OkHttpWebSocketClient(httpClient))
                    
                    session = client.connect(baseUrl)
                    isConnecting = false
                    retryDelay = 2000L // Reset delay on success
                    
                    // Subscribe to the trip topic
                    val subscription = session?.subscribeText("/topic/trip/$rideId")
                    
                    subscription?.collect { message ->
                        try {
                            val json = gson.fromJson(message, JsonObject::class.java)
                            val type = if (json.has("type")) json.get("type").asString else null
                            
                            when (type) {
                                "LOCATION_UPDATE" -> {
                                    if (json.has("data")) {
                                        val data = json.getAsJsonObject("data")
                                        val update = DriverLocationUpdate(
                                            latitude = data.get("latitude").asDouble,
                                            longitude = data.get("longitude").asDouble,
                                            heading = if (data.has("heading")) data.get("heading").asFloat else 0f,
                                            timestamp = if (data.has("timestamp")) data.get("timestamp").asLong else System.currentTimeMillis()
                                        )
                                        withContext(Dispatchers.Main) {
                                            onUpdate(update)
                                        }
                                    }
                                }
                                "STOP_EVENT" -> {
                                    val stopIndex = if (json.has("stopIndex")) json.get("stopIndex").asInt else 0
                                    withContext(Dispatchers.Main) {
                                        onStopUpdate(stopIndex)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            System.err.println("Error parsing socket message: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    isConnecting = false
                    withContext(Dispatchers.Main) {
                        onError("Socket connection failed: ${e.message}. Retrying...")
                    }
                    delay(retryDelay)
                    retryDelay = (retryDelay * 2).coerceAtMost(maxDelay)
                }
            }
        }
    }

    override fun disconnect() {
        scope.launch {
            session?.disconnect()
            session = null
            scope.cancel()
        }
    }
}
