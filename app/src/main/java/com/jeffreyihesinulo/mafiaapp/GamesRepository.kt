package com.jeffreyihesinulo.composeapp

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

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
    suspend fun createGame(
        result: String,
        season: Int,
        notes: String,
        players: Map<String, String>,
        allUsers: List<User>,
        hostId: String,
        willUid: String,
        willGuesses: Int
    ): Boolean
    {
        return try {
            val gamesCount = db.collection("games").get().await().size()
            val gameNumber = gamesCount + 1
            val gameData = hashMapOf(
                "gameNumber" to gameNumber,
                "result" to result,
                "season" to season,
                "notes" to notes,
                "date" to com.google.firebase.Timestamp.now(),
                "createdBy" to Firebase.auth.currentUser?.uid,
                "players" to players.map{(uid, role) ->
                    val user = allUsers.find { it.uid == uid}
                    hashMapOf(
                        "uid" to uid,
                        "username" to (user?.username ?: ""),
                        "mmrChange" to 0,
                        "role" to role,
                        "rank" to (user?.rank ?: "")
                    )
                }
            )
            val gameRef = db.collection("games").add(gameData).await()
            val gameId = gameRef.id
            calculateAndUpdateMMR(
                players = players,
                result = result,
                hostId = hostId,
                willUid = willUid,
                willGuesses = willGuesses,
                gameId = gameId
            )
            db.collection("activity").add(
                hashMapOf(
                    "type" to "game",
                    "title" to "New Game Recorded",
                    "body" to "Game #GM-$gameNumber has been processed. ${if (result == "town") "Town" else "Mafia"} Victory!",
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "createdBy" to Firebase.auth.currentUser?.uid,
                    "gameId" to gameId
                )
            ).await()

            true
        }catch (e: Exception){
            false
        }

    }
    suspend fun calculateAndUpdateMMR(
        players: Map<String, String>,
        result: String,
        hostId: String,
        willUid: String,
        willGuesses : Int,
        gameId : String
    ){
        players.forEach { (uid, role) ->

            var mmrGain = 1

            mmrGain += when{
                result == "town" && role == "Citizen" -> 5
                result == "town" && role == "Sheriff" -> 10
                result == "mafia" && role == "Mafia" -> 6
                result == "mafia" && role == "Don" -> 10
                else -> 0
            }
            if (uid == willUid) mmrGain += willGuesses
            val gameDoc = db.collection("games").document(gameId).get().await()
            val playerList = gameDoc.get("players") as? List<Map<String, Any>> ?: emptyList()

            val updatedPlayers = playerList.map {
                playerMap ->
                if (playerMap["uid"] == uid){
                    playerMap.toMutableMap().apply {
                        put("mmrChange", mmrGain)
                    }
                }else{
                    playerMap
                }
            }
            db.collection("games").document(gameId)
                .update("players", updatedPlayers)
                .await()
            val isWinner = (result == "town" && (role =="Citizen" || role == "Sheriff")) ||
                    (result == "mafia" && (role == "Mafia" || role == "Don"))

            val userDoc = db.collection("users").document(uid).get().await()
            val currentMmr = (userDoc.getLong("mmr") ?: 0).toInt()
            val newMmr = (currentMmr + mmrGain)
            val newRank = getRankFromMMR(newMmr)
            db.collection("users").document(uid).update(
                mapOf(
                    "mmr" to FieldValue.increment(mmrGain.toLong()),
                    "rank" to newRank,
                    "games" to FieldValue.increment(1L),
                    "wins" to FieldValue.increment(if (isWinner) 1L else 0L),
                    "losses" to FieldValue.increment(if (!isWinner) 1L else 0L),
                    "lastGameAt" to com.google.firebase.Timestamp.now()
                )
            ).await()
        }
        if (hostId.isNotEmpty()) {
            db.collection("users").document(hostId)
                .update("mmr", FieldValue.increment(1L))
                .await()
        }
    }
    fun getRankFromMMR(mmr: Int): String {
        return when {
            mmr >= 200 -> "LEGEND"
            mmr >= 100 -> "MASTER"
            mmr >= 50  -> "ELITE"
            mmr >= 25  -> "VETERAN"
            else       -> "IRON"
        }
    }

    suspend fun getPlayerMMRHistory(uid : String): List<Pair<Long, Int>>
    {
        return try{
            val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 *1000)
            val snapshot = db.collection("games").get().await()

            snapshot.documents.mapNotNull { doc ->
                val date = doc.getTimestamp("date")?.toDate()?.time ?: 0L
                if(date < oneMonthAgo) return@mapNotNull null

                val playerList = doc.get("players") as? List<Map<String, Any>> ?: emptyList()
                val player = playerList.find{ it["uid"] == uid}

                if (player != null){
                    val mmrChange = (player["mmrChange"] as? Long ?: 0).toInt()
                    Pair(date, mmrChange)
                }else null
            }.sortedBy { it.first }
        }catch (e: Exception){
            emptyList()
        }
    }
    suspend fun getPlayerRecentGames(uid: String): List<Game>{
        return try{
            val snapshot = db.collection("games")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                val playersRaw = doc.get("players") as? List<Map<String, Any>> ?: emptyList()
                val isParticipant = playersRaw.any{ it["uid"] == uid }
                if (!isParticipant) return@mapNotNull null

                val playerList = playersRaw.map { playerMap ->
                    GamePlayer(
                        uid = playerMap["uid"] as? String ?: "",
                        username = playerMap["username"] as? String ?: "",
                        role = playerMap["role"] as? String ?: "",
                        mmrChange = (playerMap["mmrChange"] as? Long ?: 0).toInt(),
                        rank = playerMap["rank"] as? String ?: ""
                    )
                }
                Game(
                    id = doc.id,
                    gameNumber = (doc.getLong("gameNumber") ?: 0).toInt(),
                    date = doc.getTimestamp("date")?.toDate()?.time ?: 0L,
                    result = doc.getString("result") ?: "",
                    season = (doc.getLong("season") ?: 0).toInt(),
                    players = playerList

                )
            }
        }catch (e: Exception)
        {
            emptyList()
        }

    }
    suspend fun getGameById(id: String): Game?
    {
        return try{
            val doc = db.collection("games").document(id).get().await()
            val playerList = doc.get("players") as? List<Map<String, Any>>?: emptyList()
                Game(
                    id = doc.id,
                    gameNumber = (doc.getLong("gameNumber") ?: 0).toInt(),
                    result = doc.getString("result") ?: "",
                    season = (doc.getLong("season")?:0).toInt(),
                    date = doc.getTimestamp("date")?.toDate()?.time ?: 0L,
                    createdBy = doc.getString("createdBy") ?: "",
                    players = playerList.map{ playerMap ->
                        GamePlayer(
                            uid = playerMap["uid"] as? String ?: "",
                            username = playerMap["username"] as? String ?: "",
                            role = playerMap["role"] as? String ?: "",
                            mmrChange = (playerMap["mmrChange"] as? Long ?: 0).toInt(),
                            rank = playerMap["rank"] as? String ?: ""

                        )
                    }
                )

            }catch (e: Exception){
                null
        }
    }




}
