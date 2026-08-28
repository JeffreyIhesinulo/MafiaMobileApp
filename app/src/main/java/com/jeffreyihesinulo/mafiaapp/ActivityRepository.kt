package com.jeffreyihesinulo.composeapp

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

data class Activity(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val createdAt: Long = 0L,
    val createdBy: String = "",
    val gameId: String = ""
)

class ActivityRepository {
    private val db = Firebase.firestore

    suspend fun getActivities(): List<Activity> {
        return try {
            val snapshot = db.collection("activity")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.map { doc ->
                Activity(
                    id = doc.id,
                    type = doc.getString("type") ?: "",
                    title = doc.getString("title") ?: "",
                    body = doc.getString("body") ?: "",
                    createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                    createdBy = doc.getString("createdBy") ?: "",
                    gameId = doc.getString("gameId") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createAnnouncement(title: String, body: String, createdBy: String): Boolean {
        return try {
            db.collection("activity").add(
                hashMapOf(
                    "type" to "announcement",
                    "title" to title,
                    "body" to body,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "createdBy" to createdBy
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}