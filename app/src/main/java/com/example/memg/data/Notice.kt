package com.example.memg.data

import kotlinx.serialization.Serializable

@Serializable
data class Notice(
    val id: String = "",
    val title: String = "",
    val pdfUrl: String = "",
    val fileName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
