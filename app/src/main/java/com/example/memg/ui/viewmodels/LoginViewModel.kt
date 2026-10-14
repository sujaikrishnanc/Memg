package com.example.memg.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memg.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _mobileNumber = MutableStateFlow("")
    val mobileNumber: StateFlow<String> = _mobileNumber.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    val isPasswordChanged: StateFlow<Boolean> = repository.isPasswordChanged
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onMobileNumberChange(newValue: String) {
        _mobileNumber.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun login() {
        //Username and password validation performed here
        if (!_mobileNumber.value.startsWith("+")) {
            _loginState.value = LoginState.Error("Include country code (e.g. +91)")
            return
        }
        if (_mobileNumber.value.length<13) {
            _loginState.value = LoginState.Error("Mobile number should be have at least 13 digits including country code.")
            return
        }
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            when (val result = repository.login(_mobileNumber.value, _password.value)) {
                is AuthRepository.LoginResult.Success -> {
                    _loginState.value = LoginState.Success(result.forcePasswordChange, result.role)
                }
                AuthRepository.LoginResult.Failure -> {
                    _loginState.value = LoginState.Error("Invalid credentials")
                }
            }
        }
    }

    sealed interface LoginState {
        data object Idle : LoginState
        data object Loading : LoginState
        data class Success(val forcePasswordChange: Boolean, val role: com.example.memg.data.UserRole) : LoginState
        data class Error(val message: String) : LoginState
    }
}
