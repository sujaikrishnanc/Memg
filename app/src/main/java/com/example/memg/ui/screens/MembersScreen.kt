package com.example.memg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memg.data.Member
import com.example.memg.ui.theme.MemgTheme
import com.example.memg.ui.viewmodels.MembersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    viewModel: MembersViewModel,
    onBack: () -> Unit,
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val members by viewModel.filteredMembers.collectAsState()

    MembersScreenContent(
        searchQuery = searchQuery,
        members = members,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreenContent(
    searchQuery: String,
    members: List<Member>,
    onSearchQueryChange: (String) -> Unit,
    onBack: () -> Unit,
) {
    var selectedMember by remember { mutableStateOf<Member?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("View Members") },
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
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or ID") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ID", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Name", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Status", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Table Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(members) { member ->
                    MemberRow(member = member) {
                        selectedMember = member
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }

    if (selectedMember != null) {
        MemberDetailsDialog(member = selectedMember!!) {
            selectedMember = null
        }
    }
}

@Composable
fun MemberRow(member: Member, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(member.membershipId, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Text(member.name, modifier = Modifier.weight(2f), fontSize = 13.sp)
        Text(
            text = member.status,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = if (member.status == "Active") Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
fun MemberDetailsDialog(member: Member, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Member Details", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailItem(label = "Membership ID", value = member.membershipId)
                DetailItem(label = "Name", value = member.name)
                DetailItem(label = "Status", value = member.status)
                DetailItem(label = "Mobile", value = member.mobileNumber)
                DetailItem(label = "Alt Contact", value = member.alternateContact.ifEmpty { "N/A" })
                DetailItem(label = "Address", value = member.address)
                DetailItem(label = "Membership Start Date", value = member.membershipStartDate)
                DetailItem(label = "Membership End Date", value = member.membershipEndDate)
                DetailItem(label = "Family Members", value = member.familyMembers.joinToString(", ").ifEmpty { "None" })
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun MembersScreenPreview() {
    val sampleMembers = listOf(
        Member(
            membershipId = "M001",
            name = "John Doe",
            status = "Active",
            mobileNumber = "9876543210",
            address = "123 Main St, Nattika",
            membershipStartDate = "01/01/2020",
            membershipEndDate = "31/12/2026",
            familyMembers = listOf("Jane Doe", "Jimmy Doe")
        ),
        Member(
            membershipId = "M002",
            name = "Jane Smith",
            status = "Non Active",
            mobileNumber = "8765432109",
            address = "456 Beach Rd, Nattika",
            membershipStartDate = "01/01/2010",
            membershipEndDate = "01/01/2020"
        ),
        Member(
            membershipId = "M003",
            name = "Robert Wilson",
            status = "Active",
            mobileNumber = "7654321098",
            address = "789 Park Lane, Nattika",
            membershipStartDate = "15/05/2022",
            membershipEndDate = "15/05/2027",
            familyMembers = listOf("Alice Wilson")
        )
    )

    MemgTheme {
        MembersScreenContent(
            searchQuery = "",
            members = sampleMembers,
            onSearchQueryChange = {},
            onBack = {}
        )
    }
}
