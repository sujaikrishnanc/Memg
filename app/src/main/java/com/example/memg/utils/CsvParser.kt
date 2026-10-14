package com.example.memg.utils

import com.example.memg.data.Member
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object CsvParser {

    private val dateFormats = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    )

    private fun isValidPhoneNumber(phone: String): Boolean {
        return (phone.length == 10) && phone.all { it.isDigit() }
    }

    fun parseMembersFromSingleCsv(inputStream: InputStream): List<Member> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val headerLine = reader.readLine()
        val header = headerLine?.let { parseCsvLine(it) }?.map { it.trim().removeSurrounding("\"") }
            ?: throw Exception("CSV file is empty")

        val requiredColumns = listOf(
            "Membership ID",
            "Member Name",
            "Phone Number",
            "Alternate Phone Number",
            "Address",
            "Membership Start Date",
            "Membership End Date",
            "List of family members"
        )
        if (!header.containsAll(requiredColumns)) {
            throw Exception("Missing required columns in CSV. Required: $requiredColumns")
        }

        val idIdx = header.indexOf("Membership ID")
        val nameIdx = header.indexOf("Member Name")
        val phoneIdx = header.indexOf("Phone Number")
        val altPhoneIdx = header.indexOf("Alternate Phone Number")
        val addressIdx = header.indexOf("Address")
        val startIdx = header.indexOf("Membership Start Date")
        val endIdx = header.indexOf("Membership End Date")
        val familyIdx = header.indexOf("List of family members")

        val members = mutableListOf<Member>()
        var line: String? = reader.readLine()
        var rowCount = 0
        val today = LocalDate.now()
        val maxEndDate = today.plusYears(1)

        while (line != null) {
            if (line.isBlank()) {
                line = reader.readLine()
                continue
            }
            val columns = parseCsvLine(line).map { it.trim().removeSurrounding("\"") }

            val id = columns.getOrNull(idIdx) ?: ""
            val name = columns.getOrNull(nameIdx) ?: ""
            val phone = columns.getOrNull(phoneIdx) ?: ""
            val altPhone = columns.getOrNull(altPhoneIdx) ?: ""
            val address = columns.getOrNull(addressIdx) ?: ""
            val startDateStr = columns.getOrNull(startIdx) ?: ""
            val endDateStr = columns.getOrNull(endIdx) ?: ""
            val familyStr = columns.getOrNull(familyIdx) ?: ""

            val rowNum = rowCount + 2

            if (id.isEmpty() || name.isEmpty() || phone.isEmpty() || address.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
                throw Exception("Row $rowNum is missing mandatory fields")
            }

            if (!isValidPhoneNumber(phone)) {
                throw Exception("Invalid Phone Number in row $rowNum: $phone (Must be 10 digits)")
            }

            if (altPhone.isNotEmpty() && !isValidPhoneNumber(altPhone)) {
                throw Exception("Invalid Alternate Phone Number in row $rowNum: $altPhone (Must be 10 digits)")
            }

            val startDate = parseDate(startDateStr) ?: throw Exception("Invalid Start Date format in row $rowNum: $startDateStr (Use dd/mm/yyyy)")
            val endDate = parseDate(endDateStr) ?: throw Exception("Invalid End Date format in row $rowNum: $endDateStr (Use dd/mm/yyyy)")

            if (endDate.isAfter(maxEndDate)) {
                throw Exception("Membership End Date in row $rowNum ($endDateStr) cannot be more than one year from today")
            }

            val status = if (startDate.isBefore(today.plusDays(1)) && endDate.isAfter(today.minusDays(1))) "Active" else "Inactive"

            members.add(
                Member(
                    membershipId = id,
                    name = name,
                    status = status,
                    mobileNumber = phone,
                    alternateContact = altPhone,
                    address = address,
                    membershipStartDate = startDateStr,
                    membershipEndDate = endDateStr,
                    familyMembers = parseFamilyMembers(familyStr, rowNum)
                )
            )
            rowCount++
            line = reader.readLine()
        }

        if (rowCount == 0) {
            throw Exception("CSV must contain at least one data row")
        }

        return members
    }

    private fun parseFamilyMembers(familyStr: String, rowNum: Int): List<String> {
        if (familyStr.isBlank()) return emptyList()
        
        // Input format: (x, y), (name2, relation2)
        val result = mutableListOf<String>()
        val pairs = familyStr.split("),").map { it.trim() }
        
        for (pair in pairs) {
            val cleanedPair = pair.removePrefix("(").removeSuffix(")")
            val parts = cleanedPair.split(",").map { it.trim() }
            if (parts.size != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
                throw Exception("Invalid Family Member format in row $rowNum: \"$pair\". Expected \"(Name, Relation)\"")
            }
            val name = parts[0]
            val relation = parts[1]
            result.add("$name ($relation)")
        }
        return result
    }

    private fun parseDate(dateStr: String): LocalDate? {
        for (format in dateFormats) {
            try {
                return LocalDate.parse(dateStr, format)
            } catch (_: DateTimeParseException) {
                // Try next format
            }
        }
        return null
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (char in line) {
            when (char) {
                '\"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) {
                        current.append(char)
                    } else {
                        result.add(current.toString())
                        current = StringBuilder()
                    }
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }
}
