package ipca.api.relay.presentation.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ipca.api.relay.presentation.components.Logo

@Composable
fun SplashScreen(
    viewModel: SplashScreenViewModel = viewModel(factory = SplashScreenViewModel.Factory),
    onNavigateToHome: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event){
                is SplashNavigationEvent.NavigateToLogin -> onNavigateToLogin()
                is SplashNavigationEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    // Show your logo/branding while resolving
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Logo(modifier = Modifier.size(128.dp))
    }
}