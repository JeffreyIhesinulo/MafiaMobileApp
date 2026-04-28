package com.example.composeapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHost
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Scaffold(
        containerColor = NavBackground,
        bottomBar = {
            val items = listOf(
                "🎮" to "Games",
                "👥" to "Players",
                "➕" to "Create",
                "🔔" to "Activity",
                "⚙️" to "Profile"
            )
            val currentRoute = navController
                .currentBackStackEntryAsState().value?.destination?.route
                if (currentRoute != "login" && currentRoute != "register"){

        NavigationBar(containerColor = CardBg) {
            items.forEach { (icon, route) ->
                NavigationBarItem(
                    selected = currentRoute == route,
                    onClick = { navController.navigate(route) },
                    icon = { Text(icon, fontSize = 20.sp) },
                    label = { Text(route.replaceFirstChar { it.uppercase() }) }
                )
            }
        } }
    }
    ){ paddingValues ->   NavHost(
        navController = navController,
        startDestination = "login",
        modifier = Modifier
            .padding(paddingValues)
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = {
                    navController.navigate("players") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("players") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable("players") {
            PlayersScreen()
        }
        composable("profile") {
            ProfileScreen()
        }
    }}



}