package ipca.api.relay.presentation.messages.conversations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import ipca.api.relay.presentation.messages.conversations.components.ConversationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    onConversationClick: (
        conversationId: String,
        title: String,
        otherUserAvatar: String?,
        otherUserUsername: String?,
        otherUserDisplayName: String?,
    ) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConversationsViewModel = viewModel(factory = ConversationsViewModel.Factory)
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = { TopAppBar(title = { Text("Messages") }) }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.conversations.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No conversations yet", color = Color.Gray)
            }
            else -> LazyColumn(modifier = modifier.padding(padding)) {
                items(state.conversations) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        onClick = {
                            onConversationClick(
                                conversation.id,
                                conversation.resourceTitle,
                                conversation.otherUserAvatar,
                                conversation.otherUserUsername,
                                conversation.otherUserDisplayName,
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
