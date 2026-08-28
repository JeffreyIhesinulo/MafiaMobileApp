package com.jeffreyihesinulo.composeapp

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


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

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var hostId by remember { mutableStateOf("") }


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
            Button(onClick = { result = "town" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(result == "town") PurpleMain else CardBg
                )
            ){ Text("♥ Town")}
            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { result = "mafia" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(result == "mafia") PurpleMain else CardBg)
            )
            { Text("♠ Mafia") }
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
            var expanded by remember { mutableStateOf(false) }

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
                            selectedPlayers + (player.uid to "")
                        } else{
                            selectedPlayers - player.uid
                        }
                    }
                )
                Text(player.username, color = Color.White)

                if (isSelected)
                {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box{
                        OutlinedButton(
                            onClick = { expanded = true },
                            border = BorderStroke(1.dp, PurpleMain),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(currentRole.ifEmpty { "Role ▼" },
                                fontSize = 12.sp)

                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false}
                        ) {
                            listOf("Citizen", "Sheriff", "Mafia", "Don").forEach { role ->
                                DropdownMenuItem(
                                    text ={ Text(role)},
                                    onClick = {
                                        selectedPlayers = selectedPlayers + (player.uid to role)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        var hostExpanded by remember { mutableStateOf(false)}
        val hostName = players.find {it.uid == hostId }?.username ?: "Select Host"


        Box(modifier = Modifier.padding(horizontal = 16.dp)){
            OutlinedButton(
                onClick = { hostExpanded = true},
                border = BorderStroke(1.dp, PurpleMain)
            ) {
                Text(hostName)
            }
            DropdownMenu(
                expanded = hostExpanded,
                onDismissRequest = { hostExpanded = false}
            ) {
                players.forEach { player ->
                    DropdownMenuItem(
                        text = { Text(player.username)},
                        onClick = {
                            hostId = player.uid
                            hostExpanded = false
                        }
                    )
                }
            }
        }
        var hasWill by remember {mutableStateOf(false)}
        var willUid by remember { mutableStateOf("") }
        var willGuesses by remember { mutableStateOf(0) }

        Row(modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            Checkbox(
                checked = hasWill,
                onCheckedChange = {hasWill = it}
            )
            Text("Last Will", color = Color.Red, fontWeight = FontWeight.Bold)
        }
        if (hasWill) {
            var willExpanded by remember { mutableStateOf(false) }
            val willName = players.find { it.uid == willUid }?.username ?: "Select Player"

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedButton(
                    onClick = { willExpanded = true },
                    border = BorderStroke(1.dp, PurpleMain)
                ) {
                    Text(willName)
                }
                DropdownMenu(
                    expanded = willExpanded,
                    onDismissRequest = { willExpanded = false }
                ) {
                    players.forEach { player ->
                        DropdownMenuItem(
                            text = { Text(player.username) },
                            onClick = {
                                willUid = player.uid
                                willExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Guesses: $willGuesses", color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { if (willGuesses > 0) willGuesses-- }) { Text("-") }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = { if (willGuesses < 4) willGuesses++ }) { Text("+") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                    val roles = selectedPlayers.values.toList()

                    when {
                        selectedPlayers.size < 8 -> {
                            Toast.makeText(context, "Minimum 8 players!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        selectedPlayers.size > 12 -> {
                            Toast.makeText(context, "Maximum 12 players!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        roles.any { it.isEmpty() } -> {
                            Toast.makeText(context, "Assign roles to all players!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        roles.count { it == "Sheriff" } != 1 -> {
                            Toast.makeText(context, "Must have exactly 1 Sheriff!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        roles.count { it == "Don" } != 1 -> {
                            Toast.makeText(context, "Must have exactly 1 Don!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        hostId.isEmpty() -> {
                            Toast.makeText(context, "Select a host!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        hasWill && willUid.isEmpty() -> {
                            Toast.makeText(context, "Select player for Last Will!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        selectedPlayers.containsKey(hostId) -> {
                            Toast.makeText(context, "Host cannot be a player!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }


                }
                scope.launch {
                    val success = gamesRepository.createGame(
                        result = result,
                        season = 1,
                        notes = notes,
                        players = selectedPlayers,
                        allUsers = players,
                        hostId = hostId,
                        willUid = if(hasWill) willUid else "",
                        willGuesses = if(hasWill) willGuesses else 0
                    )
                    if(success){
                        Toast.makeText(context, "Game created!", Toast.LENGTH_SHORT).show()
                        selectedPlayers = emptyMap()
                        notes = ""
                        result = "town"

                    } else{
                        Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Game", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

    }

}

