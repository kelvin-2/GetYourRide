// ─────────────────────────────────────────────────────────────────────────────
// NetworkModule.kt
// Package: com.example.getyourride.di
//
// PURPOSE — One place that builds Retrofit + the API interfaces.
// Add new Api interfaces here as you build more controllers
// (e.g. DriverAuthApi, RideApi, etc.)
//
// ⚠️ IMPORTANT — BASE_URL explained:
//   "10.0.2.2" is a special alias the ANDROID EMULATOR uses to reach
//   "localhost" on YOUR computer. It is NOT a real IP address — don't
//   try to ping it or use it on a physical phone.
//
//   - Testing on the Android EMULATOR  → use 10.0.2.2
//   - Testing on a REAL physical phone → use your PC's actual LAN IP
//     (run `ipconfig` on Windows, look for IPv4 Address, e.g. 192.168.1.42)
//     AND make sure your phone is on the same WiFi network as your PC.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.di

import com.example.getyourride.data.remote.api.StudentAuthApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    // Your Spring Boot server runs on port 8080 (set in application.properties)
    private const val BASE_URL = "http://localhost:8080/"

    // Logs full request/response bodies to Logcat — invaluable for debugging
    // 400/401 errors. Filter Logcat by tag "OkHttp" to see them.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ── Expose API interfaces here — add more as the backend grows ───────────
    val studentAuthApi: StudentAuthApi by lazy {
        retrofit.create(StudentAuthApi::class.java)
    }
}