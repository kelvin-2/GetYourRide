// ─────────────────────────────────────────────────────────────────────────────
// StudentAuthApi.kt
// Package: com.example.getyourride.data.remote.api
//
// PURPOSE — Retrofit interface matching StudentAuthController.java exactly.
//
//   Java:  @RequestMapping("/api/auth/student")
//          POST /register
//          POST /login
//
//   This interface is just the contract — actual network calls happen
//   through the Retrofit instance built in NetworkModule.kt (di/ package).
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.remote.api

import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.StudentLoginRequest
import com.example.getyourride.data.remote.dto.StudentRegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface StudentAuthApi {

    // Matches: @PostMapping("/register") in StudentAuthController.java
    @POST("api/auth/student/register")
    suspend fun register(@Body request: StudentRegisterRequest): Response<AuthResponse>

    // Matches: @PostMapping("/login") in StudentAuthController.java
    @POST("api/auth/student/login")
    suspend fun login(@Body request: StudentLoginRequest): Response<AuthResponse>
}