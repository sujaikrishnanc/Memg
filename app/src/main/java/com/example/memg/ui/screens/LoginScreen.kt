package com.example.memg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import com.example.memg.ui.theme.MemgTheme
import com.example.memg.ui.viewmodels.LoginViewModel
import androidx.compose.foundation.Image
import com.example.memg.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (String, Boolean, com.example.memg.data.UserRole) -> Unit,
    onForgotPassword: () -> Unit
) {
    val mobileNumber by viewModel.mobileNumber.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val isPasswordChanged by viewModel.isPasswordChanged.collectAsStateWithLifecycle()

    LaunchedEffect(loginState) {
        if (loginState is LoginViewModel.LoginState.Success) {
            val successState = loginState as LoginViewModel.LoginState.Success
            onLoginSuccess(mobileNumber, successState.forcePasswordChange, successState.role)
        }
    }

    LoginScreenContent(
        mobileNumber = mobileNumber,
        password = password,
        loginState = loginState,
        isPasswordChanged = isPasswordChanged,
        onMobileNumberChange = viewModel::onMobileNumberChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onForgotPassword = onForgotPassword
    )
}

@Composable
fun LoginScreenContent(
    mobileNumber: String,
    password: String,
    loginState: LoginViewModel.LoginState,
    isPasswordChanged: Boolean,
    onMobileNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.memg),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 32.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = mobileNumber,
            onValueChange = onMobileNumberChange,
            label = { Text("Mobile Number") },
            placeholder = { Text("+919876543210") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            supportingText = {
                Text("Include country code (e.g. +91)")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        if (isPasswordChanged) {
            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?")
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                onLoginClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = loginState !is LoginViewModel.LoginState.Loading,
            shape = MaterialTheme.shapes.large
        ) {
            if (loginState is LoginViewModel.LoginState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login", style = MaterialTheme.typography.titleMedium)
            }
        }

        if (loginState is LoginViewModel.LoginState.Error) {
            Text(
                text = loginState.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MemgTheme {
        LoginScreenContent(
            mobileNumber = "+919876543210",
            password = "password",
            loginState = LoginViewModel.LoginState.Idle,
            isPasswordChanged = true,
            onMobileNumberChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onForgotPassword = {}
        )
    }
}
