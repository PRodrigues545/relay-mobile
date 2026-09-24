package ipca.api.relay.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import ipca.api.relay.presentation.messages.conversations.ConversationsScreen
import ipca.api.relay.presentation.messages.messages.MessagesScreen

fun NavGraphBuilder.messagesGraph(
    navController: NavController
){
    navigation<MessagesGraph>(startDestination = Conversations) {
        composable<Conversations> {
            ConversationsScreen(
                onConversationClick = { id, title, avatar, username, displayName ->
                    navController.navigate(
                        Conversation(
                            id = id,
                            title = title,
                            otherUserAvatar = avatar,
                            otherUserUsername = username,
                            otherUserDisplayName = displayName,
                        )
                    )
                }
            )
        }
        composable<Conversation> { backStack ->
            val args = backStack.toRoute<Conversation>()
            MessagesScreen(
                conversationId = args.id,
                conversationTitle = args.title,
                otherUserAvatar = args.otherUserAvatar,
                otherUserUsername = args.otherUserUsername,
                otherUserDisplayName = args.otherUserDisplayName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
