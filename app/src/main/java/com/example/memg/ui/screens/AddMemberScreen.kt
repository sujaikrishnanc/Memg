package com.example.memg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.memg.ui.viewmodels.AddMemberUiState
import com.example.memg.ui.viewmodels.AddMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    viewModel: AddMemberViewModel,
    onBack: () -> Unit
) {
    val membershipId by viewModel.membershipId.collectAsState()
    val memberName by viewModel.memberName.collectAsState()
    val mobileNumber by viewModel.mobileNumber.collectAsState()
    val alternateContact by viewModel.alternateContact.collectAsState()
    val address by viewModel.address.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState) {
        if (uiState is AddMemberUiState.Success) {
            snackbarHostState.showSnackbar("Member added successfully")
            viewModel.resetState()
        } else if (uiState is AddMemberUiState.Error) {
            snackbarHostState.showSnackbar((uiState as AddMemberUiState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Member") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = membershipId,
                onValueChange = {},
                label = { Text("Membership ID (Generated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                enabled = false
            )

            OutlinedTextField(
                value = memberName,
                onValueChange = viewModel::onMemberNameChange,
                label = { Text("Member Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = viewModel::onMobileNumberChange,
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            OutlinedTextField(
                value = alternateContact,
                onValueChange = viewModel::onAlternateContactChange,
                label = { Text("Alternate Phone Number (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            OutlinedTextField(
                value = address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = viewModel::onStartDateChange,
                label = { Text("Membership Start Date") },
                placeholder = { Text("dd/mm/yyyy") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState is AddMemberUiState.Error && (uiState as AddMemberUiState.Error).message.contains("date", ignoreCase = true)
            )

            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Family Members", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = viewModel::addFamilyMember) {
                    Icon(Icons.Default.Add, contentDescription = "Add Family Member")
                }
            }

            familyMembers.forEachIndexed { index, familyMember ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = familyMember.name,
                        onValueChange = { viewModel.onFamilyMemberChange(index, it, familyMember.relation) },
                        label = { Text("Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = familyMember.relation,
                        onValueChange = { viewModel.onFamilyMemberChange(index, familyMember.name, it) },
                        label = { Text("Relation") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = { viewModel.removeFamilyMember(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.reviewMember()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AddMemberUiState.Loading
            ) {
                if (uiState is AddMemberUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Add Member")
                }
            }
        }
    }

    if (uiState is AddMemberUiState.Reviewing) {
        val member = (uiState as AddMemberUiState.Reviewing).member
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            title = { Text("Verify Member Details") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MemberDetailRow("Membership ID", member.membershipId)
                    MemberDetailRow("Name", member.name)
                    MemberDetailRow("Phone", member.mobileNumber)
                    if (member.alternateContact.isNotBlank()) {
                        MemberDetailRow("Alt Phone", member.alternateContact)
                    }
                    MemberDetailRow("Address", member.address)
                    MemberDetailRow("Start Date", member.membershipStartDate)
                    MemberDetailRow("End Date", member.membershipEndDate)
                    
                    Text("Family Members:", style = MaterialTheme.typography.titleSmall)
                    member.familyMembers.forEach {
                        Text("- $it", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.addMember(member) }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("Edit")
                }
            }
        )
    }
}

@Composable
private fun MemberDetailRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
