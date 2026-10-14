package com.example.memg.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memg.data.AuthRepository
import com.example.memg.data.Member
import com.example.memg.data.MembersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddMemberViewModel(
    private val authRepository: AuthRepository,
    private val membersRepository: MembersRepository = MembersRepository()
) : ViewModel() {

    private val _membershipId = MutableStateFlow("Generating...")
    val membershipId: StateFlow<String> = _membershipId

    init {
        fetchNextId()
    }

    private fun fetchNextId() {
        viewModelScope.launch {
            _membershipId.value = membersRepository.generateNextMembershipId()
        }
    }

    private val _memberName = MutableStateFlow("")
    val memberName: StateFlow<String> = _memberName

    private val _mobileNumber = MutableStateFlow("")
    val mobileNumber: StateFlow<String> = _mobileNumber

    private val _alternateContact = MutableStateFlow("")
    val alternateContact: StateFlow<String> = _alternateContact

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address

    private val _startDate = MutableStateFlow("")
    val startDate: StateFlow<String> = _startDate

    private val _familyMembers = MutableStateFlow<List<FamilyMemberInput>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMemberInput>> = _familyMembers

    private val _uiState = MutableStateFlow<AddMemberUiState>(AddMemberUiState.Idle)
    val uiState: StateFlow<AddMemberUiState> = _uiState

    fun onMemberNameChange(value: String) { _memberName.value = value }
    fun onMobileNumberChange(value: String) { _mobileNumber.value = value }
    fun onAlternateContactChange(value: String) { _alternateContact.value = value }
    fun onAddressChange(value: String) { _address.value = value }
    fun onStartDateChange(value: String) { _startDate.value = value }

    fun addFamilyMember() {
        _familyMembers.value = _familyMembers.value + FamilyMemberInput()
    }

    fun onFamilyMemberChange(index: Int, name: String, relation: String) {
        val list = _familyMembers.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(name = name, relation = relation)
            _familyMembers.value = list
        }
    }

    fun removeFamilyMember(index: Int) {
        val list = _familyMembers.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _familyMembers.value = list
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        if (phone.isEmpty()) return true // Optional fields
        val e164Regex = Regex("^\\+[1-9]\\d{1,14}$")
        val tenDigitRegex = Regex("^\\d{10}$")
        return phone.matches(e164Regex) || phone.matches(tenDigitRegex)
    }

    fun reviewMember() {
        if (_memberName.value.isBlank()) {
            _uiState.value = AddMemberUiState.Error("Member name is required")
            return
        }
        if (_mobileNumber.value.isBlank()) {
            _uiState.value = AddMemberUiState.Error("Phone number is required")
            return
        }
        if (!isValidPhoneNumber(_mobileNumber.value)) {
            _uiState.value = AddMemberUiState.Error("Invalid phone number format")
            return
        }
        if (_alternateContact.value.isNotBlank() && !isValidPhoneNumber(_alternateContact.value)) {
            _uiState.value = AddMemberUiState.Error("Invalid alternate contact format")
            return
        }
        if (_address.value.isBlank()) {
            _uiState.value = AddMemberUiState.Error("Address is required")
            return
        }
        if (_startDate.value.isBlank()) {
            _uiState.value = AddMemberUiState.Error("Start date is required")
            return
        }
        if (_familyMembers.value.isEmpty()) {
            _uiState.value = AddMemberUiState.Error("At least one family member must be added")
            return
        }
        if (_familyMembers.value.any { it.name.isBlank() || it.relation.isBlank() }) {
            _uiState.value = AddMemberUiState.Error("All family member details must be filled")
            return
        }

        try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val start = LocalDate.parse(_startDate.value, formatter)
            if (start.isAfter(LocalDate.now())) {
                _uiState.value = AddMemberUiState.Error("Membership start date cannot be in the future")
                return
            }
            
            val end = start.plusYears(1)
            val member = Member(
                membershipId = _membershipId.value,
                name = _memberName.value,
                status = "Active",
                mobileNumber = _mobileNumber.value,
                alternateContact = _alternateContact.value,
                address = _address.value,
                membershipStartDate = _startDate.value,
                membershipEndDate = end.format(formatter),
                familyMembers = _familyMembers.value.map { "${it.name} (${it.relation})" }
            )
            _uiState.value = AddMemberUiState.Reviewing(member)
        } catch (e: Exception) {
            _uiState.value = AddMemberUiState.Error("Invalid date format. Use dd/MM/yyyy")
        }
    }

    fun addMember(member: Member) {
        viewModelScope.launch {
            _uiState.value = AddMemberUiState.Loading
            try {
                // Save member data
                val result = membersRepository.saveMembers(listOf(member))
                if (result.isSuccess) {
                    // Create auth account
                    authRepository.addMember(member.mobileNumber)
                    _uiState.value = AddMemberUiState.Success
                    clearFields()
                    fetchNextId() // Fetch next ID for the next entry
                } else {
                    _uiState.value = AddMemberUiState.Error(result.exceptionOrNull()?.message ?: "Failed to save member")
                }
            } catch (e: Exception) {
                _uiState.value = AddMemberUiState.Error("An unexpected error occurred")
            }
        }
    }

    private fun clearFields() {
        _memberName.value = ""
        _mobileNumber.value = ""
        _alternateContact.value = ""
        _address.value = ""
        _startDate.value = ""
        _familyMembers.value = emptyList()
    }

    fun resetState() {
        _uiState.value = AddMemberUiState.Idle
    }
}

data class FamilyMemberInput(val name: String = "", val relation: String = "")

sealed class AddMemberUiState {
    data object Idle : AddMemberUiState()
    data object Loading : AddMemberUiState()
    data class Reviewing(val member: Member) : AddMemberUiState()
    data object Success : AddMemberUiState()
    data class Error(val message: String) : AddMemberUiState()
}
