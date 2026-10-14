package com.example.memg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.memg.ui.viewmodels.DeleteMemberUiState
import com.example.memg.ui.viewmodels.DeleteMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteMemberScreen(
    viewModel: DeleteMemberViewModel,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredMembers by viewModel.filteredMembers.collectAsState()
    val selectedMember by viewModel.selectedMember.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is DeleteMemberUiState.Success) {
            snackbarHostState.showSnackbar("Member deleted successfully")
            viewModel.resetState()
        } else if (uiState is DeleteMemberUiState.Error) {
            snackbarHostState.showSnackbar((uiState as DeleteMemberUiState.Error).message)
            viewModel.resetState()
        }
    }

    if (selectedMember != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearSelection,
            title = { Text("Delete Member") },
            text = { Text("Are you sure you want to delete member ${selectedMember?.name} (${selectedMember?.membershipId})? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::deleteMember,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::clearSelection) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delete Member") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or ID") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredMembers) { member ->
                    MemberRow(member = member) {
                        viewModel.selectMember(member)
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}
