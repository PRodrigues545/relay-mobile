package ipca.api.relay.presentation.messages.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ipca.api.relay.presentation.messages.messages.components.MessageBubble

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    conversationId: String,
    conversationTitle: String,
    otherUserAvatar: String? = null,
    otherUserUsername: String? = null,
    otherUserDisplayName: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessagesViewModel = viewModel(
        factory = MessagesViewModel.factory(
            conversationId,
            conversationTitle,
            otherUserAvatar,
            otherUserUsername,
            otherUserDisplayName,
        )
    )
) {
    val state = viewModel.uiState
    val listState = rememberLazyListState()

    // The VM already computes this from the JWT exactly once at construction
    // time. Reading it through the VM avoids re-decoding the token (Base64 +
    // JSON parse) on every recomposition.
    val currentUserId = viewModel.currentUserId

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val headerName = state.otherUserDisplayName
        ?: state.otherUserUsername
        ?: conversationTitle
    val showResourceSubtitle = headerName != conversationTitle

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(headerName)
                        if (showResourceSubtitle) {
                            Text(
                                conversationTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.body,
                        onValueChange = viewModel::onBodyChange,
                        placeholder = { Text("Message...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 4
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = viewModel::sendMessage,
                        enabled = state.body.isNotBlank()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color(0xFF6666FF)
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    isOwn = message.senderId == currentUserId,
                    otherUserAvatar = state.otherUserAvatar,
                    otherUserUsername = state.otherUserDisplayName ?: state.otherUserUsername,
                    currentUserId = currentUserId,
                    onAcceptOffer = { viewModel.respondToOffer(message.id, true) },
                    onDeclineOffer = { viewModel.respondToOffer(message.id, false) }
                )
            }
        }
    }
}

/**
 * Decode the user id claim from a JWT without pulling in a dedicated library.
 * Public because [OperationRepositoryImpl] also reaches for it; that is a small
 * abstraction leak but keeping it here avoids a separate util file for one fn.
 *
 * Note: this only parses the payload — it does NOT validate the signature.
 * The server is the source of truth for token validity.
 */
fun decodeUserId(token: String): Int {
    return try {
        val payload = token.split(".").getOrNull(1) ?: return -1
        val decoded = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE))
        org.json.JSONObject(decoded).optInt("id", -1)
    } catch (e: Exception) {
        -1
    }
}