package com.example.clearstackprototype1

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun ClearStackNavigation(startDestination: String = "notifications") {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
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
        composable("model_download") {
            ModelDownloadScreen(navController = navController)
        }
        composable("permission") {
            val context = LocalContext.current
            PermissionScreen(
                onEnableClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
            )
        }
    }
}