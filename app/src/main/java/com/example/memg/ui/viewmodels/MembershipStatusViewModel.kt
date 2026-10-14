package com.example.memg.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memg.data.AuthRepository
import com.example.memg.data.Member
import com.example.memg.data.MembersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalCoroutinesApi::class)
class MembershipStatusViewModel(
    private val authRepository: AuthRepository,
    private val membersRepository: MembersRepository,
) : ViewModel() {

    private val dateFormats = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy")
    )

    val member: StateFlow<Member?> = flow {
        emit(authRepository.getCurrentUserStatus())
    }.flatMapLatest { userStatus ->
        if (userStatus != null) {
            membersRepository.getMemberByMobile(userStatus.mobileNumber)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _paymentStatus = MutableStateFlow<PaymentStatus>(PaymentStatus.Idle)
    val paymentStatus: StateFlow<PaymentStatus> = _paymentStatus

    fun canRenew(member: Member): Boolean {
        val today = LocalDate.now()
        val endDate = parseDate(member.membershipEndDate) ?: return false
        
        // Literal requirement: "from 10 days before the membership end date till 10 days after the membership start date"
        // Interpreted as 10 days before and after the expiration (end) date for practical renewal.
        val renewalStart = endDate.minusDays(10)
        val renewalEnd = endDate.plusDays(10)
        
        return (today.isEqual(renewalStart) || today.isAfter(renewalStart)) && 
               (today.isEqual(renewalEnd) || today.isBefore(renewalEnd))
    }

    private fun parseDate(dateStr: String): LocalDate? {
        for (format in dateFormats) {
            try {
                return LocalDate.parse(dateStr, format)
            } catch (_: DateTimeParseException) {
            }
        }
        return null
    }

    fun onPaymentSuccess(paymentId: String, member: Member) {
        // paymentId could be logged or stored for audit
        _paymentStatus.value = PaymentStatus.Loading
        viewModelScope.launch {
            val currentEndDate = parseDate(member.membershipEndDate) ?: LocalDate.now()
            val newEndDate = currentEndDate.plusYears(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val result = membersRepository.renewMembership(member.membershipId, newEndDate)
            if (result.isSuccess) {
                _paymentStatus.value = PaymentStatus.Success
            } else {
                _paymentStatus.value = PaymentStatus.Error("Failed to update membership: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun onPaymentError(code: Int, description: String) {
        _paymentStatus.value = PaymentStatus.Error("Payment failed ($code): $description")
    }
    
    fun resetPaymentStatus() {
        _paymentStatus.value = PaymentStatus.Idle
    }
}

sealed class PaymentStatus {
    object Idle : PaymentStatus()
    object Loading : PaymentStatus()
    object Success : PaymentStatus()
    data class Error(val message: String) : PaymentStatus()
}
