package com.example.memg.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memg.data.Notice
import com.example.memg.data.UserRole
import com.example.memg.ui.viewmodels.DashboardViewModel
import com.example.memg.ui.viewmodels.UploadStatus
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.example.memg.ui.theme.MemgTheme
import com.example.memg.R

@Composable
fun DashboardScreen(
    role: UserRole,
    onLogout: () -> Unit,
    onAddMember: () -> Unit,
    onViewMembers: () -> Unit,
    onEditMember: () -> Unit,
    onDeleteMember: () -> Unit,
    onMembershipStatus: () -> Unit,
    onIssueNotice: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val uploadStatus by viewModel.uploadStatus.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val isLoadingNotices by viewModel.isLoadingNotices.collectAsState()
    val currentMember by viewModel.currentMember.collectAsState()

    DashboardScreenContent(
        role = role,
        onLogout = onLogout,
        onAddMember = onAddMember,
        onViewMembers = onViewMembers,
        onEditMember = onEditMember,
        onDeleteMember = onDeleteMember,
        onMembershipStatus = onMembershipStatus,
        onIssueNotice = onIssueNotice,
        uploadStatus = uploadStatus,
        notices = notices,
        isLoadingNotices = isLoadingNotices,
        currentMember = currentMember,
        onUploadCsv = { uri -> viewModel.uploadCsvFile(context, uri) },
        onResetStatus = { viewModel.resetStatus() }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DashboardScreenContent(
    role: UserRole,
    onLogout: () -> Unit,
    onAddMember: () -> Unit,
    onViewMembers: () -> Unit,
    onEditMember: () -> Unit,
    onDeleteMember: () -> Unit,
    onMembershipStatus: () -> Unit,
    onIssueNotice: () -> Unit,
    uploadStatus: UploadStatus,
    notices: List<Notice>,
    isLoadingNotices: Boolean,
    currentMember: com.example.memg.data.Member?,
    onUploadCsv: (Uri?) -> Unit,
    onResetStatus: () -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showCsvDialog by remember { mutableStateOf(false) }
    var showMembershipDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uploadStatus) {
        if (uploadStatus is UploadStatus.Success) {
            Toast.makeText(context, "Members added successfully", Toast.LENGTH_LONG).show()
            onResetStatus()
            showCsvDialog = false
        }
    }

    if (showMembershipDialog && currentMember != null) {
        MembershipStatusDialog(member = currentMember) {
            showMembershipDialog = false
        }
    }

    if (showCsvDialog) {
        CsvUploadDialog(
            onDismiss = { 
                showCsvDialog = false 
                onResetStatus()
            },
            onUpload = { uri: Uri? ->
                onUploadCsv(uri)
            },
            status = uploadStatus
        )
    }

    if (uploadStatus is UploadStatus.Error) {
        AlertDialog(
            onDismissRequest = { onResetStatus() },
            title = { Text("Upload Failed") },
            text = { Text("${uploadStatus.message}. Please try again.") },
            confirmButton = {
                TextButton(onClick = { onResetStatus() }) {
                    Text("OK")
                }
            }
        )
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane(modifier = Modifier) {
                DashboardList(
                    role = role,
                    notices = notices,
                    isLoadingNotices = isLoadingNotices,
                    currentMember = currentMember,
                    onWelcomeCardClick = { showMembershipDialog = true },
                    onItemSelected = { item ->
                        when (item) {
                            "View Members" -> onViewMembers()
                            "Add Members using CSV File" -> showCsvDialog = true
                            "Add a new Member" -> onAddMember()
                            "Edit a Member" -> onEditMember()
                            "Delete a Member" -> onDeleteMember()
                            "Membership Status" -> onMembershipStatus()
                            "Issue a Notice" -> onIssueNotice()
                            else -> {
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
                                }
                            }
                        }
                    },
                    onLogout = onLogout
                )
            }
        },
        detailPane = {
            AnimatedPane(modifier = Modifier) {
                val selectedItem = navigator.currentDestination?.contentKey
                DashboardDetail(
                    role = role,
                    item = selectedItem,
                    onBack = {
                        scope.launch {
                            navigator.navigateBack()
                        }
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardList(
    role: UserRole,
    notices: List<Notice>,
    isLoadingNotices: Boolean,
    currentMember: com.example.memg.data.Member?,
    onWelcomeCardClick: () -> Unit,
    onItemSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isMember = role == UserRole.MEMBER
    var selectedBottomItem by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White, // Match your desired background color
                shadowElevation = 4.dp // Optional: gives it the top bar elevation look
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp, 8.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.memg),
                        contentDescription = "Organization Logo",
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Memg",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8C252C))
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(16.dp, 8.dp)) {
                    IconButton(onClick = { onItemSelected("My Profile") }) {
                        Icon(Icons.Rounded.AccountCircle, contentDescription = "Profile", tint=Color(0xFF8C252C) )
                    }
                    Text(
                        text = "See Profile",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.offset(y = (-8).dp)
                    )
                    /* Transfer the logout button to AdminProfileScreen
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Rounded.RemoveCircle, contentDescription = "Logout", modifier = Modifier.offset(y = (-8).dp), tint=Color(0xFF8C252C))
                    }*/
                }
            }
        },
        bottomBar = {
            if (isMember) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedBottomItem == 0,
                        onClick = { selectedBottomItem = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedBottomItem == 1,
                        onClick = { 
                            selectedBottomItem = 1
                            onItemSelected("Membership Status")
                        },
                        icon = { Icon(Icons.Default.ContactPage, contentDescription = "Membership Status") },
                        label = { Text("Renew Membership") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),

                ) {
                if (role == UserRole.ADMIN) {
                    item {
                        WelcomeCard(
                            name = currentMember?.name ?: "Member",
                            isAdmin = !isMember,
                            status = currentMember?.status ?: "Unknown",
                            onClick = onWelcomeCardClick
                        )
                    }
                    item {
                        ManagementCategory(title = "General Management")
                    }
                    items(listOf("View Members", "Add Members using CSV File", "Add a new Member", "Edit a Member", "Delete a Member", "Issue a Notice")) { item ->
                        DashboardListItem(text = item, onClick = { onItemSelected(item) })
                    }
                } else {
                    item {
                        WelcomeCard(
                            name = currentMember?.name ?: "Member",
                            isAdmin = !isMember,
                            status = currentMember?.status ?: "Unknown",
                            onClick = onWelcomeCardClick
                        )
                    }

                    item {
                        ManagementCategory(title = "Announcements")
                    }
                    
                    if (isLoadingNotices) {
                        items(3) {
                            ShimmerNoticeItem()
                        }
                    } else if (notices.isEmpty()) {
                        item {
                            Text("No announcements yet", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(notices) { notice ->
                            NoticeListItem(notice = notice, onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, notice.pdfUrl.toUri())
                                context.startActivity(intent)
                            })
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun WelcomeCard(name: String, isAdmin: Boolean, status: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = Color.LightGray
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint=Color(0xFF8C252C),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                if(isAdmin) {
                    Text(
                        text = "Welcome, Admin.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                else {
                    Text(
                        text = "Welcome, $name.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Click for more information",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            if(!isAdmin) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (status.equals("Active", ignoreCase = true)) Color(0xFF2E7D32) else Color.Red
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)

            }
        }
    }
}

@Composable
fun MembershipStatusDialog(member: com.example.memg.data.Member, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Membership Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow(label = "Name", value = member.name)
                DetailRow(label = "Start Date", value = member.membershipStartDate)
                DetailRow(label = "End Date", value = member.membershipEndDate)
                DetailRow(label = "Status", value = member.status)
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
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun NoticeListItem(notice: Notice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notice.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(notice.fileName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            TextButton(onClick = onClick) {
                Text("View Notice")
            }
        }
    }
}

@Composable
fun ShimmerNoticeItem() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(32.dp).background(brush))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).background(brush))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp).background(brush))
            }
        }
    }
}

@Composable
fun ManagementCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = Color(0xFF8C252C),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun DashboardListItem(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = BorderStroke(2.dp, Color.Gray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text,
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardDetail(
    role: UserRole,
    item: String?,
    onBack: () -> Unit
) {
    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select an item to see details")
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(item) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.Settings, contentDescription = "Back")
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
                Text(
                    text = "Details for $item",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "This is a responsive detail view for $item. On tablets or larger screens, this appears next to the list. On phones, it takes up the full screen.",
                    style = MaterialTheme.typography.bodyMedium
                )
                // Role specific detail placeholder
                if (role == UserRole.ADMIN) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { /* TODO */ }) {
                        Text("Manage $item")
                    }
                }
            }
        }
    }
}

@Composable
fun CsvUploadDialog(
    onDismiss: () -> Unit,
    onUpload: (Uri?) -> Unit,
    status: UploadStatus
) {
    var csvUri by remember { mutableStateOf<Uri?>(null) }

    val csvPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> csvUri = uri }
    )

    AlertDialog(
        onDismissRequest = if (status is UploadStatus.Loading) ({}) else onDismiss,
        title = { Text("Upload CSV File") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("CSV Format Requirements:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("Header (must be in this order):", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "\"Membership ID\", \"Member Name\", \"Phone Number\", \"Alternate Phone Number\", \"Address\", \"Membership Start Date\", \"Membership End Date\", \"List of family members\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Family Members Column Format:", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "\"(Name 1, Relation), (Name 2, Relation), ... etc\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Date Format:", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "dd/mm/yyyy (e.g., 06/09/2026)",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text("Please upload the CSV file containing member information.", style = MaterialTheme.typography.bodySmall)

                // CSV File
                Column {
                    Button(
                        onClick = { csvPicker.launch(arrayOf("text/comma-separated-values", "text/csv")) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = status !is UploadStatus.Loading
                    ) {
                        Text(if (csvUri == null) "Select CSV File" else "CSV File Selected")
                    }
                    if (csvUri != null) {
                        Text(
                            text = csvUri?.lastPathSegment ?: "File selected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (status is UploadStatus.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpload(csvUri) },
                enabled = csvUri != null && status !is UploadStatus.Loading
            ) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = status !is UploadStatus.Loading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenAdminPreview() {
    MemgTheme {
        DashboardScreenContent(
            role = UserRole.ADMIN,
            onLogout = {},
            onAddMember = {},
            onViewMembers = {},
            onEditMember = {},
            onDeleteMember = {},
            onMembershipStatus = {},
            onIssueNotice = {},
            uploadStatus = UploadStatus.Idle,
            notices = emptyList(),
            isLoadingNotices = false,
            currentMember = null,
            onUploadCsv = { _ -> },
            onResetStatus = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenMemberPreview() {
    MemgTheme() {
        DashboardScreenContent(
            role = UserRole.MEMBER,
            onLogout = {},
            onAddMember = {},
            onViewMembers = {},
            onEditMember = {},
            onDeleteMember = {},
            onMembershipStatus = {},
            onIssueNotice = {},
            uploadStatus = UploadStatus.Idle,
            notices = listOf(Notice(title = "Sample Notice", fileName = "notice.pdf")),
            isLoadingNotices = true, // Show shimmer in preview
            currentMember = com.example.memg.data.Member(
                name = "Brian",
                status = "Active",
                membershipStartDate = "01/01/2023",
                membershipEndDate = "01/01/2024"
            ),
            onUploadCsv = { _ -> },
            onResetStatus = {}
        )
    }
}
