package com.example.clearstackprototype1

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.input.pointer.stylusHoverIcon
import androidx.compose.ui.modifier.modifierLocalConsumer

@Composable
fun ConversationScreen (
    thread: ConversationThread
){
    val insight =
        AiInsightStore.insights[thread.sender]
    val summary =
        insight?.summary
            ?: SummaryStore.summaries[thread.sender]
            ?: "Generating Ai summary..."
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = thread.appName,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = thread.sender,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "🧠AI Summary",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyLarge
        )
        InfoSection(
            title = "📋 Tasks",
            items = insight?.tasks ?: emptyList()
        )
        InfoSection(
            title = "💰 Payments",
            items = insight?.payments ?: emptyList()
        )

        InfoSection(
            title = "📅 Meetings",
            items = insight?.meetings ?: emptyList()
        )

        InfoSection(
            title = "⏰ Reminders",
            items = insight?.reminders ?: emptyList()
        )
        InfoValue(
            title = "🔑 OTP",
            value = insight?.otp
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Messages",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        if(thread.messages.isEmpty()){
            Text(
                text = "No messages yet.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        else{
            LazyColumn{

                items(
                    thread.messages.sortedBy {
                        it.timeStamp
                    }
                ){message -> MessageCard(message)}
            }
        }
    }

}
@Composable
fun MessageCard(
    message: NotificationData
){
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = TimeUtils.getTimeAgo(message.timeStamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    items: List<String>
){
    if(items.isEmpty()) return
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.height(20.dp))

    items.forEach{ item ->
        Text(
            text = "• $item",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Composable
fun InfoValue(
    title: String,
    value: String?
) {

    if (value.isNullOrBlank()) return

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge
    )

}