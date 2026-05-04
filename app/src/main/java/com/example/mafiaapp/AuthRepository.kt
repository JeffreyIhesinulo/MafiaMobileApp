package com.example.composeapp

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = Firebase.auth

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, username: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: ""
            Firebase.firestore.collection("users").document(uid).set(
                mapOf(
                        "username" to username,
                        "email" to email,
                        "isAdmin" to false,
                        "mmr" to 0,
                        "wins" to 0,
                        "losses" to 0,
                        "games" to 0,
                        "rank" to "IRON",
                        "approved" to false,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}