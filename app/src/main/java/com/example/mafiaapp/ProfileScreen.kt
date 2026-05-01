package com.example.composeapp

import android.app.Dialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.composeapp.NavBackground
import com.example.composeapp.PurpleMain
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.launch
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val repository = remember { ProfileRepository() }
    val tempPFP = Color(0xC4504D4D)
    val adminColor = Color(0xFF9C27B0)
    var showEditDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    if (showEditDialog)
    {
        var newUsername by remember { mutableStateOf(user?.username?:"") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false},
            title = {Text("Edit Username")},
            text = {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it},
                    label = { Text("Username")}
                )
            },
            confirmButton = {
                Button(
                    shape = RoundedCornerShape(14.dp),
                    onClick = {
                        scope.launch{
                            val success = repository.updateUsername(newUsername)
                            if (success)
                            {
                                user = user?.copy(username = newUsername)
                                showEditDialog = false
                                Toast.makeText(context, "Username updated!", Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {showEditDialog = false}) {
                    Text("Cancel", color = Color.Gray)
                }
            }

        )
    }

    LaunchedEffect(Unit)
    {
        user = repository.getCurrentUser()
        isLoading = false
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {


        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PurpleMain)
            }
        } else {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(DarkBackground))
            {
                Text(
                    text = "Player Profile",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )


                IconButton(onClick = { showEditDialog = true }
                , modifier = Modifier.align (Alignment.CenterEnd) ) {
                    Text("✏️", fontSize = 18.sp)
                }

            }


            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(contentAlignment = Alignment.BottomEnd)
            {
                Box(
                    modifier = Modifier.size(90.dp)
                        .clip(CircleShape)
                        .background(tempPFP),
                        contentAlignment = Alignment.Center
                )
                {
                    Text(text = user?.username?.first()?.uppercase() ?: "?",
                        fontSize = 36.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold)
                }


                Box(
                    modifier = Modifier.size(24.dp)
                        .clip(CircleShape)
                        .background(getRankColor(user?.rank ?: ""))
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = user?.username ?: "No user",
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            if(user?.isAdmin == true)
            {
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PurpleMain.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 4.dp))
                {
                    Text(text = "Admin",
                        color = adminColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

            }

            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = "${user?.mmr} MMR",
                color = Color.White
            )
            Spacer(modifier = Modifier.height(36.dp))

        }

            }

