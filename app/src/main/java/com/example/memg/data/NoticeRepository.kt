package com.example.memg.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NoticeRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    fun getNotices(): Flow<List<Notice>> {
        return db.collection("notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Notice::class.java)
            }
    }

    suspend fun uploadNotice(title: String, pdfUri: Uri, fileName: String): Result<Unit> = try {
        val noticeId = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("notices/$noticeId.pdf")
        
        storageRef.putFile(pdfUri).await()
        val downloadUrl = storageRef.downloadUrl.await().toString()
        
        val notice = Notice(
            id = noticeId,
            title = title,
            pdfUrl = downloadUrl,
            fileName = fileName
        )
        
        db.collection("notices").document(noticeId).set(notice).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteNotice(noticeId: String, pdfUrl: String): Result<Unit> = try {
        // Delete from Firestore
        db.collection("notices").document(noticeId).delete().await()
        
        // Delete from Storage
        val storageRef = storage.getReferenceFromUrl(pdfUrl)
        storageRef.delete().await()
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
