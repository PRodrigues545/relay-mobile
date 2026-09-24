package ipca.api.relay.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute // Ensure this is imported
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ipca.api.relay.navigation.HomeGraph
import ipca.api.relay.navigation.mainTabsGraph
import ipca.api.relay.presentation.components.RelayNavigationBar

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Visibility logic based on hierarchy
    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        val route = dest.route ?: ""
        route.contains("Home") || route.contains("Requests") ||
                route.contains("Create") || route.contains("Profile") || route.contains("Messages")
    } ?: false

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                RelayNavigationBar(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        // 1. Check if the target route graph is already present in the active hierarchy
                        val isCurrentTab = currentDestination?.hierarchy?.any {
                            it.hasRoute(route::class)
                        } == true

                        // 2. Only navigate if the user clicked a DIFFERENT tab
                        if (!isCurrentTab) {
                            bottomNavController.navigate(route) {
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    inclusive = false
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = HomeGraph,
            modifier = Modifier.padding(innerPadding)
        ) {
            mainTabsGraph(
                navController = bottomNavController,
                onLogout = onLogout
            )
        }
    }
}