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
fun ForgotPasswordRequestScreen(
    viewModel: ForgotPasswordViewModel,
    onOtpSent: (String) -> Unit
) {
    val mobileNumber by viewModel.mobileNumber.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state) {
        if (state is ForgotPasswordViewModel.ForgotPasswordState.OtpSent) {
            onOtpSent((state as ForgotPasswordViewModel.ForgotPasswordState.OtpSent).mobileNumber)
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
            text = "Forgot Password",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Enter your mobile number to receive an OTP",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = mobileNumber,
            onValueChange = viewModel::onMobileNumberChange,
            label = { Text("Mobile Number") },
            placeholder = { Text("+919876543210") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            supportingText = {
                Text("Include country code (e.g. +91)")
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                viewModel.requestOtp()
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
                Text("Send OTP", style = MaterialTheme.typography.titleMedium)
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
