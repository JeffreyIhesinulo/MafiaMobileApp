package io.github.jeffreyihesinulo.mafiaapp

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
            val lastGameMap = getLastGameTimestamps()
            snapshot.documents.map { doc ->
                Player(
                    uid = doc.id,
                    username = doc.getString("username") ?: "",
                    rank = doc.getString("rank") ?: "",
                    rankColor = getRankColor(doc.getString("rank") ?: "0"),
                    mmr = (doc.getLong("mmr") ?: 0).toInt(),
                    mmrChange = (doc.getLong("mmrChange") ?: 0).toInt(),
                    games = (doc.getLong("games") ?: 0).toInt(),
                    isAdmin = doc.getBoolean("isAdmin") ?: false,
                    lastGameAt = lastGameMap[doc.id] ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun getPendingUsers(): List<User> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("approved", false)
                .get().await()
            snapshot.documents.map { doc ->
                User(
                    uid = doc.id,
                    username = doc.getString("username") ?: "",
                    email = doc.getString("email") ?: "",
                    mmr = (doc.getLong("mmr") ?: 0).toInt(),
                    wins = (doc.getLong("wins") ?: 0).toInt(),
                    losses = (doc.getLong("losses") ?: 0).toInt(),
                    games = (doc.getLong("games") ?: 0).toInt(),
                    rank = doc.getString("rank") ?: "",
                    isAdmin = doc.getBoolean("isAdmin") ?: false,
                    approved = doc.getBoolean("approved") ?: false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }

    }
    suspend fun rejectUser(uid: String): Boolean {
        return try {
            db.collection("users").document(uid).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun approveUser(uid: String): Boolean {
        return try {
            db.collection("users").document(uid)
                .update("approved", true).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun getLastGameTimestamps(): Map<String, Long> {
        val snapshot = db.collection("games").get().await()
        val map = mutableMapOf<String, Long>()
        snapshot.documents.forEach { doc ->
            val date = doc.getTimestamp("date")?.toDate()?.time ?: 0L
            val playersList = doc.get("players") as? List<Map<String, Any>> ?: emptyList()
            playersList.forEach { playerMap ->
                val uid = playerMap["uid"] as? String ?: return@forEach
                if (date > (map[uid] ?: 0L)) map[uid] = date
            }
        }
        return map
    }


}