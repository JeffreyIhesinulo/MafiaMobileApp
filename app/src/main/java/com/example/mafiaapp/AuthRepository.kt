package com.example.composeapp

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = Firebase.auth

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {

            val result = auth.signInWithEmailAndPassword(email, password).await()
            if (result.user?.isEmailVerified == false)
            {
                auth.signOut()
                return Result.failure(Exception("Email not verified"))
            }
            val userDoc = Firebase.firestore.collection("users")
                .document(result.user?.uid ?: "")
                .get().await()

            if (userDoc.getBoolean("approved") == false) {
                auth.signOut()
                return Result.failure(Exception("Account not approved"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, username: String): Result<Unit> {
        return try {
             val usernameCheck = Firebase.firestore.collection("users")
                 .whereEqualTo("username", username)
                 .get().await()

            if (!usernameCheck.isEmpty)
            {
                return Result.failure(Exception("Username already taken"))
            }
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: ""
            result.user?.sendEmailVerification()?.await()
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
    suspend fun resetPassword(email: String): Boolean{
        return try{
            Firebase.auth.sendPasswordResetEmail(email).await()
            true
        }
        catch (e: Exception)
        {
            false
        }
    }
}