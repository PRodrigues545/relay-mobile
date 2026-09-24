package ipca.api.relay.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import ipca.api.relay.di.AppModule
import ipca.api.relay.presentation.create.*

fun NavGraphBuilder.createGraph(navController: NavHostController) {
    navigation<CreateGraph>(startDestination = CreateResource) {
        composable<CreateResource> {
            val viewModel: CreateResourceViewModel = viewModel(
                factory = CreateResourceViewModelFactory(AppModule.resourceRepository)
            )
            CreateResourceScreen(navController = navController, viewModel = viewModel)
        }
    }
}