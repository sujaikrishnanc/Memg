package com.example.memg.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memg.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _mobileNumber = MutableStateFlow("")
    val mobileNumber: StateFlow<String> = _mobileNumber.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _state = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onMobileNumberChange(newValue: String) {
        _mobileNumber.value = newValue
    }

    fun onOtpChange(newValue: String) {
        _otp.value = newValue
    }

    fun onNewPasswordChange(newValue: String) {
        _newPassword.value = newValue
    }

    fun onConfirmPasswordChange(newValue: String) {
        _confirmPassword.value = newValue
    }

    fun requestOtp() {
        if (_mobileNumber.value.isEmpty()) {
            _state.value = ForgotPasswordState.Error("Mobile number is required")
            return
        }
        if (!_mobileNumber.value.startsWith("+")) {
            _state.value = ForgotPasswordState.Error("Include country code (e.g. +91)")
            return
        }
        viewModelScope.launch {
            _state.value = ForgotPasswordState.Loading
            val success = repository.requestOtp(_mobileNumber.value)
            if (success) {
                _state.value = ForgotPasswordState.OtpSent(_mobileNumber.value)
            } else {
                _state.value = ForgotPasswordState.Error("Failed to send OTP. Ensure you have already changed your password once.")
            }
        }
    }

    fun verifyOtp() {
        if (_otp.value.isEmpty()) {
            _state.value = ForgotPasswordState.Error("OTP is required")
            return
        }
        viewModelScope.launch {
            _state.value = ForgotPasswordState.Loading
            val success = repository.verifyOtp(_mobileNumber.value, _otp.value)
            if (success) {
                _state.value = ForgotPasswordState.OtpVerified(_mobileNumber.value, _otp.value)
            } else {
                _state.value = ForgotPasswordState.Error("Invalid OTP")
            }
        }
    }

    fun resetPassword() {
        if (_newPassword.value.length < 6) {
            _state.value = ForgotPasswordState.Error("Password must be at least 6 characters long")
            return
        }
        
        if (_newPassword.value != _confirmPassword.value) {
            _state.value = ForgotPasswordState.Error("Passwords do not match")
            return
        }
        viewModelScope.launch {
            _state.value = ForgotPasswordState.Loading
            val success = repository.resetPassword(_mobileNumber.value, _otp.value, _newPassword.value)
            if (success) {
                _state.value = ForgotPasswordState.Success
            } else {
                _state.value = ForgotPasswordState.Error("Failed to reset password")
            }
        }
    }

    fun resetState() {
        _state.value = ForgotPasswordState.Idle
    }

    sealed interface ForgotPasswordState {
        data object Idle : ForgotPasswordState
        data object Loading : ForgotPasswordState
        data class OtpSent(val mobileNumber: String) : ForgotPasswordState
        data class OtpVerified(val mobileNumber: String, val otp: String) : ForgotPasswordState
        data object Success : ForgotPasswordState
        data class Error(val message: String) : ForgotPasswordState
    }
}
