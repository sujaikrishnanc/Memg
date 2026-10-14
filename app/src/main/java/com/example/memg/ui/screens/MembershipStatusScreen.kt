package com.example.memg.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memg.PaymentResult
import com.example.memg.ui.viewmodels.MembershipStatusViewModel
import com.example.memg.data.Member
import com.example.memg.ui.viewmodels.PaymentStatus
import com.example.memg.ui.theme.MemgTheme
import com.razorpay.Checkout
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject
import com.example.memg.R
@Composable
fun MembershipStatusScreen(
    viewModel: MembershipStatusViewModel,
    paymentResult: SharedFlow<PaymentResult>,
    onBack: () -> Unit,
) {
    val member by viewModel.member.collectAsState()
    val paymentStatus by viewModel.paymentStatus.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(paymentResult) {
        paymentResult.collect { result ->
            when (result) {
                is PaymentResult.Success -> {
                    member?.let { viewModel.onPaymentSuccess(result.paymentId ?: "", it) }
                }
                is PaymentResult.Error -> {
                    viewModel.onPaymentError(result.code, result.description ?: "Unknown error")
                }
            }
        }
    }

    LaunchedEffect(paymentStatus) {
        if (paymentStatus is PaymentStatus.Success) {
            Toast.makeText(context, "Membership renewed successfully", Toast.LENGTH_LONG).show()
            viewModel.resetPaymentStatus()
        }
    }

    MembershipStatusScreenContent(
        member = member,
        paymentStatus = paymentStatus,
        canRenew = member?.let { viewModel.canRenew(it) } ?: false,
        onRenewClick = { member?.let { startPayment(activity, it) } },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipStatusScreenContent(
    member: Member?,
    paymentStatus: PaymentStatus,
    canRenew: Boolean,
    onRenewClick: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Membership Status") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (member == null) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StatusCard(member)

                    Spacer(modifier = Modifier.height(24.dp))

                    DetailsSection(member)

                    Spacer(modifier = Modifier.weight(1f))

                    if (canRenew) {
                        Button(
                            onClick = onRenewClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = paymentStatus !is PaymentStatus.Loading
                        ) {
                            if (paymentStatus is PaymentStatus.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text("Renew Membership")
                            }
                        }
                    }

                    if (paymentStatus is PaymentStatus.Error) {
                        Text(
                            text = paymentStatus.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(member: Member) {
    val isActive = member.status.equals("Active", ignoreCase = true)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isActive) "Active Membership" else "Inactive Membership",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Text(
                    text = "ID: ${member.membershipId}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun DetailsSection(member: Member) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailItem("Name", member.name)
        DetailItem("Valid From", member.membershipStartDate)
        DetailItem("Valid Till", member.membershipEndDate)
        DetailItem("Mobile", member.mobileNumber)
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

fun startPayment(activity: Activity, member: Member) {
    val checkout = Checkout()
    checkout.setKeyID(activity.getString(R.string.razorpay_key_id))

    try {
        val options = JSONObject()
        options.put("name", "Memg")
        options.put("description", "Membership Renewal - ${member.membershipId}")
        options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
        options.put("theme.color", "#3399cc")
        options.put("currency", "INR")
        options.put("amount", "100") // 1.00 in paise (as per your recent change)
        
        // Option 2: Placeholder for Order ID (required for live/secure payments)
        // options.put("order_id", "order_DBJOWzybf0sJbb") 
        
        options.put("prefill.contact", member.mobileNumber)
        options.put("prefill.email", "${member.mobileNumber}@memg.org")

        checkout.open(activity, options)
    } catch (e: Exception) {
        Toast.makeText(activity, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Preview(showBackground = true, name = "Active Membership")
@Composable
fun MembershipStatusScreenActivePreview() {
    val sampleMember = Member(
        membershipId = "M12345",
        name = "Sujai Santhosh",
        status = "Active",
        mobileNumber = "9876543210",
        membershipStartDate = "01/01/2023",
        membershipEndDate = "01/01/2027"
    )
    MemgTheme {
        MembershipStatusScreenContent(
            member = sampleMember,
            paymentStatus = PaymentStatus.Idle,
            canRenew = false,
            onRenewClick = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Renewable Membership")
@Composable
fun MembershipStatusScreenRenewablePreview() {
    val sampleMember = Member(
        membershipId = "M12345",
        name = "Sujai Santhosh",
        status = "Active",
        mobileNumber = "9876543210",
        membershipStartDate = "01/01/2023",
        membershipEndDate = "10/09/2026" // Assuming today is around 06/09/2026
    )
    MemgTheme {
        MembershipStatusScreenContent(
            member = sampleMember,
            paymentStatus = PaymentStatus.Idle,
            canRenew = true,
            onRenewClick = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun MembershipStatusScreenLoadingPreview() {
    MemgTheme {
        MembershipStatusScreenContent(
            member = null,
            paymentStatus = PaymentStatus.Idle,
            canRenew = false,
            onRenewClick = {},
            onBack = {}
        )
    }
}
