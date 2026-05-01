package com.example.composeapp

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.collections.get

data class Game(
    val id: String = "",
    val gameNumber: Int = 0,
    val result: String = "",
    val season: Int = 0,
    val date: Long = 0L,
    val createdBy: String = "",
    val players: List<GamePlayer> = emptyList()
)

data class GamePlayer(
    val uid: String = "",
    val username: String = "",
    val role: String = "",
    val mmrChange: Int = 0,
    val rank: String = "",

)
class GamesRepository{
    private val db = Firebase.firestore

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getGames(): List<Game> {
        return try {
            val snapshot = db.collection("games").get().await()
            snapshot.documents.map { doc ->
                val playersList = doc.get("players") as? List<Map<String, Any>> ?:emptyList()
                Game(
                    id = doc.id,
                    gameNumber = (doc.getLong("gameNumber") ?: 0).toInt(),
                    date = doc.getTimestamp("date")?.toDate()?.time ?: 0L,
                    result = doc.getString("result") ?: "",
                    season = (doc.getLong("season") ?: 0).toInt(),
                    players = playersList.map { playerMap ->
                        GamePlayer(
                            uid = playerMap["uid"] as? String ?: "",
                            username = playerMap["username"] as? String ?: "",
                            role = playerMap["role"] as? String ?: "",
                            mmrChange = (playerMap["mmrChange"] as? Long ?: 0).toInt(),
                            rank = playerMap["rank"] as? String ?: ""
                        )
                    }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

}
