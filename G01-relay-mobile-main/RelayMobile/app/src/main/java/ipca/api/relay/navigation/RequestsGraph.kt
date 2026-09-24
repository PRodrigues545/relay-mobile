package ipca.api.relay.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import ipca.api.relay.di.AppModule
import ipca.api.relay.presentation.create.*

fun NavGraphBuilder.requestsGraph(navController: NavHostController) {
    navigation<RequestsGraph>(startDestination = CreateRequest) {
        composable<CreateRequest> {
            val viewModel: CreateRequestViewModel = viewModel(
                factory = CreateRequestViewModelFactory(
                    AppModule.requestRepository,
                    AppModule.getTokenManager(),
                )
            )
            CreateRequestScreen(navController = navController, viewModel = viewModel)
        }
    }
}
