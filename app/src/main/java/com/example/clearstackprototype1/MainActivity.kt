package com.example.clearstackprototype1

import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.clearstackprototype1.ui.theme.ClearstackPrototype1Theme

class MainActivity : ComponentActivity() {

    private var hasPermission = mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        hasPermission.value = isNotificationServiceEnabled()
        setContent {

            ClearstackPrototype1Theme {
                if(hasPermission.value){
                    NotificationScreen()
                }else{
                    PermissionScreen(
                        onEnableClick = {
                            openNotificationSettings()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        hasPermission.value = isNotificationServiceEnabled()
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
            compareByDescending<ConversationThread>{
                when(
                    PriorityManager.getPriority(
                        SummaryStore.summaries[it.sender]?: ""
                    )
                ) {
                        Priority.HIGH -> 3
                        Priority.MEDIUM -> 2
                        Priority.LOW -> 1
                }
            }.thenByDescending { it.lastUpdated }
        )
        LazyColumn{

            items(
                items = sortedThreads,
                key = {it.sender}
            ){thread -> ThreadCard(thread)}
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
    thread: ConversationThread
){
    val summary = SummaryStore.summaries[thread.sender]?: "Generating summary..."
    val priority = PriorityManager.getPriority(summary)
    val cardColor = when(priority){
        Priority.HIGH -> Color(0xFFFFEBEE)
        Priority.MEDIUM -> Color(0xFFFFF8E1)
        Priority.LOW -> Color(0xFFE8F5E9)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),

        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = thread.appName,

            )

            Text(
                text = when(priority){
                    Priority.HIGH -> "🔴 HIGH"
                    Priority.MEDIUM -> "🟠 MEDIUM"
                    Priority.LOW -> "🟢 LOW"
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

            Text(
                text = summary
            )


        }
    }
}