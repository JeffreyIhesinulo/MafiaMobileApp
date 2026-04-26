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
            val snapshot = db.collection("players").get().await()
            snapshot.documents.map { doc ->
                Player(
                    name = doc.getString("name") ?: "",
                    rank = doc.getString("rank") ?: "",
                    rankColor = getRankColor(doc.getString("rank") ?: ""),
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