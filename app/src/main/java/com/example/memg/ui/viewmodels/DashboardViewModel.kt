package com.example.memg.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memg.data.AuthRepository
import com.example.memg.data.Member
import com.example.memg.data.MembersRepository
import com.example.memg.data.Notice
import com.example.memg.data.NoticeRepository
import com.example.memg.utils.CsvParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class DashboardViewModel(
    private val repository: MembersRepository = MembersRepository(),
    private val noticeRepository: NoticeRepository = NoticeRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uploadStatus = MutableStateFlow<UploadStatus>(UploadStatus.Idle)
    val uploadStatus: StateFlow<UploadStatus> = _uploadStatus.asStateFlow()

    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _isLoadingNotices = MutableStateFlow(false)
    val isLoadingNotices: StateFlow<Boolean> = _isLoadingNotices.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentMember: StateFlow<Member?> = flow {
        emit(authRepository.getCurrentUserStatus())
    }.flatMapLatest { userStatus ->
        if (userStatus != null) {
            repository.getMemberByMobile(userStatus.mobileNumber)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadNotices()
    }

    private fun loadNotices() {
        viewModelScope.launch {
            _isLoadingNotices.value = true
            noticeRepository.getNotices().collect {
                _notices.value = it
                // Simulate slow loading for shimmer effect as requested
                kotlinx.coroutines.delay(2000.milliseconds)
                _isLoadingNotices.value = false
            }
        }
    }

    fun uploadCsvFile(context: Context, uri: Uri?) {
        if (uri == null) {
            _uploadStatus.value = UploadStatus.Error("Please select a CSV file")
            return
        }

        _uploadStatus.value = UploadStatus.Loading

        viewModelScope.launch {
            try {
                val members = context.contentResolver.openInputStream(uri)?.use {
                    CsvParser.parseMembersFromSingleCsv(it)
                } ?: throw Exception("Failed to open CSV file")

                val result = repository.saveMembers(members)
                if (result.isSuccess) {
                    _uploadStatus.value = UploadStatus.Success
                } else {
                    _uploadStatus.value = UploadStatus.Error(result.exceptionOrNull()?.message ?: "Failed to save members")
                }
            } catch (e: Exception) {
                _uploadStatus.value = UploadStatus.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetStatus() {
        _uploadStatus.value = UploadStatus.Idle
    }
}

sealed class UploadStatus {
    object Idle : UploadStatus()
    object Loading : UploadStatus()
    object Success : UploadStatus()
    data class Error(val message: String) : UploadStatus()
}
