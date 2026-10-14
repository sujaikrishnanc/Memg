package com.example.memg.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memg.data.Member
import com.example.memg.data.MembersRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DeleteMemberViewModel(
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

    private val _uiState = MutableStateFlow<DeleteMemberUiState>(DeleteMemberUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectMember(member: Member) {
        _selectedMember.value = member
    }

    fun clearSelection() {
        _selectedMember.value = null
    }

    fun deleteMember() {
        val member = _selectedMember.value ?: return
        viewModelScope.launch {
            _uiState.value = DeleteMemberUiState.Loading
            val result = repository.deleteMember(member.membershipId)
            if (result.isSuccess) {
                _uiState.value = DeleteMemberUiState.Success
                _selectedMember.value = null
                _searchQuery.value = ""
            } else {
                _uiState.value = DeleteMemberUiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete member")
            }
        }
    }

    fun resetState() {
        _uiState.value = DeleteMemberUiState.Idle
    }
}

sealed class DeleteMemberUiState {
    data object Idle : DeleteMemberUiState()
    data object Loading : DeleteMemberUiState()
    data object Success : DeleteMemberUiState()
    data class Error(val message: String) : DeleteMemberUiState()
}
