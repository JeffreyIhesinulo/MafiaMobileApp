package com.example.composeapp

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

data class User(
    val uid: String,
    val username: String,
    val email: String,
    val mmr: Int,
    val wins: Int,
    val losses: Int,
    val games: Int,
    val rank: String,
    val isAdmin: Boolean,
    val approved: Boolean
)
class ProfileRepository
{
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    suspend fun getCurrentUser(): User?
    {

        return try {
            val uid = auth.currentUser?.uid?: return null
            val doc = db.collection("users").document(uid).get().await()
            User(
                uid = doc.id,
                username = doc.getString("username")?:"",
                email = doc.getString("email")?:"",
                mmr = (doc.getLong("mmr")?:0).toInt(),
                wins = (doc.getLong("wins")?:0).toInt(),
                losses = (doc.getLong("losses")?:0).toInt(),
                games = (doc.getLong("games")?:0).toInt(),
                rank = doc.getString("rank")?:"",
                isAdmin = doc.getBoolean("isAdmin")?:false,
                approved = doc.getBoolean("approved")?:false
            )

        }
        catch(e : Exception)
        {
            null
        }

    }


}

