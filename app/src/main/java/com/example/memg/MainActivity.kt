package com.example.memg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.memg.data.AuthRepository
import com.example.memg.data.MembersRepository
import com.example.memg.data.NoticeRepository
import com.example.memg.ui.navigation.NavRoute
import com.example.memg.ui.screens.AddMemberScreen
import com.example.memg.ui.screens.ChangePasswordScreen
import com.example.memg.ui.screens.DashboardScreen
import com.example.memg.ui.screens.DeleteMemberScreen
import com.example.memg.ui.screens.EditMemberScreen
import com.example.memg.ui.screens.ForgotPasswordOtpScreen
import com.example.memg.ui.screens.ForgotPasswordRequestScreen
import com.example.memg.ui.screens.ForgotPasswordResetScreen
import com.example.memg.ui.screens.IssueNoticeScreen
import com.example.memg.ui.screens.LoginScreen
import com.example.memg.ui.screens.MembersScreen
import com.example.memg.ui.screens.SplashScreen
import com.example.memg.ui.theme.MemgTheme
import com.example.memg.ui.viewmodels.AddMemberViewModel
import com.example.memg.ui.viewmodels.ChangePasswordViewModel
import com.example.memg.ui.viewmodels.DashboardViewModel
import com.example.memg.ui.viewmodels.DeleteMemberViewModel
import com.example.memg.ui.viewmodels.LoginViewModel
import com.example.memg.ui.viewmodels.MainViewModel
import com.example.memg.ui.viewmodels.MembersViewModel
import com.example.memg.ui.viewmodels.MembershipStatusViewModel
import com.example.memg.ui.viewmodels.NoticeViewModel
import com.example.memg.ui.screens.MembershipStatusScreen
import com.example.memg.ui.viewmodels.EditMemberViewModel
import com.example.memg.ui.viewmodels.ForgotPasswordViewModel
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed class PaymentResult {
    data class Success(val paymentId: String?) : PaymentResult()
    data class Error(val code: Int, val description: String?) : PaymentResult()
}

class MainActivity : ComponentActivity(), PaymentResultListener {

    private val _paymentResult = MutableSharedFlow<PaymentResult>(extraBufferCapacity = 1)
    val paymentResult = _paymentResult.asSharedFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Checkout.preload(applicationContext)

        val authRepository = AuthRepository()
        val membersRepository = MembersRepository()
        val noticeRepository = NoticeRepository()

        setContent {
            MemgTheme {
                App(authRepository, membersRepository, noticeRepository, paymentResult)
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        _paymentResult.tryEmit(PaymentResult.Success(razorpayPaymentId))
    }

    override fun onPaymentError(code: Int, response: String?) {
        _paymentResult.tryEmit(PaymentResult.Error(code, response))
    }
}

@Composable
fun App(
    authRepository: AuthRepository,
    membersRepository: MembersRepository,
    noticeRepository: NoticeRepository,
    paymentResult: SharedFlow<PaymentResult>,
    mainViewModel: MainViewModel = viewModel()
) {
    val backStack by mainViewModel.backStack.collectAsState()
    val scope = rememberCoroutineScope()

    AppContent(
        backStack = backStack,
        onBack = { mainViewModel.pop() }
    ) { key ->
        when (key) {
            NavRoute.Splash -> SplashScreen(
                authRepository = authRepository,
                onFinished = { route ->
                    mainViewModel.replaceAll(route)
                }
            )

            NavRoute.Login -> {
                val loginViewModel = remember { LoginViewModel(authRepository) }
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = { mobile, forceChange, role ->
                        if (forceChange) {
                            mainViewModel.navigateTo(NavRoute.ChangePassword(mobile, role))
                        } else {
                            mainViewModel.replaceAll(NavRoute.Dashboard(role))
                        }
                    },
                    onForgotPassword = {
                        mainViewModel.navigateTo(NavRoute.ForgotPasswordRequest)
                    }
                )
            }

            is NavRoute.ChangePassword -> {
                val role = key.role
                val mobile = key.mobileNumber
                val changePasswordViewModel = remember { ChangePasswordViewModel(authRepository) }
                ChangePasswordScreen(
                    viewModel = changePasswordViewModel,
                    mobileNumber = mobile,
                    onSuccess = {
                        mainViewModel.replaceAll(NavRoute.Dashboard(role))
                    },
                    onLogout = {
                        mainViewModel.replaceAll(NavRoute.Login)
                    }
                )
            }

            NavRoute.ForgotPasswordRequest -> {
                val forgotPasswordViewModel = remember { ForgotPasswordViewModel(authRepository) }
                ForgotPasswordRequestScreen(
                    viewModel = forgotPasswordViewModel,
                    onOtpSent = { mobile ->
                        mainViewModel.navigateTo(NavRoute.ForgotPasswordOtp(mobile))
                    }
                )
            }

            is NavRoute.ForgotPasswordOtp -> {
                val mobile = key.mobileNumber
                val forgotPasswordViewModel = remember { ForgotPasswordViewModel(authRepository) }
                ForgotPasswordOtpScreen(
                    mobileNumber = mobile,
                    viewModel = forgotPasswordViewModel,
                    onOtpVerified = { mob, otp ->
                        mainViewModel.navigateTo(NavRoute.ForgotPasswordReset(mob, otp))
                    }
                )
            }

            is NavRoute.ForgotPasswordReset -> {
                val mobile = key.mobileNumber
                val otp = key.otp
                val forgotPasswordViewModel = remember { ForgotPasswordViewModel(authRepository) }
                ForgotPasswordResetScreen(
                    mobileNumber = mobile,
                    otp = otp,
                    viewModel = forgotPasswordViewModel,
                    onSuccess = {
                        mainViewModel.replaceAll(NavRoute.Login)
                    }
                )
            }

            is NavRoute.Dashboard -> {
                val dashboardViewModel = remember {
                    DashboardViewModel(membersRepository, noticeRepository, authRepository)
                }
                DashboardScreen(
                    role = key.role,
                    onLogout = {
                        scope.launch {
                            authRepository.logout()
                            mainViewModel.replaceAll(NavRoute.Login)
                        }
                    },
                    onAddMember = {
                        mainViewModel.navigateTo(NavRoute.AddMember)
                    },
                    onViewMembers = {
                        mainViewModel.navigateTo(NavRoute.ViewMembers)
                    },
                    onEditMember = {
                        mainViewModel.navigateTo(NavRoute.EditMember)
                    },
                    onDeleteMember = {
                        mainViewModel.navigateTo(NavRoute.DeleteMember)
                    },
                    onMembershipStatus = {
                        mainViewModel.navigateTo(NavRoute.MembershipStatus)
                    },
                    onIssueNotice = {
                        mainViewModel.navigateTo(NavRoute.IssueNotice)
                    },
                    viewModel = dashboardViewModel
                )
            }

            NavRoute.IssueNotice -> {
                val noticeViewModel = remember { NoticeViewModel(noticeRepository) }
                IssueNoticeScreen(
                    viewModel = noticeViewModel,
                    onBack = { mainViewModel.pop() }
                )
            }

            NavRoute.AddMember -> {
                val addMemberViewModel = remember { AddMemberViewModel(authRepository) }
                AddMemberScreen(
                    viewModel = addMemberViewModel,
                    onBack = { mainViewModel.pop() }
                )
            }

            NavRoute.EditMember -> {
                val editMemberViewModel = viewModel<EditMemberViewModel>()
                EditMemberScreen(
                    viewModel = editMemberViewModel,
                    onBack = { mainViewModel.pop() }
                )
            }

            NavRoute.DeleteMember -> {
                val deleteMemberViewModel = viewModel<DeleteMemberViewModel>()
                DeleteMemberScreen(
                    viewModel = deleteMemberViewModel,
                    onBack = { mainViewModel.pop() }
                )
            }

            NavRoute.ViewMembers -> {
                val membersViewModel = viewModel<MembersViewModel>()
                MembersScreen(
                    viewModel = membersViewModel,
                    onBack = { mainViewModel.pop() }
                )
            }

            NavRoute.MembershipStatus -> {
                val membershipViewModel = remember {
                    MembershipStatusViewModel(authRepository, membersRepository)
                }
                MembershipStatusScreen(
                    viewModel = membershipViewModel,
                    paymentResult = paymentResult,
                    onBack = { mainViewModel.pop() }
                )
            }
        }
    }
}

@Composable
fun AppContent(
    backStack: List<NavRoute>,
    onBack: () -> Unit,
    content: @Composable (NavRoute) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = onBack,
            modifier = Modifier.padding(innerPadding)
        ) { key ->
            NavEntry(key) {
                content(key)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() = MemgTheme {
    AppContent(
        backStack = listOf(NavRoute.Splash),
        onBack = {}
    ) { key ->
        when (key) {
            NavRoute.Splash -> Box(modifier = Modifier.fillMaxSize()) //Placeholder for splash in preview
            else -> {}
        }
    }
}
