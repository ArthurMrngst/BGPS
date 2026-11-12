package com.example.bgps.data.network

import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val accessToken: String)

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse
}
