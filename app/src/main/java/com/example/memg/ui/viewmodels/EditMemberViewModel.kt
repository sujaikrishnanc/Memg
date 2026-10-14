package com.example.memg.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memg.data.Member
import com.example.memg.data.MembersRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EditMemberViewModel(
    private val repository: MembersRepository = MembersRepository()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedMember = MutableStateFlow<Member?>(null)
    val selectedMember = _selectedMember.asStateFlow()

    val filteredMembers = combine(
        repository.getMembers(),
        _searchQuery
    ) { members, query ->
        if (query.isBlank()) emptyList()
        else members.filter {
            it.name.contains(query, ignoreCase = true) || 
            it.membershipId.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Edit Form State
    private val _memberName = MutableStateFlow("")
    val memberName = _memberName.asStateFlow()

    private val _mobileNumber = MutableStateFlow("")
    val mobileNumber = _mobileNumber.asStateFlow()

    private val _alternateContact = MutableStateFlow("")
    val alternateContact = _alternateContact.asStateFlow()

    private val _address = MutableStateFlow("")
    val address = _address.asStateFlow()

    private val _startDate = MutableStateFlow("")
    val startDate = _startDate.asStateFlow()

    private val _familyMembers = MutableStateFlow<List<FamilyMemberInput>>(emptyList())
    val familyMembers = _familyMembers.asStateFlow()

    private val _uiState = MutableStateFlow<EditMemberUiState>(EditMemberUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectMember(member: Member) {
        _selectedMember.value = member
        _memberName.value = member.name
        _mobileNumber.value = member.mobileNumber
        _alternateContact.value = member.alternateContact
        _address.value = member.address
        _startDate.value = member.membershipStartDate
        _familyMembers.value = member.familyMembers.map { 
            val parts = it.split(" (")
            FamilyMemberInput(parts[0], parts.getOrNull(1)?.removeSuffix(")") ?: "")
        }
    }

    fun clearSelection() {
        _selectedMember.value = null
    }

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

    fun updateMember() {
        val currentMember = _selectedMember.value ?: return
        if (_memberName.value.isEmpty() || _mobileNumber.value.isEmpty() || _startDate.value.isEmpty()) {
            _uiState.value = EditMemberUiState.Error("Please fill all mandatory fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = EditMemberUiState.Loading
            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val start = LocalDate.parse(_startDate.value, formatter)
                val end = start.plusYears(1)

                val updatedMember = currentMember.copy(
                    name = _memberName.value,
                    mobileNumber = _mobileNumber.value,
                    alternateContact = _alternateContact.value,
                    address = _address.value,
                    membershipStartDate = _startDate.value,
                    membershipEndDate = end.format(formatter),
                    familyMembers = _familyMembers.value.map { "${it.name} (${it.relation})" }
                )

                val result = repository.updateMember(updatedMember)
                if (result.isSuccess) {
                    _uiState.value = EditMemberUiState.Success
                    _selectedMember.value = null
                    _searchQuery.value = ""
                } else {
                    _uiState.value = EditMemberUiState.Error(result.exceptionOrNull()?.message ?: "Failed to update member")
                }
            } catch (e: Exception) {
                _uiState.value = EditMemberUiState.Error("Invalid date format. Use dd/MM/yyyy")
            }
        }
    }

    fun resetState() {
        _uiState.value = EditMemberUiState.Idle
    }
}

sealed class EditMemberUiState {
    data object Idle : EditMemberUiState()
    data object Loading : EditMemberUiState()
    data object Success : EditMemberUiState()
    data class Error(val message: String) : EditMemberUiState()
}
