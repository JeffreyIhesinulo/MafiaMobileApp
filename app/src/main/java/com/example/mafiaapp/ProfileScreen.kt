package com.example.composeapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.composeapp.NavBackground
import com.example.composeapp.PurpleMain
import com.google.firebase.auth.ktx.auth

@Composable
fun ProfileScreen() {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val repository = remember { ProfileRepository() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PurpleMain)
            }
        } else {
            Text(
                text = user?.username ?: "No user",
                color = Color.White
            )
            Text(
                text = "${user?.mmr} MMR",
                color = Color.White
            )
        }
    }
    LaunchedEffect(Unit) {
        try {
            val uid = com.google.firebase.ktx.Firebase.auth.currentUser?.uid
            println("DEBUG uid = $uid")
            user = repository.getCurrentUser()
            println("DEBUG user = $user")
        } catch(e: Exception) {
            println("DEBUG ERROR = ${e.message}")
            println("DEBUG ERROR type = ${e.javaClass.simpleName}")
        }
        isLoading = false
    }
}
