package com.example.memg.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MembersRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Returns a real-time Flow of all members from the 'members' collection.
     */
    fun getMembers(): Flow<List<Member>> {
        return db.collection("members")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Member::class.java)
            }
    }

    /**
     * Saves a list of members to Firestore using a batch operation.
     */
    suspend fun saveMembers(members: List<Member>): Result<Unit> = try {
        val batch = db.batch()
        members.forEach { member ->
            val docRef = db.collection("members").document(member.membershipId)
            batch.set(docRef, member)
        }
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Generates a new unique Membership ID by finding the highest current ID.
     */
    suspend fun generateNextMembershipId(): String {
        return try {
            val snapshot = db.collection("members").get().await()
            val ids = snapshot.documents.mapNotNull { it.id.removePrefix("M").toIntOrNull() }
            val nextId = (ids.maxOrNull() ?: 0) + 1
            "M${nextId.toString().padStart(3, '0')}"
        } catch (e: Exception) {
            "M${System.currentTimeMillis().toString().takeLast(4)}" // Fallback
        }
    }

    /**
     * Updates an existing member in Firestore.
     */
    suspend fun updateMember(member: Member): Result<Unit> = try {
        db.collection("members").document(member.membershipId).set(member).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Returns a flow of a specific member by mobile number.
     */
    fun getMemberByMobile(mobileNumber: String): Flow<Member?> {
        return db.collection("members")
            .whereEqualTo("mobileNumber", mobileNumber)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Member::class.java).firstOrNull()
            }
    }

    /**
     * Renews a membership by updating status and end date.
     */
    suspend fun renewMembership(membershipId: String, newEndDate: String): Result<Unit> = try {
        db.collection("members").document(membershipId)
            .update(
                mapOf(
                    "status" to "Active",
                    "membershipEndDate" to newEndDate
                )
            ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Deletes a member from Firestore.
     */
    suspend fun deleteMember(membershipId: String): Result<Unit> = try {
        db.collection("members").document(membershipId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
