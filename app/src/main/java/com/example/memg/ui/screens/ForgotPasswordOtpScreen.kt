package com.example.memg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.memg.ui.viewmodels.ForgotPasswordViewModel

@Composable
fun ForgotPasswordOtpScreen(
    mobileNumber: String,
    viewModel: ForgotPasswordViewModel,
    onOtpVerified: (String, String) -> Unit
) {
    val otp by viewModel.otp.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state) {
        if (state is ForgotPasswordViewModel.ForgotPasswordState.OtpVerified) {
            val verified = state as ForgotPasswordViewModel.ForgotPasswordState.OtpVerified
            onOtpVerified(verified.mobileNumber, verified.otp)
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
            text = "Verify OTP",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "OTP sent to $mobileNumber",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = otp,
            onValueChange = viewModel::onOtpChange,
            label = { Text("Enter OTP") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                viewModel.verifyOtp()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = state !is ForgotPasswordViewModel.ForgotPasswordState.Loading,
            shape = MaterialTheme.shapes.large
        ) {
            if (state is ForgotPasswordViewModel.ForgotPasswordState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Verify OTP", style = MaterialTheme.typography.titleMedium)
            }
        }

        if (state is ForgotPasswordViewModel.ForgotPasswordState.Error) {
            Text(
                text = (state as ForgotPasswordViewModel.ForgotPasswordState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
