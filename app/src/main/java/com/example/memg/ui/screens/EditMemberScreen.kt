package com.example.memg.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memg.data.Member
import com.example.memg.ui.theme.MemgTheme
import com.example.memg.ui.viewmodels.EditMemberUiState
import com.example.memg.ui.viewmodels.EditMemberViewModel
import com.example.memg.ui.viewmodels.FamilyMemberInput

@Composable
fun EditMemberScreen(
    viewModel: EditMemberViewModel,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredMembers by viewModel.filteredMembers.collectAsState()
    val selectedMember by viewModel.selectedMember.collectAsState()
    
    val memberName by viewModel.memberName.collectAsState()
    val mobileNumber by viewModel.mobileNumber.collectAsState()
    val alternateContact by viewModel.alternateContact.collectAsState()
    val address by viewModel.address.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    EditMemberContent(
        searchQuery = searchQuery,
        filteredMembers = filteredMembers,
        selectedMember = selectedMember,
        memberName = memberName,
        mobileNumber = mobileNumber,
        alternateContact = alternateContact,
        address = address,
        startDate = startDate,
        familyMembers = familyMembers,
        uiState = uiState,
        onBack = onBack,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSelectMember = viewModel::selectMember,
        onClearSelection = viewModel::clearSelection,
        onMemberNameChange = viewModel::onMemberNameChange,
        onMobileNumberChange = viewModel::onMobileNumberChange,
        onAlternateContactChange = viewModel::onAlternateContactChange,
        onAddressChange = viewModel::onAddressChange,
        onAddFamilyMember = viewModel::addFamilyMember,
        onFamilyMemberChange = viewModel::onFamilyMemberChange,
        onRemoveFamilyMember = viewModel::removeFamilyMember,
        onUpdateMember = viewModel::updateMember,
        onResetState = viewModel::resetState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemberContent(
    searchQuery: String,
    filteredMembers: List<Member>,
    selectedMember: Member?,
    memberName: String,
    mobileNumber: String,
    alternateContact: String,
    address: String,
    startDate: String,
    familyMembers: List<FamilyMemberInput>,
    uiState: EditMemberUiState,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectMember: (Member) -> Unit,
    onClearSelection: () -> Unit,
    onMemberNameChange: (String) -> Unit,
    onMobileNumberChange: (String) -> Unit,
    onAlternateContactChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onAddFamilyMember: () -> Unit,
    onFamilyMemberChange: (Int, String, String) -> Unit,
    onRemoveFamilyMember: (Int) -> Unit,
    onUpdateMember: () -> Unit,
    onResetState: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState) {
        if (uiState is EditMemberUiState.Success) {
            snackbarHostState.showSnackbar("Member updated successfully")
            onResetState()
        } else if (uiState is EditMemberUiState.Error) {
            snackbarHostState.showSnackbar(uiState.message)
            onResetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedMember == null) "Select Member to Edit" else "Edit Member") },
                navigationIcon = {
                    IconButton(onClick = if (selectedMember == null) onBack else onClearSelection) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (selectedMember == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name or ID") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredMembers) { member ->
                        MemberRow(member = member) {
                            onSelectMember(member)
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Editing Membership ID: ${selectedMember.membershipId}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = memberName,
                    onValueChange = onMemberNameChange,
                    label = { Text("Member Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = onMobileNumberChange,
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                OutlinedTextField(
                    value = alternateContact,
                    onValueChange = onAlternateContactChange,
                    label = { Text("Alternate Phone Number (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = startDate,
                    onValueChange = { /* startDate is disabled */ },
                    label = { Text("Membership End Date") },
                    placeholder = { Text("dd/mm/yyyy") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Family Members", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onAddFamilyMember) {
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
                            onValueChange = { onFamilyMemberChange(index, it, familyMember.relation) },
                            label = { Text("Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = familyMember.relation,
                            onValueChange = { onFamilyMemberChange(index, familyMember.name, it) },
                            label = { Text("Relation") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = { onRemoveFamilyMember(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        keyboardController?.hide()
                        onUpdateMember()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is EditMemberUiState.Loading
                ) {
                    if (uiState is EditMemberUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Update Member")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditMemberSelectionPreview() {
    MemgTheme {
        EditMemberContent(
            searchQuery = "John",
            filteredMembers = listOf(
                Member(membershipId = "MEM001", name = "John Doe", status = "Active"),
                Member(membershipId = "MEM002", name = "Johnny Smith", status = "Inactive")
            ),
            selectedMember = null,
            memberName = "",
            mobileNumber = "",
            alternateContact = "",
            address = "",
            startDate = "",
            familyMembers = emptyList(),
            uiState = EditMemberUiState.Idle,
            onBack = {},
            onSearchQueryChange = {},
            onSelectMember = {},
            onClearSelection = {},
            onMemberNameChange = {},
            onMobileNumberChange = {},
            onAlternateContactChange = {},
            onAddressChange = {},
            onAddFamilyMember = {},
            onFamilyMemberChange = { _, _, _ -> },
            onRemoveFamilyMember = {},
            onUpdateMember = {},
            onResetState = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditMemberFormPreview() {
    MemgTheme {
        EditMemberContent(
            searchQuery = "",
            filteredMembers = emptyList(),
            selectedMember = Member(
                membershipId = "MEM001",
                name = "John Doe",
                status = "Active",
                mobileNumber = "1234567890",
                address = "123 Main St, Nattika",
                membershipStartDate = "01/01/2023"
            ),
            memberName = "John Doe",
            mobileNumber = "1234567890",
            alternateContact = "9876543210",
            address = "123 Main St, Nattika",
            startDate = "01/01/2023",
            familyMembers = listOf(
                FamilyMemberInput("Jane Doe", "Wife"),
                FamilyMemberInput("Jimmy Doe", "Son")
            ),
            uiState = EditMemberUiState.Idle,
            onBack = {},
            onSearchQueryChange = {},
            onSelectMember = {},
            onClearSelection = {},
            onMemberNameChange = {},
            onMobileNumberChange = {},
            onAlternateContactChange = {},
            onAddressChange = {},
            onAddFamilyMember = {},
            onFamilyMemberChange = { _, _, _ -> },
            onRemoveFamilyMember = {},
            onUpdateMember = {},
            onResetState = {}
        )
    }
}
