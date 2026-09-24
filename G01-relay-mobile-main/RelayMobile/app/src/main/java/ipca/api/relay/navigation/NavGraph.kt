package ipca.api.relay.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ipca.api.relay.di.AppModule
import ipca.api.relay.presentation.MainScreen
import ipca.api.relay.presentation.splash.SplashScreen
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {

    LaunchedEffect(Unit) {
        AppModule.sessionExpiredEvent.receiveAsFlow().collect {
            navController.navigate(AuthGraph) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Splash
    ) {
        composable<Splash> {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(MainGraph) { popUpTo<Splash> { inclusive = true } }
                },
                onNavigateToLogin = {
                    navController.navigate(AuthGraph) { popUpTo<Splash> { inclusive = true } }
                }
            )
        }

        authGraph(
            navController = navController,
            onLoginSuccess = {
                navController.navigate(MainGraph) { popUpTo(0) { inclusive = true } }
            }
        )

        composable<MainGraph> {
            MainScreen(
                rootNavController = navController,
                onLogout = {
                    navController.navigate(AuthGraph) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}