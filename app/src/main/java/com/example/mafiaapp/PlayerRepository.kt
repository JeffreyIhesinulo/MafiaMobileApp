package com.example.composeapp

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class PlayerRepository {
    private val db = Firebase.firestore

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getPlayers(): List<Player> {
        return try {
            val snapshot = db.collection("users").get().await()
            snapshot.documents.map { doc ->
                Player(
                    username = doc.getString("username") ?: "",
                    rank = doc.getString("rank") ?: "",
                    rankColor = getRankColor(doc.getString("rank") ?: "0"),
                    mmr = (doc.getLong("mmr") ?: 0).toInt(),
                    mmrChange = (doc.getLong("mmrChange") ?: 0).toInt(),
                    games = (doc.getLong("games") ?: 0).toInt(),
                    isAdmin = doc.getBoolean("isAdmin") ?: false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}