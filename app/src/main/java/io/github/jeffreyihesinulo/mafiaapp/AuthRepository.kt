package io.github.jeffreyihesinulo.mafiaapp

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = Firebase.auth

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Login failed"))

            if (!user.isEmailVerified) {
                auth.signOut()
                return Result.failure(Exception("Email not verified"))
            }

            val userDoc = Firebase.firestore.collection("users")
                .document(user.uid)
                .get().await()

            if (userDoc.getBoolean("approved") != true) {
                auth.signOut()
                return Result.failure(Exception("Account not approved"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, username: String): Result<Unit> {
        val db = Firebase.firestore
        val nameKey = username.lowercase()

        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Registration failed"))


            try {
                db.collection("usernames").document(nameKey)
                    .set(mapOf("uid" to user.uid)).await()
            } catch (e: Exception) {
                user.delete().await()
                auth.signOut()
                return Result.failure(Exception("Username already taken"))
            }

            try {
                db.collection("users").document(user.uid).set(
                    mapOf(
                        "username" to username,
                        "email" to email,
                        "isAdmin" to false,
                        "mmr" to 0,
                        "wins" to 0,
                        "losses" to 0,
                        "games" to 0,
                        "rank" to "IRON",
                        "approved" to false
                    )
                ).await()
            } catch (e: Exception) {

                db.collection("usernames").document(nameKey).delete().await()
                user.delete().await()
                auth.signOut()
                return Result.failure(e)
            }

            user.sendEmailVerification().await()


            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            auth.signOut()
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun resetPassword(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}