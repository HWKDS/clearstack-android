package com.example.clearstackprototype1

import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.clearstackprototype1.ui.theme.ClearstackPrototype1Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            ClearstackPrototype1Theme {

                NotificationScreen(
                    onEnableClick = {
                        openNotificationSettings()
                    }
                )
            }
        }
    }
    private fun openNotificationSettings(){
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }
}

@Composable
fun NotificationScreen(
    onEnableClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onEnableClick
        ){
            Text("Enable Notification Access")
        }
        Spacer(
            modifier = Modifier.height(16.dp)
        )

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

        LazyColumn{

            items(
                items = NotificationStore.threads,
                key = {it.sender}
            ){thread -> ThreadCard(thread)}
        }
    }
}

@Composable
fun ThreadCard(
    thread: ConversationThread
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = thread.sender,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "${thread.messages.size} new messages"
            )

            Spacer(
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text =
                    SummaryStore.summaries[thread.sender]
                        ?: "Generating summary..."
            )
            Text(
                text = "Updated: ${thread.lastUpdated}"
            )

        }
    }
}