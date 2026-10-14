package com.example.memg.data

import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val membershipId: String = "",
    val name: String = "",
    val status: String = "",
    val mobileNumber: String = "",
    val alternateContact: String = "",
    val address: String = "",
    val membershipStartDate: String = "",
    val membershipEndDate: String = "",
    val familyMembers: List<String> = emptyList()
)
