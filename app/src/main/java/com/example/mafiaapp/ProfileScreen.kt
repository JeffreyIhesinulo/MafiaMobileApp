package com.example.composeapp

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import android.R
import android.R.attr.onClick
import android.app.Dialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.composeapp.NavBackground
import com.example.composeapp.PurpleMain
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.launch
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val repository = remember { ProfileRepository() }
    val adminColor = Color(0xFF9C27B0)
    var showEditDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var recentGames by remember { mutableStateOf<List<Game>>(emptyList())}


    if (showEditDialog) {
        var newUsername by remember { mutableStateOf(user?.username ?: "") }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Username") },
            text = {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Username") }
                )
            },
            confirmButton = {
                Button(
                    shape = RoundedCornerShape(14.dp),
                    onClick = {
                        scope.launch {
                            val success = repository.updateUsername(newUsername)
                            if (success) {
                                user = user?.copy(username = newUsername)
                                showEditDialog = false
                                Toast.makeText(context, "Username updated!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
    var mmrHistory by remember { mutableStateOf<List<Pair<Long, Int>>>(emptyList()) }
    val gamesRepository = remember { GamesRepository()}

    LaunchedEffect(Unit) {
        user = repository.getCurrentUser()
        user?.let {
            mmrHistory = gamesRepository.getPlayerMMRHistory(it.uid)
            recentGames = gamesRepository.getPlayerRecentGames(it.uid)
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PurpleMain)
            }
        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(PurpleMain, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("👤", fontSize = 16.sp) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) { Text("✏️", fontSize = 18.sp) }
            }

            Spacer(modifier = Modifier.height(24.dp))


            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(CardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user?.username?.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 36.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(getRankColor(user?.rank ?: ""))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))


            Text(user?.username ?: "", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))


            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(getRankColor(user?.rank ?: "").copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(user?.rank ?: "", color = getRankColor(user?.rank ?: ""), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                if (user?.isAdmin == true) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PurpleMain.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Admin", color = adminColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (user?.isAdmin == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate("pending") },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleMain)
                ) {
                    Text("Pending Approvals")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CURRENT RATING", color = Color.Gray, fontSize = 11.sp)
                    Text(
                        "${user?.mmr ?: 0} MMR",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
                    .padding(vertical = 16.dp)
            ) {
                val winRate = if ((user?.games ?: 0) != 0)
                    (user?.wins?.toFloat()!! / user?.games!! * 100).toInt()
                else 0

                listOf(
                    "${user?.games ?: 0}" to "GAMES",
                    "${user?.wins ?: 0}" to "WINS",
                    "${user?.losses ?: 0}" to "LOSSES",
                    "$winRate%" to "WINRATE"
                ).forEachIndexed { index, (value, label) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(label, color = Color.Gray, fontSize = 11.sp)
                    }
                    if (index < 3) {
                        Box(modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color.Gray.copy(alpha = 0.3f)))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

          Box(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(CardBg)
                  .padding(16.dp)

          )
          {
              Column {
                  Text("Rating Progress",
                      color = Color.White,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold)
                  Text("LAST MONTH",
                      color = Color.Gray,
                      fontSize = 11.sp)
                  Spacer(modifier = Modifier.height(16.dp))

                  if (mmrHistory.isEmpty()){
                      Text("No games this month", color = Color.Gray, fontSize = 12.sp)
                  } else {
                      var runningMmr = (user?.mmr ?: 0) - mmrHistory.sumOf{ it.second}
                      val points = mmrHistory.map {(_,change) ->
                          runningMmr += change
                          runningMmr
                      }
                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                          Text("${points.minOrNull() ?: 0}", color = Color.Gray, fontSize = 10.sp)
                          Text("${points.maxOrNull() ?: 0}", color = Color.Gray, fontSize = 10.sp)
                      }
                      Canvas(
                          modifier = Modifier
                              .fillMaxWidth()
                              .height(120.dp)
                      ) {


                          val width = size.width
                          val height = size.height
                          val padding = 16.dp.toPx()


                          val minMmr = points.min().toFloat()
                          val maxMmr = points.max().toFloat()
                          val range = (maxMmr - minMmr).coerceAtLeast(1f)

                          val path = Path()
                          points.forEachIndexed { index, mmr ->
                              val x = padding + (index.toFloat() / (points.size - 1).coerceAtLeast(1)) * (width - padding * 2)
                              val y = height - padding - ((mmr - minMmr) / range) * (height - padding * 2)
                              if (index == 0) path.moveTo(x, y)
                              else path.lineTo(x, y)
                          }
                          val gridLines = 3
                          for (i in 0..gridLines) {
                              val y = padding + (i.toFloat() / gridLines) * (height - padding * 2)
                              drawLine(
                                  color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.2f),
                                  start = Offset(padding, y),
                                  end = Offset(width - padding, y),
                                  strokeWidth = 1.dp.toPx()
                              )
                          }

                          drawPath(
                              path = path,
                              color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                              style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                          )

                          points.forEachIndexed { index, mmr ->
                              val x = padding + (index.toFloat() / (points.size - 1).coerceAtLeast(1)) * (width - padding * 2)
                              val y = height - padding - ((mmr - minMmr) / range) * (height - padding * 2)
                              drawCircle(
                                  color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                  radius = 4.dp.toPx(),
                                  center = Offset(x, y)
                              )
                          }

                      }
                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                          mmrHistory.firstOrNull()?.let {
                              Text(formatDate(it.first).take(6), color = Color.Gray, fontSize = 10.sp)
                          }
                          mmrHistory.lastOrNull()?.let {
                              Text(formatDate(it.first).take(6), color = Color.Gray, fontSize = 10.sp)
                          }
                      }


                  }
                  Spacer(modifier = Modifier.height(24.dp))

                  Box(
                      modifier = Modifier
                          .fillMaxWidth()
                          .clip(RoundedCornerShape(12.dp))
                          .background(CardBg)
                          .padding(16.dp)
                  ){
                      Column{
                          Text("Recent Games", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                          Spacer(modifier = Modifier.height(8.dp))

                          if(recentGames.isEmpty())
                          {
                              Text("No recent games", color = Color.Gray, fontSize = 12.sp)

                          }else{
                              recentGames.forEach { game ->
                                  val myPlayer = game.players.find { it.uid == user?.uid}
                                  RecentGameRow(game = game, myPlayer = myPlayer, onClick = { navController.navigate("game/${game.id}")})
                                  Spacer(modifier = Modifier.height(8.dp))
                              }
                          }




                      }

                  }



              }

          }

        }
    }
}
@Composable
fun RecentGameRow(game: Game, myPlayer: GamePlayer?, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NavBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable{onClick()},
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "#GM-${game.gameNumber}",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = myPlayer?.role ?: "",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "+ ${myPlayer?.mmrChange ?: 0}",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(if (game.result == "town") LightGreen.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
            {
                Text(
                    text = if (game.result == "town") "♥ Town" else "♠ Mafia",
                    color = if (game.result == "town") LightGreen else Color.Red,
                    fontSize = 13.sp
                )
            }
        }
    }
}