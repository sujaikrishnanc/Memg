package com.example.memg.ui.screens

import android.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.memg.ui.theme.MemgTheme
import com.example.memg.ui.viewmodels.ChangePasswordViewModel

@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel,
    mobileNumber: String,
    onSuccess: () -> Unit,
    onLogout: () -> Unit
) {
    val newPassword by viewModel.newPassword.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.confirmPassword.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler {
        viewModel.logout()
        onLogout()
    }

    ChangePasswordContent(
        newPassword = newPassword,
        confirmPassword = confirmPassword,
        state = state,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onChangePasswordClick = viewModel::changePassword,
        onSuccess = onSuccess
    )
}

@Composable
fun ChangePasswordContent(
    newPassword: String,
    confirmPassword: String,
    state: ChangePasswordViewModel.ChangePasswordState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onChangePasswordClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state) {
        if (state is ChangePasswordViewModel.ChangePasswordState.Success) {
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Update Password",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            )
        Text(
            text = "For security, you must change your default password on first login.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = { Text("New Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm New Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                onChangePasswordClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = state !is ChangePasswordViewModel.ChangePasswordState.Loading,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color(Color.BLACK), // Dark Yellow
                contentColor = androidx.compose.ui.graphics.Color(Color.WHITE)
            )
        ) {
            if (state is ChangePasswordViewModel.ChangePasswordState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Update Password", style = MaterialTheme.typography.titleMedium)
            }
        }

        if (state is ChangePasswordViewModel.ChangePasswordState.Error) {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordPreview() {
    MemgTheme {
        ChangePasswordContent(
            newPassword = "",
            confirmPassword = "",
            state = ChangePasswordViewModel.ChangePasswordState.Idle,
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onChangePasswordClick = {},
            onSuccess = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun ChangePasswordErrorPreview() {
    MemgTheme {
        ChangePasswordContent(
            newPassword = "password",
            confirmPassword = "wrong",
            state = ChangePasswordViewModel.ChangePasswordState.Error("Passwords do not match"),
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onChangePasswordClick = {},
            onSuccess = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun ChangePasswordLoadingPreview() {
    MemgTheme {
        ChangePasswordContent(
            newPassword = "password",
            confirmPassword = "password",
            state = ChangePasswordViewModel.ChangePasswordState.Loading,
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onChangePasswordClick = {},
            onSuccess = {}
        )
    }
}
