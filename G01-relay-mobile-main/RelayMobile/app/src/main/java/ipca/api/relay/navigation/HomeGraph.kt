package ipca.api.relay.navigation

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import ipca.api.relay.di.AppModule
import ipca.api.relay.presentation.home.HomeScreen
import ipca.api.relay.presentation.home.HomeViewModel
import ipca.api.relay.presentation.notifications.NotificationsScreen

// The "home_with_category" composable was a deep-link target for the deleted
// Search tab — Home now owns search and category filtering inline, so no
// secondary entry into Home is needed.
fun NavGraphBuilder.homeGraph(navController: NavHostController) {
    navigation<HomeGraph>(startDestination = Home) {
        composable<Home> {
            val viewModel = remember {
                HomeViewModel(
                    repository = AppModule.resourceRepository,
                    notificationRepository = AppModule.notificationRepository
                )
            }
            // Reload unread count every time this screen becomes visible
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.loadUnreadCount()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            HomeScreen(navController, viewModel)
        }

        composable<Notifications> {
            NotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}