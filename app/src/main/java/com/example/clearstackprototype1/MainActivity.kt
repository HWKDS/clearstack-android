package com.example.clearstackprototype1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavHostController
import com.example.clearstackprototype1.ui.theme.ClearstackPrototype1Theme
import java.io.File

class MainActivity : ComponentActivity() {

    // Class properties to hold UI state that can be accessed by private functions
    private var hasPermission by mutableStateOf(false)
    private var startDestination by mutableStateOf("notifications")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize state based on current permission and model existence
        hasPermission = isNotificationServiceEnabled()
        startDestination = if (hasPermission) {
            val modelFile = File(filesDir, "gemma3-1b-it.litertlm")
            if (modelFile.exists()) "notifications" else "model_download"
        } else {
            "permission"
        }

        setContent {
            ClearstackPrototype1Theme {
                // Check permission on resume to detect changes from settings
                DisposableEffect(Unit) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            val newHasPermission = isNotificationServiceEnabled()
                            if (hasPermission != newHasPermission) {
                                hasPermission = newHasPermission
                                val modelFile = File(filesDir, "gemma3-1b-it.litertlm")
                                startDestination = if (newHasPermission) {
                                    if (modelFile.exists()) "notifications" else "model_download"
                                } else {
                                    "permission"
                                }
                            }
                        }
                    }
                    ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
                    onDispose {
                        ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
                    }
                }

                // Also check for model existence when hasPermission changes
                LaunchedEffect(hasPermission) {
                    if (hasPermission) {
                        val modelFile = File(filesDir, "gemma3-1b-it.litertlm")
                        startDestination = if (modelFile.exists()) "notifications" else "model_download"
                    }
                }

                // Start loading notifications in the background
                LaunchedEffect(Unit) {
                    Thread {
                        NotificationLoader.loadThreads(this@MainActivity)
                    }.start()
                }

                ClearStackNavigation(startDestination = startDestination)
            }
        }
    }

    private fun checkPermissionAndUpdateState() {
        val newHasPermission = isNotificationServiceEnabled()
        if (hasPermission != newHasPermission) {
            hasPermission = newHasPermission
            updateStartDestination(newHasPermission)
        }
    }
    private fun updateStartDestination(hasPermission: Boolean) {
        if (!hasPermission) {
            startDestination = "permission"
        } else {
            val modelFile = File(filesDir, "gemma3-1b-it.litertlm")
            startDestination = if (modelFile.exists()) "notifications" else "model_download"
        }
    }

    private fun checkModelAndUpdateDestination() {
        val modelFile = File(filesDir, "gemma3-1b-it.litertlm")
        startDestination = if (modelFile.exists()) "notifications" else "model_download"
    }

    // check if notification setting is enabled
    private fun isNotificationServiceEnabled(): Boolean{
        val enabledListeners = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(packageName) == true
    }
    //open notification settings
    private fun openNotificationSettings(){
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }
}

@Composable
fun NotificationScreen(
    onThreadClick: (ConversationThread) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "ClearStack Notification",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if(NotificationStore.threads.isEmpty()){
            Text(
                text = "No notifications yet..."
            )
        }
        val sortedThreads = NotificationStore.threads.sortedWith(
            compareByDescending<ConversationThread>{ thread ->
                val priority =
                    AiInsightStore.insights[thread.sender]?.priority ?: "LOW"
                when(priority){
                    "HIGH" -> 3
                    "MEDIUM" -> 2
                    else -> 1
                }
            }.thenByDescending { it.lastUpdated }
        )
        val context = LocalContext.current
        LazyColumn{
            items(
                items = sortedThreads,
                key = {it.sender}
            ){ thread ->
                ThreadCard(
                    thread = thread,
                    onClick = {
                        onThreadClick(thread)
                    },
                    onDelete = { selectedThread ->

                        NotificationStore.threads.remove(
                            selectedThread
                        )
                        SummaryStore.summaries.remove(
                            selectedThread.sender
                        )
                        AiInsightStore.insights.remove(selectedThread.sender)
                        AnalysisStateStore.states.remove(selectedThread.sender)
                        Thread {

                            val dao =
                                DatabaseProvider
                                    .getDatabase(context)
                                    .notificationDao()

                            kotlinx.coroutines.runBlocking {

                                dao.deleteThread(
                                    selectedThread.sender
                                )

                            }

                        }.start()
                    }
                )
            }
        }
    }
}

@Composable
fun PermissionScreen(
    onEnableClick: () -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Please enable notification access"
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )
        Button(
            onClick = onEnableClick
        ) {
            Text("Enable Access")
        }
    }
}

@Composable
fun ThreadCard(
    thread: ConversationThread,
    onClick: () -> Unit,
    onDelete: (ConversationThread) -> Unit
){
    val summary = SummaryStore.summaries[thread.sender]?: ""
    val analysisState = AnalysisStateStore.states[thread.sender]?: AnalysisState.SUCCESS
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    val priority =
        AiInsightStore.insights[thread.sender]?.priority ?: "LOW"
    val cardColor = when(priority){
        "HIGH" -> Color(0xFFFFEBEE)
        "MEDIUM" -> Color(0xFFFFF8E1)
        else -> Color(0xFFE8F5E9)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    showDeleteDialog = true
                }
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    )

    {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = thread.appName,
            )

            Text(
                text = when(priority){
                    "HIGH" -> "🔴 HIGH"
                    "MEDIUM" -> "🟠 MEDIUM"
                    else -> "🟢 LOW"
                }
            )

            Text(
                text = thread.sender,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "${thread.messages.size} messages • ${TimeUtils.getTimeAgo(thread.lastUpdated)}"
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            when(analysisState){
                AnalysisState.ANALYZING -> {
                    Text("Analyzing conversation...")
                }
                AnalysisState.SUCCESS -> {
                    Text(summary)
                }

                AnalysisState.FAILED -> {
                    Text("⚠ Analysis failed. Tap to retry.")
                }
            }


        }
    }
    if(showDeleteDialog){
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Delete Thread")
            },
            text = {
                Text("Delete all notification from ${thread.sender}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(thread)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }

        )
    }
}