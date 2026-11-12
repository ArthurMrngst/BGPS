package com.example.bgps.data.repository

import com.example.bgps.auth.AuthTokenStore
import com.example.bgps.data.network.AuthApi
import com.example.bgps.data.network.LoginRequest
import com.example.bgps.data.network.RegisterRequest
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: AuthTokenStore
) {
    val isLoggedIn: StateFlow<String?> get() = tokenStore.email

    suspend fun register(email: String, password: String) {
        val response = api.register(RegisterRequest(email, password))
        tokenStore.save(response.accessToken, email)
    }

    suspend fun login(email: String, password: String) {
        val response = api.login(LoginRequest(email, password))
        tokenStore.save(response.accessToken, email)
    }

    fun logout() {
        tokenStore.clear()
    }
}
