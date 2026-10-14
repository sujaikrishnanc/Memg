package com.example.memg.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memg.data.AuthRepository
import com.example.memg.ui.navigation.NavRoute
import com.example.memg.ui.theme.MemgTheme
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.memg.R

@Composable
fun SplashScreen(
    authRepository: AuthRepository,
    onFinished: (NavRoute) -> Unit
) {
    LaunchedEffect(Unit) {
        val userStatus = authRepository.getCurrentUserStatus()
        
        val route = if (userStatus == null) {
            NavRoute.Login
        } else if (!userStatus.isPasswordChanged) {
            NavRoute.ChangePassword(userStatus.mobileNumber, userStatus.role)
        } else {
            NavRoute.Dashboard(userStatus.role)
        }

        onFinished(route)
    }

    SplashScreenContent()
}

@Composable
fun SplashScreenContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.memg),
            contentDescription = "Organization Logo",
            modifier = Modifier.size(200.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MemgTheme {
        SplashScreenContent()
    }
}