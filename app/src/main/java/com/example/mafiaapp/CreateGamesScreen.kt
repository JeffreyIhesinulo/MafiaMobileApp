package com.example.composeapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CreateGamesScreen() {
    var selectedPlayers by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var notes by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("town") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var isLoading by remember { mutableStateOf(false) }
    var players by remember { mutableStateOf<List<User>>(emptyList()) }
    val repository = remember { ProfileRepository() }
    val gamesRepository = remember { GamesRepository() }

    LaunchedEffect(Unit) {
        players = repository.getUsers()
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavBackground)
            .verticalScroll(rememberScrollState())
    ) {

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp)
                        .background(PurpleMain, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Games",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = {Text("Notes")},
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            minLines = 3
        )
        Row(modifier = Modifier.padding(16.dp))
        {
            Button(onClick = { result = "town" }){ Text("♥ Town")}
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { result = "mafia" }) { Text("♠ Mafia") }
        }
        Text(
            "Select Players",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        players.forEach { player ->
            val isSelected = selectedPlayers.containsKey(player.uid)
            val currentRole = selectedPlayers[player.uid] ?: ""

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { checked ->
                        selectedPlayers = if(checked)
                        {
                            selectedPlayers + (player.uid to "Citizen")
                        } else{
                            selectedPlayers - player.uid
                        }
                    }
                )
                Text(player.username, color = Color.White)
            }

        }
    }

}

