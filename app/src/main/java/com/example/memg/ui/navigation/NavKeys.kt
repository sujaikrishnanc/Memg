package com.example.memg.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute : NavKey {
    @Serializable
    data object Splash : NavRoute

    @Serializable
    data object Login : NavRoute

    @Serializable
    data class ChangePassword(val mobileNumber: String, val role: com.example.memg.data.UserRole) : NavRoute

    @Serializable
    data class Dashboard(val role: com.example.memg.data.UserRole) : NavRoute

    @Serializable
    data object AddMember : NavRoute

    @Serializable
    data object ViewMembers : NavRoute

    @Serializable
    data object EditMember : NavRoute

    @Serializable
    data object DeleteMember : NavRoute

    @Serializable
    data object IssueNotice : NavRoute

    @Serializable
    data object MembershipStatus : NavRoute

    @Serializable
    data object ForgotPasswordRequest : NavRoute

    @Serializable
    data class ForgotPasswordOtp(val mobileNumber: String) : NavRoute

    @Serializable
    data class ForgotPasswordReset(val mobileNumber: String, val otp: String) : NavRoute
}
