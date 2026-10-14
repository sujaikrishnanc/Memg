package com.example.memg.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memg.data.AuthRepository
import com.example.memg.data.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChangePasswordViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _state = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val state: StateFlow<ChangePasswordState> = _state.asStateFlow()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun onNewPasswordChange(newValue: String) {
        _newPassword.value = newValue
    }

    fun onConfirmPasswordChange(newValue: String) {
        _confirmPassword.value = newValue
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun changePassword() {
        if (_newPassword.value.length < 6) {
            _state.value = ChangePasswordState.Error("Password must be at least 6 characters long")
            return
        }
        
        if (_newPassword.value != _confirmPassword.value) {
            _state.value = ChangePasswordState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _state.value = ChangePasswordState.Loading
            val success = repository.changePassword(_newPassword.value)
            if (success) {
                _state.value = ChangePasswordState.Success
                try {
                    val user = auth.currentUser
                    val mobile = user?.email?.substringBefore("@").toString()
                    Log.d("MainActivity", mobile)
                    db.collection("admins").document(mobile).update(
                        mapOf("isLoggingFirstTime" to false)
                    ).await() //Perform this instruction after successful initial password change

                    db.collection("admins").document(mobile).update(
                        mapOf("defaultPassword" to FieldValue.delete())
                    ).await() //Perform this instruction after successful initial password change

                    db.collection("users").document(mobile).set(
                        mapOf(
                            "role" to UserRole.ADMIN.name,
                            "isPasswordChanged" to true
                        )
                    ).await() //Perform this instruction after successful initial password change
                }
               catch (e: Exception) {
                   _state.value = ChangePasswordState.Error("Unable to fetch username. Please connect to network and try again.")
                   return@launch
               }
            } else {
                _state.value = ChangePasswordState.Error("Failed to change password")
            }
        }
    }

    sealed interface ChangePasswordState {
        data object Idle : ChangePasswordState
        data object Loading : ChangePasswordState
        data object Success : ChangePasswordState
        data class Error(val message: String) : ChangePasswordState
    }
}
