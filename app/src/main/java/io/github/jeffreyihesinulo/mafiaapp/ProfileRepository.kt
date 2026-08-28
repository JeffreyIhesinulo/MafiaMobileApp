package io.github.jeffreyihesinulo.mafiaapp

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
    suspend fun updateUsername(newUsername: String): Boolean
    {
        return try {
            val uid = auth.currentUser?.uid?: return false
            val check = db.collection("users").whereEqualTo("username", newUsername).get().await()

            if (!check.isEmpty) return false


            db.collection("users").document(uid).update("username", newUsername).await()
            true
        }   catch (e: Exception){
            false
        }
    }
    suspend fun getUsers(): List<User> {
        return try {
            val snapshot = db.collection("users").get().await()
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
    suspend fun getUserById(uid: String): User? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
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
        } catch (e: Exception) {
            null
        }
    }



}

