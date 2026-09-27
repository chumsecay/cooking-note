package com.cookingnote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookingnote.app.data.prefs.UserSessionStore
import com.cookingnote.app.data.repository.CookbookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val email: String = "",
    val fullName: String = "",
    val role: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val repository: CookbookRepository,
    private val sessionStore: UserSessionStore,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _successMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AuthUiState> = combine(
        sessionStore.session,
        _isLoading,
        _errorMessage,
        _successMessage
    ) { session, isLoading, error, success ->
        AuthUiState(
            isLoading = isLoading,
            isLoggedIn = session.isLoggedIn,
            email = session.email.orEmpty(),
            fullName = session.fullName.orEmpty(),
            role = session.role,
            errorMessage = error,
            successMessage = success
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthUiState()
    )

    fun login(email: String, pass: String, onSuccess: (() -> Unit)? = null) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        if (trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            _errorMessage.value = "Vui lòng nhập đầy đủ email và mật khẩu."
            return
        }
        viewModelScope.launch(ioDispatcher) {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            repository.login(trimmedEmail, trimmedPass)
                .onSuccess { auth ->
                    sessionStore.saveSession(
                        token = auth.token,
                        refreshToken = auth.refreshToken,
                        userId = auth.userId,
                        email = auth.email ?: trimmedEmail,
                        fullName = auth.fullName ?: "Người dùng",
                        role = auth.role
                    )
                    _successMessage.value = "Đăng nhập thành công!"
                    onSuccess?.invoke()
                }
                .onFailure { err ->
                    _errorMessage.value = err.message ?: "Đăng nhập thất bại"
                }
            _isLoading.value = false
        }
    }

    fun register(fullName: String, email: String, pass: String, onSuccess: (() -> Unit)? = null) {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        if (trimmedName.isBlank() || trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            _errorMessage.value = "Vui lòng điền đầy đủ các thông tin."
            return
        }
        if (trimmedPass.length < 6) {
            _errorMessage.value = "Mật khẩu phải có ít nhất 6 ký tự."
            return
        }
        viewModelScope.launch(ioDispatcher) {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            repository.register(trimmedEmail, trimmedPass, trimmedName)
                .onSuccess { auth ->
                    sessionStore.saveSession(
                        token = auth.token,
                        refreshToken = auth.refreshToken,
                        userId = auth.userId,
                        email = auth.email ?: trimmedEmail,
                        fullName = auth.fullName ?: trimmedName,
                        role = auth.role
                    )
                    _successMessage.value = "Tạo tài khoản thành công!"
                    onSuccess?.invoke()
                }
                .onFailure { err ->
                    _errorMessage.value = err.message ?: "Đăng ký thất bại"
                }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch(ioDispatcher) {
            sessionStore.clearSession()
            _successMessage.value = "Đã đăng xuất tài khoản."
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
}
