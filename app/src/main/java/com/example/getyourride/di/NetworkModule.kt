package com.example.getyourride.di

import com.example.getyourride.UserSession
import com.example.getyourride.data.remote.api.StudentAuthApi
import com.example.getyourride.data.remote.api.TripApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    // ⚠️ You had "http://localhost:8080/" — that will NOT work on the emulator.
    // localhost inside the emulator means the emulator itself, not your PC.
    // Your memory notes 10.0.2.2 as the correct base URL — switching back to that.

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ── Attaches "Authorization: Bearer <token>" to every outgoing request ───
    // Reads UserSession.token fresh on every call, so it always has the
    // latest token without you touching individual Api interfaces.
    private val authInterceptor = okhttp3.Interceptor { chain ->
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

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)      // attach token first
        .addInterceptor(loggingInterceptor)   // then log the final request
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val studentAuthApi: StudentAuthApi by lazy {
        retrofit.create(StudentAuthApi::class.java)
    }
    //trip api
    val tripApi: TripApi by lazy {
        retrofit.create(TripApi::class.java)
    }

    // Trips API — added once TripApi.kt exists (see below, pending DTO fields)
    // val tripApi: TripApi by lazy { retrofit.create(TripApi::class.java) }
}