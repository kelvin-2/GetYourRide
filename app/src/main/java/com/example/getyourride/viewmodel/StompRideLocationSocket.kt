package com.example.getyourride.viewmodel

import com.example.getyourride.UserSession
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
        onStopUpdate: (stopId: Long) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isConnecting) return
        isConnecting = true

        scope.launch {
            var retryDelay = 2000L
            val maxDelay = 30000L

            while (isActive) {
                try {
                    // The /ws handshake goes through the same Spring Security filter chain as any
                    // REST call and requires "Authorization: Bearer <token>" (see JwtAuthFilter).
                    // A plain OkHttpClient() sends no such header, so the handshake gets rejected
                    // once the endpoint actually enforces authentication - attach the same
                    // interceptor pattern used for Retrofit in NetworkModule.
                    val authInterceptor = okhttp3.Interceptor { chain ->
                        val original = chain.request()
                        val token = UserSession.token
                        val request = if (token != null) {
                            original.newBuilder()
                                .addHeader("Authorization", "Bearer $token")
                                .build()
                        } else {
                            original
                        }
                        chain.proceed(request)
                    }
                    val httpClient = OkHttpClient.Builder()
                        .addInterceptor(authInterceptor)
                        .build()
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
                                    // Wire format is flat, per LocationUpdateDTO and
                                    // TrackingMessageContractTest on the backend:
                                    // {"type":"LOCATION_UPDATE","tripId":42,"lat":-33.96,"lng":25.61,"legIndex":1}
                                    if (json.has("lat") && json.has("lng")) {
                                        val update = DriverLocationUpdate(
                                            latitude = json.get("lat").asDouble,
                                            longitude = json.get("lng").asDouble,
                                            heading = 0f,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        withContext(Dispatchers.Main) {
                                            onUpdate(update)
                                        }
                                    }
                                }
                                "STOP_EVENT" -> {
                                    // Wire format per StopEventDTO:
                                    // {"type":"STOP_EVENT","tripId":42,"stopId":7,"status":"ARRIVED"}
                                    // stopId is trip_stop.id, not a list index - callers that need a
                                    // position in the stops list must resolve it themselves.
                                    val stopId = if (json.has("stopId")) json.get("stopId").asLong else 0L
                                    withContext(Dispatchers.Main) {
                                        onStopUpdate(stopId)
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