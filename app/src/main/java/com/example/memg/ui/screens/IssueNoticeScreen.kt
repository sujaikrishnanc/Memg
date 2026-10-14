package com.example.memg.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.memg.data.Notice
import com.example.memg.ui.viewmodels.NoticeUploadStatus
import com.example.memg.ui.viewmodels.NoticeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueNoticeScreen(
    viewModel: NoticeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notices by viewModel.notices.collectAsState()
    val uploadStatus by viewModel.uploadStatus.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var pdfName by remember { mutableStateOf("") }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            selectedPdfUri = uri
            uri?.let {
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    pdfName = cursor.getString(nameIndex)
                }
            }
        }
    )

    LaunchedEffect(uploadStatus) {
        if (uploadStatus is NoticeUploadStatus.Success) {
            Toast.makeText(context, "Notice issued successfully", Toast.LENGTH_SHORT).show()
            title = ""
            selectedPdfUri = null
            pdfName = ""
            viewModel.resetStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue a Notice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notice Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedPdfUri == null) "Select PDF Notice" else "PDF Selected: $pdfName")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    selectedPdfUri?.let { viewModel.issueNotice(title, it, pdfName) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && selectedPdfUri != null && uploadStatus !is NoticeUploadStatus.Loading
            ) {
                if (uploadStatus is NoticeUploadStatus.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Issue Notice")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Recent Notices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(notices) { notice ->
                    NoticeItem(notice = notice, onDelete = { viewModel.deleteNotice(notice) })
                }
            }
        }
    }
}

@Composable
fun NoticeItem(notice: Notice, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notice.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(notice.fileName, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
