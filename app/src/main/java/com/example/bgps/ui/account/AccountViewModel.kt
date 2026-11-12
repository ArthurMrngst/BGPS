package com.example.bgps.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.repository.AuthRepository
import com.example.bgps.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AccountUiState {
    data object Idle : AccountUiState
    data object Loading : AccountUiState
    data class Error(val message: String) : AccountUiState
    data object SyncSuccess : AccountUiState
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    // null = chưa đăng nhập (chế độ khách, vẫn dùng app bình thường với dữ
    // liệu cục bộ) — không có gì bắt buộc phải đăng nhập.
    val loggedInEmail: StateFlow<String?> get() = authRepository.isLoggedIn

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Idle)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String) = runCatchingAsync {
        authRepository.register(email, password)
    }

    fun login(email: String, password: String) = runCatchingAsync {
        authRepository.login(email, password)
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AccountUiState.Idle
    }

    fun syncNow() = runCatchingAsync(onSuccessState = AccountUiState.SyncSuccess) {
        syncRepository.sync()
    }

    fun clearError() {
        _uiState.value = AccountUiState.Idle
    }

    private fun runCatchingAsync(onSuccessState: AccountUiState = AccountUiState.Idle, block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AccountUiState.Loading
            _uiState.value = try {
                block()
                onSuccessState
            } catch (e: Exception) {
                AccountUiState.Error(e.message ?: "Có lỗi xảy ra, thử lại sau")
            }
        }
    }
}
