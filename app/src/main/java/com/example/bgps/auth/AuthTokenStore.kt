package com.example.bgps.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Lưu JWT sau khi đăng nhập/đăng ký — đăng nhập hoàn toàn TUỲ CHỌN, app vẫn
// dùng được đầy đủ với dữ liệu cục bộ khi chưa đăng nhập (xem AccountScreen).
// Chỉ cần token này khi gọi API đồng bộ lên backend.
@Singleton
class AuthTokenStore @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _token = MutableStateFlow(prefs.getString(KEY_TOKEN, null))
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _email = MutableStateFlow(prefs.getString(KEY_EMAIL, null))
    val email: StateFlow<String?> = _email.asStateFlow()

    fun getTokenOrNull(): String? = _token.value

    fun save(token: String, email: String) {
        prefs.edit().putString(KEY_TOKEN, token).putString(KEY_EMAIL, email).apply()
        _token.value = token
        _email.value = email
    }

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_EMAIL).apply()
        _token.value = null
        _email.value = null
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_EMAIL = "email"
    }
}
