package com.example.composeapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val NavBackground = Color(0xFF12101A)
val CardBg = Color(0xFF1E1B2E)
val PurpleMain = Color(0xFF7B2FBE)
val GreenColor = Color(0xFF4CAF50)
val RedColor = Color(0xFFE53935)
val GoldColor = Color(0xFFFFD700)

data class Player(
    val name: String,
    val rank: String,
    val rankColor: Color,
    val mmr: Int,
    val mmrChange: Int,
    val games: Int,
    val isAdmin: Boolean = false
)

fun getRankColor(rank: String): Color {
    return when (rank) {
        "LEGEND" -> GoldColor
        "MASTER" -> Color(0xFF9C27B0)
        "ELITE" -> Color(0xFFE53935)
        "VETERAN" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
}

@Composable
fun PlayersScreen() {
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Players") }
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val repository = remember { PlayerRepository() }
    val filters = listOf("All Players", "Top Rated", "Recent Active", "Admins")

    LaunchedEffect(Unit) {
        players = repository.getPlayers()
        isLoading = false
    }

    val filteredPlayers = when (selectedFilter) {
        "Admins" -> players.filter { it.isAdmin }
        "Top Rated" -> players.sortedByDescending { it.mmr }
        else -> players
    }.filter { it.name.contains(searchText, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().background(NavBackground)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(PurpleMain, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Text("⚡", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Players", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.size(36.dp).background(CardBg, CircleShape), contentAlignment = Alignment.Center) {
                Text("🔔", fontSize = 16.sp)
            }
        }

        OutlinedTextField(
            value = searchText, onValueChange = { searchText = it },
            placeholder = { Text("Search players...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleMain, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White, unfocusedContainerColor = CardBg, focusedContainerColor = CardBg),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Text("🔍", fontSize = 16.sp) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .background(if (isSelected) PurpleMain else CardBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(filter, color = if (isSelected) Color.White else Color.Gray, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("CLUB ROSTER", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Total ${filteredPlayers.size} Registered Members", color = Color.Gray, fontSize = 11.sp)
            }
            Text("Sorted by MMR", color = Color.Gray, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PurpleMain)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredPlayers) { player -> PlayerCard(player = player) }
            }
        }

        BottomNavBar()
    }
}

@Composable
fun PlayerCard(player: Player) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(PurpleMain, CircleShape), contentAlignment = Alignment.Center) {
                Text(player.name.first().toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(player.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.background(player.rankColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(player.rank, color = player.rankColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text("⚡ ${player.games} games", color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${player.mmr} MMR", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(if (player.mmrChange > 0) "+${player.mmrChange}" else "${player.mmrChange}", color = if (player.mmrChange > 0) GreenColor else RedColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("›", color = Color.Gray, fontSize = 20.sp)
        }
    }
}

@Composable
fun BottomNavBar() {
    val items = listOf("🎮" to "Games", "👥" to "Players", "➕" to "Create", "🔔" to "Activity", "⚙️" to "Settings")
    var selected by remember { mutableStateOf("Players") }

    Row(modifier = Modifier.fillMaxWidth().background(CardBg).padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
        items.forEach { (icon, label) ->
            val isSelected = label == selected
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
                Text(icon, fontSize = 20.sp)
                Text(label, color = if (isSelected) PurpleMain else Color.Gray, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}