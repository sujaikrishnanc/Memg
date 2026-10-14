package com.example.memg.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memg.data.Notice
import com.example.memg.data.NoticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoticeViewModel(
    private val repository: NoticeRepository = NoticeRepository()
) : ViewModel() {

    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uploadStatus = MutableStateFlow<NoticeUploadStatus>(NoticeUploadStatus.Idle)
    val uploadStatus: StateFlow<NoticeUploadStatus> = _uploadStatus.asStateFlow()

    init {
        loadNotices()
    }

    private fun loadNotices() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getNotices().collect {
                _notices.value = it
                _isLoading.value = false
            }
        }
    }

    fun issueNotice(title: String, pdfUri: Uri, fileName: String) {
        _uploadStatus.value = NoticeUploadStatus.Loading
        viewModelScope.launch {
            val result = repository.uploadNotice(title, pdfUri, fileName)
            if (result.isSuccess) {
                _uploadStatus.value = NoticeUploadStatus.Success
            } else {
                _uploadStatus.value = NoticeUploadStatus.Error(result.exceptionOrNull()?.message ?: "Upload failed")
            }
        }
    }

    fun deleteNotice(notice: Notice) {
        viewModelScope.launch {
            repository.deleteNotice(notice.id, notice.pdfUrl)
        }
    }

    fun resetStatus() {
        _uploadStatus.value = NoticeUploadStatus.Idle
    }
}

sealed class NoticeUploadStatus {
    object Idle : NoticeUploadStatus()
    object Loading : NoticeUploadStatus()
    object Success : NoticeUploadStatus()
    data class Error(val message: String) : NoticeUploadStatus()
}
