package com.example.clearstackprototype1

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun ClearStackNavigation(){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "notifications"
    ){
        composable("notifications"){
            NotificationScreen(
                onThreadClick = { thread ->
                    navController.navigate("conversation/${thread.sender}")
                }
            )
        }
        composable(
            route = "conversation/{sender}",
            arguments = listOf(
                navArgument("sender"){
                    type = NavType.StringType
                }
            )
            ){ backStackEntry ->
            val sender = backStackEntry.arguments?.getString("sender")
            val thread =
                NotificationStore.threads.firstOrNull{
                    it.sender == sender
                }
            if(thread != null){
                ConversationScreen(
                    thread = thread
                )
            }
        }
    }
}